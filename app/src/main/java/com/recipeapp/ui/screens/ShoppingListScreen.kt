package com.recipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeapp.ui.viewmodel.ShoppingListViewModel

@Composable
fun ShoppingListScreen(vm: ShoppingListViewModel) {
    val cookList by vm.cookList.collectAsState()
    val shoppingList by vm.shoppingList.collectAsState()

    LaunchedEffect(Unit) { vm.generate() }

    Column(Modifier.padding(16.dp).fillMaxSize()) {
        Text("Cook List", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (cookList.isEmpty()) {
            Text("Nothing queued yet — add recipes from the recipe list.")
        } else {
            cookList.forEach { recipe ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(recipe.title)
                    TextButton(onClick = { vm.removeFromCookList(recipe.id) }) { Text("Remove") }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Shopping List", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(shoppingList) { line ->
                val qtyStr = listOfNotNull(
                    line.quantity?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
                    line.unit
                ).joinToString(" ")
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text("• ${line.name}${if (qtyStr.isNotBlank()) " — $qtyStr" else ""}")
                    Text(
                        "for: ${line.fromRecipes.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
