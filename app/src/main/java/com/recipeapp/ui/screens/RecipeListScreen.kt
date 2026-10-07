package com.recipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeapp.data.model.Recipe
import com.recipeapp.ui.viewmodel.RecipeListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    vm: RecipeListViewModel,
    onAddRecipe: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onOpenShoppingList: () -> Unit,
    onOpenMealPlan: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val query by vm.query.collectAsState()
    val recipes by vm.recipes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") },
                actions = {
                    TextButton(onClick = onOpenSettings) { Text("API Key") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) { Text("+") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                label = { Text("Search recipes") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = onOpenShoppingList) { Text("Shopping List") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onOpenMealPlan) { Text("Weekly Plan") }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeRow(recipe, onClick = { onOpenRecipe(recipe.id) }, onAddToCookList = { vm.addToCookList(recipe.id) })
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(recipe: Recipe, onClick: () -> Unit, onAddToCookList: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                val meta = listOfNotNull(
                    recipe.complexity,
                    recipe.prepMinutes?.let { "${it}m prep" },
                    recipe.servings?.let { "${it} servings" }
                ).joinToString(" · ")
                Text(meta, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onAddToCookList) { Text("+ Cook List") }
        }
    }
}
