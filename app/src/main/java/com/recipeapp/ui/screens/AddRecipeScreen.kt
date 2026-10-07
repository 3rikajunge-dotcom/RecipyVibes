package com.recipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeapp.ui.viewmodel.AddRecipeViewModel

@Composable
fun AddRecipeScreen(vm: AddRecipeViewModel, onDone: (Long) -> Unit) {
    var url by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()

    Column(Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Add Recipe from URL", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Recipe URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.addFromUrl(url) },
            enabled = url.isNotBlank() && state !is AddRecipeViewModel.AddState.Loading
        ) { Text("Fetch & Rewrite") }

        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            is AddRecipeViewModel.AddState.Loading -> CircularProgressIndicator()
            is AddRecipeViewModel.AddState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            is AddRecipeViewModel.AddState.Success -> {
                Text("Saved: ${s.recipe.title}")
                LaunchedEffect(s.recipe.id) { onDone(s.recipe.id) }
            }
            else -> {}
        }
    }
}
