package com.recipeapp.network

import android.content.Context
import com.recipeapp.data.ApiKeyStore
import com.recipeapp.data.model.ExtractedRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Calls Google's Gemini API (generativelanguage.googleapis.com). Gemini Flash
 * has a genuinely ongoing free tier (unlike one-time trial credits elsewhere) --
 * rate-limited, but comfortably enough for a personal app making occasional
 * requests. Get a key at https://aistudio.google.com/apikey.
 *
 * The key is never baked into the app at build time -- it's typed in once via
 * SettingsScreen and stored encrypted on-device through ApiKeyStore. Nothing
 * secret lives in source control or the compiled APK.
 */
object GeminiClient {

    private const val MODEL = "gemini-flash-latest"
    private const val API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
    private val JSON_MEDIA = "application/json".toMediaType()
    private val json = Json { ignoreUnknownKeys = true }

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    class NoApiKeyException : Exception("No Gemini API key saved yet. Add one in Settings.")

    /** @param jsonOutput if true, asks Gemini to constrain its output to valid JSON. */
    private fun call(context: Context, systemPrompt: String, userPrompt: String, jsonOutput: Boolean): String {
        val apiKey = ApiKeyStore.getKey(context) ?: throw NoApiKeyException()

        val generationConfig = JSONObject().apply {
            if (jsonOutput) put("responseMimeType", "application/json")
            put("thinkingConfig", JSONObject().put("thinkingLevel", "minimal"))
        }

        val body = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }
            ))
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody(JSON_MEDIA))
            .build()

        http.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw RuntimeException("Gemini API error ${resp.code}: ${resp.body?.string()}")
            }
            val respJson = JSONObject(resp.body!!.string())
            val candidates = respJson.optJSONArray("candidates")
                ?: throw RuntimeException("Gemini returned no candidates: $respJson")
            val parts = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                sb.append(parts.getJSONObject(i).optString("text"))
            }
            return sb.toString()
        }
    }

    /** Fetches [url], extracts the recipe, and rewrites it concisely as structured JSON. */
    suspend fun extractRecipeFromUrl(context: Context, url: String): ExtractedRecipe = withContext(Dispatchers.IO) {
        val pageText = PageFetcher.fetchReadableText(url)

        val system = """
            You extract recipes from messy web page text and rewrite them concisely.
            Respond with ONLY valid JSON matching exactly this shape:
            {
              "title": string,
              "concisedText": string,
              "ingredients": [{"name": string, "quantity": number|null, "unit": string|null, "notes": string|null}],
              "steps": [string],
              "prepMinutes": number|null,
              "cookMinutes": number|null,
              "servings": number|null,
              "complexity": "quick" | "medium" | "elaborate",
              "tags": [string]
            }
            "concisedText" is a short markdown recipe: title, ingredient list, numbered steps -- no
            life stories, no ads, no filler. "quick" = roughly <= 30 min total and few steps,
            "elaborate" = long or multi-component. Normalize units where sensible (tbsp, cup, g).
            If the page has no real recipe, do your best from whatever content is present.
        """.trimIndent()

        val user = "Page URL: $url\n\nPage text:\n$pageText"

        val raw = call(context, system, user, jsonOutput = true)
        json.decodeFromString<ExtractedRecipe>(raw)
    }

    /**
     * Asks Gemini to suggest meals for a week given the day-by-day complexity
     * preferences and a pool of already-saved recipes it can pick from.
     */
    suspend fun suggestWeeklyMeals(
        context: Context,
        dayComplexities: Map<String, String>,
        savedRecipeTitles: List<String>
    ): String = withContext(Dispatchers.IO) {
        val system = """
            You are a meal-planning assistant. Given a list of days with a desired
            complexity ("quick" = under ~30 min, simple; "elaborate" = a bigger
            weekend-style cook; "either" = no preference) and a pool of recipes the
            user already has saved, suggest one meal per day.
            Prefer picking from the saved recipes when a good fit exists for that
            day's complexity. Only suggest a brand-new recipe idea (not in the pool)
            when nothing saved fits well, and clearly mark those as "(new idea)".
            Respond in concise markdown: one line per day, bolded day name, then the
            meal name and a 5-10 word reason it fits.
        """.trimIndent()

        val user = buildString {
            appendLine("Days and desired complexity:")
            dayComplexities.forEach { (day, c) -> appendLine("- $day: $c") }
            appendLine()
            appendLine("Saved recipes available to choose from:")
            if (savedRecipeTitles.isEmpty()) appendLine("(none saved yet)")
            else savedRecipeTitles.forEach { appendLine("- $it") }
        }

        call(context, system, user, jsonOutput = false)
    }
}
