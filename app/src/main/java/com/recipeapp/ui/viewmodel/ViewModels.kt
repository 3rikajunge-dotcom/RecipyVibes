package com.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeapp.data.model.Recipe
import com.recipeapp.data.model.ShoppingListLine
import com.recipeapp.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeListViewModel(private val repo: RecipeRepository) : ViewModel() {
    val query = MutableStateFlow("")

    val recipes: StateFlow<List<Recipe>> = query
        .debounce(250)
        .flatMapLatest { q -> if (q.isBlank()) repo.observeAllRecipes() else repo.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToCookList(recipeId: Long) = viewModelScope.launch { repo.addToCookList(recipeId) }
}

class AddRecipeViewModel(private val repo: RecipeRepository) : ViewModel() {
    private val _state = MutableStateFlow<AddState>(AddState.Idle)
    val state: StateFlow<AddState> = _state

    fun addFromUrl(url: String) {
        _state.value = AddState.Loading
        viewModelScope.launch {
            try {
                val recipe = repo.addRecipeFromUrl(url)
                _state.value = AddState.Success(recipe)
            } catch (e: Exception) {
                _state.value = AddState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    sealed interface AddState {
        object Idle : AddState
        object Loading : AddState
        data class Success(val recipe: Recipe) : AddState
        data class Error(val message: String) : AddState
    }
}

class ShoppingListViewModel(private val repo: RecipeRepository) : ViewModel() {
    val cookList: StateFlow<List<Recipe>> =
        repo.observeCookList().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _shoppingList = MutableStateFlow<List<ShoppingListLine>>(emptyList())
    val shoppingList: StateFlow<List<ShoppingListLine>> = _shoppingList

    fun removeFromCookList(recipeId: Long) = viewModelScope.launch {
        repo.removeFromCookList(recipeId)
        generate()
    }

    fun generate() = viewModelScope.launch {
        _shoppingList.value = repo.buildShoppingList()
    }
}

class MealPlanViewModel(private val repo: RecipeRepository) : ViewModel() {
    private val _suggestion = MutableStateFlow<String?>(null)
    val suggestion: StateFlow<String?> = _suggestion

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    /** dayComplexities e.g. {"Monday" to "quick", "Tuesday" to "elaborate", ...} */
    fun requestSuggestions(dayComplexities: Map<String, String>) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _suggestion.value = repo.suggestWeeklyMeals(dayComplexities)
            } catch (e: Exception) {
                _suggestion.value = "Couldn't get suggestions: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
