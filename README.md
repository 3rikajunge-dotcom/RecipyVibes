# Recipe App

Native Android app (Kotlin + Jetpack Compose + Room) that:
- Pulls a recipe from a URL and has Gemini rewrite it concisely
- Stores recipes locally in a searchable database
- Lets you build a "Cook List" and generates a merged shopping list from it
- Suggests a week of meals once you mark each day quick/elaborate/either

## AI: Google Gemini (free tier)
Google keeps an **ongoing free tier** for Gemini Flash models (no credit card,
no expiring trial credit) -- rate-limited to roughly 10-15 requests/minute and
a few hundred to ~1,500/day, comfortably enough for a personal app like this.
Two things worth knowing:

- **Commercial use is excluded** from the free tier's terms -- fine for your
  own personal use, not for shipping to other users or monetizing.
- Rate limits shift over time as Google tunes them; check current limits at
  https://ai.google.dev/gemini-api/docs/rate-limits if you start seeing 429 errors.

## Your API key: entered in-app, not baked into the build
The Gemini API key is **not** stored in `local.properties` or compiled into
the app. Instead, the first time you launch the app it takes you to a
Settings screen where you paste in your own key; it's then stored encrypted
on your device only (`androidx.security:security-crypto` / EncryptedSharedPreferences),
never in source control or the APK. Get a free key at
https://aistudio.google.com/apikey (Google account, no credit card).

This matters because a key baked into the app at build time is technically
extractable by decompiling the APK. Since this app only runs on your own
phone, that risk is mostly theoretical -- but entering the key at runtime
instead of at build time avoids it entirely at basically no extra cost.

If you ever *do* want to distribute this app to other people, each using
their own account automatically (rather than each person manually pasting a
key), the next step up is Gemini's OAuth flow (sign in with Google, per-user
token) -- more setup (OAuth consent screen, signing-cert-bound client ID,
Credential Manager integration) but no copy-pasted keys at all. Ask if you
want that built in.

## What's implemented
- Full Gradle project structure
- Room DB: `Recipe`, `CookListItem`, `MealPlanEntry`
- `ApiKeyStore` -- encrypted on-device storage for your Gemini key
- `PageFetcher` (Jsoup) -- strips a recipe web page down to readable text
- `GeminiClient` -- calls Gemini 2.5 Flash via `generativelanguage.googleapis.com` to:
  - extract + rewrite a recipe as structured JSON (using Gemini's JSON response mode for reliability)
  - suggest a week of meals given day-by-day complexity preferences
- `RecipeRepository` -- DB/network glue + shopping-list ingredient merging
- Compose screens: Settings (API key entry), Recipe List/Search, Add-from-URL,
  Recipe Detail, Shopping List, Weekly Plan
- Navigation wiring + `MainActivity`

## Setup
1. Open the `RecipeApp` folder in Android Studio (Koala or newer).
2. Copy `local.properties.example` → `local.properties`, set `sdk.dir` to your Android SDK path.
3. Let Gradle sync, then Run on an emulator or device (minSdk 26 / Android 8+).
4. On first launch, tap through to Settings and paste in your free Gemini API key
   (https://aistudio.google.com/apikey). You can get back to this screen anytime
   via the "API Key" button in the top bar.

## Known gaps / good next steps
- **Meal plan → actual assignment:** the planner currently shows Gemini's suggestions
  as text; it doesn't yet parse them back into `MealPlanEntry` rows or let you tap
  "add to cook list" from a suggestion. That's the natural next feature.
- **Editing recipes:** no edit screen yet for tweaking a saved recipe or its ingredients.
- **Rate limit handling:** `GeminiClient` throws on non-2xx (including 429 rate-limit
  errors) and the UI shows the raw message. Worth adding backoff/retry.
- **Offline-first:** recipe adding requires network (by design); everything else
  (browsing, search, shopping list) works fully offline already.
- **Share-sheet intent:** `AndroidManifest.xml` already declares the `SEND` intent filter
  so you can share a URL from Chrome straight into the app -- `MainActivity` has a TODO
  comment where to read `Intent.EXTRA_TEXT` and pre-fill the Add screen.
- **Unit tests:** none yet -- `buildShoppingList()`'s merging logic is the highest-value
  thing to cover first.

## Project layout
```
app/src/main/java/com/recipeapp/
  data/            ApiKeyStore (encrypted key storage)
  data/model/      Recipe, Ingredient, CookListItem, MealPlanEntry, ShoppingListLine
  data/db/         Room DAOs + AppDatabase
  data/repository/ RecipeRepository (DB + network + shopping-list logic)
  network/         PageFetcher (Jsoup), GeminiClient (Google Gemini API)
  ui/viewmodel/    One ViewModel per screen
  ui/screens/      Compose screens, including SettingsScreen
  ui/nav/          NavHost + ViewModel factory
  MainActivity.kt
```
