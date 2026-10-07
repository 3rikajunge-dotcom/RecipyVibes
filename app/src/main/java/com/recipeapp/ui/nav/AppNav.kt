package com.recipeapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.recipeapp.data.ApiKeyStore
import com.recipeapp.data.repository.RecipeRepository
import com.recipeapp.ui.screens.*
import com.recipeapp.ui.viewmodel.*
import kotlinx.coroutines.launch

class VMFactory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        RecipeListViewModel::class.java -> RecipeListViewModel(repo) as T
        AddRecipeViewModel::class.java -> AddRecipeViewModel(repo) as T
        ShoppingListViewModel::class.java -> ShoppingListViewModel(repo) as T
        MealPlanViewModel::class.java -> MealPlanViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel $modelClass")
    }
}

@Composable
fun AppNavHost(repo: RecipeRepository) {
    val navController = rememberNavController()
    val factory = VMFactory(repo)
    val context = LocalContext.current
    val startDestination = if (ApiKeyStore.hasKey(context)) "list" else "settings"

    NavHost(navController, startDestination = startDestination) {
        composable("settings") {
            SettingsScreen(onDone = {
                navController.navigate("list") { popUpTo("settings") { inclusive = true } }
            })
        }
        composable("list") {
            val vm: RecipeListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            RecipeListScreen(
                vm = vm,
                onAddRecipe = { navController.navigate("add") },
                onOpenRecipe = { id -> navController.navigate("detail/$id") },
                onOpenShoppingList = { navController.navigate("shopping") },
                onOpenMealPlan = { navController.navigate("mealplan") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("add") {
            val vm: AddRecipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            AddRecipeScreen(vm = vm, onDone = { id ->
                navController.popBackStack()
                navController.navigate("detail/$id")
            })
        }
        composable(
            "detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            val scope = rememberCoroutineScope()
            RecipeDetailScreen(repo = repo, recipeId = recipeId, onAddToCookList = { id ->
                scope.launch { repo.addToCookList(id) }
            })
        }
        composable("shopping") {
            val vm: ShoppingListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            ShoppingListScreen(vm = vm)
        }
        composable("mealplan") {
            val vm: MealPlanViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            MealPlanScreen(vm = vm)
        }
    }
}
