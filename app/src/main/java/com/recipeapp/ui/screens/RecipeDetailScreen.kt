package com.recipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeapp.data.repository.RecipeRepository
import com.recipeapp.data.model.Recipe
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(repo: RecipeRepository, recipeId: Long, onAddToCookList: (Long) -> Unit) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(recipeId) { recipe = repo.getRecipe(recipeId) }

    recipe?.let { r ->
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(r.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(listOfNotNull(
                r.complexity,
                r.prepMinutes?.let { "${it}m prep" },
                r.cookMinutes?.let { "${it}m cook" },
                r.servings?.let { "${it} servings" }
            ).joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Text(r.concisedText) // concise, AI-rewritten markdown recipe
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onAddToCookList(r.id) }) { Text("Add to Cook List") }
            r.sourceUrl?.let {
                Spacer(Modifier.height(8.dp))
                Text("Source: $it", style = MaterialTheme.typography.labelSmall)
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
