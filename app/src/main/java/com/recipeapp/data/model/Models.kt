package com.recipeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceUrl: String?,
    val concisedText: String,       // the AI-rewritten, concise recipe (markdown)
    val ingredientsJson: String,    // JSON-encoded List<IngredientLine>
    val stepsJson: String,          // JSON-encoded List<String>
    val tags: String = "",          // comma-separated, e.g. "quick,vegetarian"
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
    val servings: Int? = null,
    val complexity: String = "medium", // "quick" | "medium" | "elaborate"
    val dateAdded: Long = System.currentTimeMillis()
)

@Serializable
data class IngredientLine(
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val notes: String? = null
)

@Entity(tableName = "cook_list")
data class CookListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_plan")
data class MealPlanEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartEpochDay: Long,   // identifies which week this belongs to
    val dayOfWeek: Int,            // 1=Mon .. 7=Sun
    val desiredComplexity: String, // "quick" | "elaborate" | "either"
    val recipeId: Long? = null     // filled in once Claude / user picks a recipe
)

/** Parsed structure Claude returns when extracting a recipe from a web page. */
@Serializable
data class ExtractedRecipe(
    val title: String,
    val concisedText: String,
    val ingredients: List<IngredientLine>,
    val steps: List<String>,
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
    val servings: Int? = null,
    val complexity: String = "medium",
    val tags: List<String> = emptyList()
)

/** One line in the generated shopping list, after merging duplicate ingredients. */
data class ShoppingListLine(
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val fromRecipes: List<String>
)
