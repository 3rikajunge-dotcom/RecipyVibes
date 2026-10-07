package com.recipeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.recipeapp.data.db.AppDatabase
import com.recipeapp.data.repository.RecipeRepository
import com.recipeapp.ui.nav.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(applicationContext)
        val repo = RecipeRepository(db, applicationContext)

        // If launched via Android's Share sheet with a recipe URL, you could read
        // it here from the intent and pre-fill the Add screen -- left as a TODO
        // hook so you can wire it up once the core flow feels right.

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(repo = repo)
                }
            }
        }
    }
}
