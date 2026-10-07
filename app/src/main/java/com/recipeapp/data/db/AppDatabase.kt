package com.recipeapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.recipeapp.data.model.CookListItem
import com.recipeapp.data.model.MealPlanEntry
import com.recipeapp.data.model.Recipe

@Database(
    entities = [Recipe::class, CookListItem::class, MealPlanEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun cookListDao(): CookListDao
    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_app.db"
                ).build().also { INSTANCE = it }
            }
    }
}
