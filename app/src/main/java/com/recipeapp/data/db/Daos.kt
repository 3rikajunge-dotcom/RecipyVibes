package com.recipeapp.data.db

import androidx.room.*
import com.recipeapp.data.model.CookListItem
import com.recipeapp.data.model.MealPlanEntry
import com.recipeapp.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("SELECT * FROM recipes ORDER BY dateAdded DESC")
    fun observeAll(): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes
        WHERE title LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
           OR concisedText LIKE '%' || :query || '%'
        ORDER BY dateAdded DESC
    """)
    fun search(query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Long): Recipe?

    @Query("SELECT * FROM recipes WHERE complexity = :complexity")
    suspend fun getByComplexity(complexity: String): List<Recipe>
}

@Dao
interface CookListDao {
    @Insert
    suspend fun insert(item: CookListItem): Long

    @Query("DELETE FROM cook_list WHERE recipeId = :recipeId")
    suspend fun removeByRecipeId(recipeId: Long)

    @Query("""
        SELECT recipes.* FROM recipes
        INNER JOIN cook_list ON recipes.id = cook_list.recipeId
        ORDER BY cook_list.dateAdded DESC
    """)
    fun observeCookListRecipes(): Flow<List<Recipe>>

    @Query("SELECT COUNT(*) FROM cook_list WHERE recipeId = :recipeId")
    suspend fun isInCookList(recipeId: Long): Int
}

@Dao
interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MealPlanEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MealPlanEntry>)

    @Query("SELECT * FROM meal_plan WHERE weekStartEpochDay = :weekStart ORDER BY dayOfWeek ASC")
    fun observeWeek(weekStart: Long): Flow<List<MealPlanEntry>>

    @Query("DELETE FROM meal_plan WHERE weekStartEpochDay = :weekStart")
    suspend fun clearWeek(weekStart: Long)
}
