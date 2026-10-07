package com.recipeapp.data.repository

import android.content.Context
import com.recipeapp.data.db.AppDatabase
import com.recipeapp.data.model.*
import com.recipeapp.network.GeminiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecipeRepository(private val db: AppDatabase, private val appContext: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun observeAllRecipes(): Flow<List<Recipe>> = db.recipeDao().observeAll()

    fun search(query: String): Flow<List<Recipe>> = db.recipeDao().search(query)

    fun observeCookList(): Flow<List<Recipe>> = db.cookListDao().observeCookListRecipes()

    suspend fun getRecipe(id: Long): Recipe? = db.recipeDao().getById(id)

    /** Fetches a URL, has Gemini extract+rewrite the recipe, and saves it to the DB. */
    suspend fun addRecipeFromUrl(url: String): Recipe {
        val extracted = GeminiClient.extractRecipeFromUrl(appContext, url)
        val recipe = Recipe(
            title = extracted.title,
            sourceUrl = url,
            concisedText = extracted.concisedText,
            ingredientsJson = json.encodeToString(extracted.ingredients),
            stepsJson = json.encodeToString(extracted.steps),
            tags = extracted.tags.joinToString(","),
            prepMinutes = extracted.prepMinutes,
            cookMinutes = extracted.cookMinutes,
            servings = extracted.servings,
            complexity = extracted.complexity
        )
        val id = db.recipeDao().insert(recipe)
        return recipe.copy(id = id)
    }

    suspend fun addToCookList(recipeId: Long) {
        if (db.cookListDao().isInCookList(recipeId) == 0) {
            db.cookListDao().insert(CookListItem(recipeId = recipeId))
        }
    }

    suspend fun removeFromCookList(recipeId: Long) =
        db.cookListDao().removeByRecipeId(recipeId)

    /**
     * Builds a shopping list from every recipe currently on the Cook List,
     * merging duplicate ingredients (same name + unit) by summing quantities.
     */
    suspend fun buildShoppingList(): List<ShoppingListLine> {
        val recipes = db.cookListDao().observeCookListRecipes()
        // Use a one-shot read via first emitted value.
        val cookListRecipes = kotlinx.coroutines.flow.first(recipes)

        data class Key(val name: String, val unit: String?)
        val merged = LinkedHashMap<Key, MutableList<Pair<Double?, String>>>() // qty, recipeTitle

        for (recipe in cookListRecipes) {
            val ingredients: List<IngredientLine> =
                json.decodeFromString(recipe.ingredientsJson)
            for (ing in ingredients) {
                val key = Key(ing.name.trim().lowercase(), ing.unit?.trim()?.lowercase())
                merged.getOrPut(key) { mutableListOf() }.add(ing.quantity to recipe.title)
            }
        }

        return merged.map { (key, entries) ->
            val totalQty = if (entries.all { it.first != null })
                entries.sumOf { it.first ?: 0.0 } else null
            ShoppingListLine(
                name = key.name.replaceFirstChar { it.uppercase() },
                quantity = totalQty,
                unit = key.unit,
                fromRecipes = entries.map { it.second }.distinct()
            )
        }.sortedBy { it.name }
    }

    suspend fun saveMealPlanWeek(weekStart: Long, entries: List<MealPlanEntry>) {
        db.mealPlanDao().clearWeek(weekStart)
        db.mealPlanDao().insertAll(entries.map { it.copy(weekStartEpochDay = weekStart) })
    }

    fun observeMealPlanWeek(weekStart: Long): Flow<List<MealPlanEntry>> =
        db.mealPlanDao().observeWeek(weekStart)

    /** Asks Gemini for suggestions given each day's desired complexity. */
    suspend fun suggestWeeklyMeals(dayComplexities: Map<String, String>): String {
        val savedTitles = kotlinx.coroutines.flow.first(db.recipeDao().observeAll()).map { it.title }
        return GeminiClient.suggestWeeklyMeals(appContext, dayComplexities, savedTitles)
    }
}
