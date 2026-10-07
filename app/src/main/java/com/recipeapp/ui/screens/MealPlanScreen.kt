package com.recipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeapp.ui.viewmodel.MealPlanViewModel

private val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
private val OPTIONS = listOf("quick", "either", "elaborate")

@Composable
fun MealPlanScreen(vm: MealPlanViewModel) {
    val selections = remember { mutableStateMapOf<String, String>().apply { DAYS.forEach { put(it, "either") } } }
    val suggestion by vm.suggestion.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(Modifier.padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Plan Your Week", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Pick how much effort each day should take.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))

        DAYS.forEach { day ->
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(day, style = MaterialTheme.typography.titleSmall)
                Row {
                    OPTIONS.forEach { opt ->
                        FilterChip(
                            selected = selections[day] == opt,
                            onClick = { selections[day] = opt },
                            label = { Text(opt) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.requestSuggestions(selections.toMap()) }, enabled = !loading) {
            Text(if (loading) "Thinking..." else "Suggest Meals")
        }

        Spacer(Modifier.height(16.dp))
        suggestion?.let {
            Text("Suggestions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(it)
        }
    }
}
