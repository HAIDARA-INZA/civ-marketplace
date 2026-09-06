package com.example.myapplication.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.CategoryDto
import com.example.myapplication.data.model.ProductDto
import com.example.myapplication.data.model.PromotionDto
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.util.PusherManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val pusherManager: PusherManager
) : ViewModel() {

    private val _homeState = mutableStateOf<HomeState>(HomeState.Loading)
    val homeState: State<HomeState> = _homeState

    private val _categoriesState = mutableStateOf<HomeCategoriesState>(HomeCategoriesState.Loading)
    val categoriesState: State<HomeCategoriesState> = _categoriesState

    private val _promotionsState = mutableStateOf<PromotionsState>(PromotionsState.Loading)
    val promotionsState: State<PromotionsState> = _promotionsState

    val cartCount: StateFlow<Int> = productRepository.getCartItems()
        .map { items -> items.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadProducts()
        loadCategories()
        loadPromotions()
        setupPusher()
    }

    fun loadProducts(query: String? = null) {
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            productRepository.getProducts(query)
                .onSuccess { _homeState.value = HomeState.Success(it) }
                .onFailure { _homeState.value = HomeState.Error(it.message ?: "Impossible de charger les produits") }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = HomeCategoriesState.Loading
            productRepository.getCategories()
                .onSuccess { _categoriesState.value = HomeCategoriesState.Success(it) }
                .onFailure { _categoriesState.value = HomeCategoriesState.Error(it.message ?: "Impossible de charger les catégories") }
        }
    }

    fun loadPromotions() {
        viewModelScope.launch {
            _promotionsState.value = PromotionsState.Loading
            productRepository.getActivePromotions()
                .onSuccess { _promotionsState.value = PromotionsState.Success(it) }
                .onFailure { _promotionsState.value = PromotionsState.Error(it.message ?: "Impossible de charger les promotions") }
        }
    }

    fun setFavorite(product: ProductDto, favorite: Boolean) {
        viewModelScope.launch {
            productRepository.setFavorite(product.id, favorite)
                .onSuccess {
                    val current = (_homeState.value as? HomeState.Success)?.products ?: return@onSuccess
                    _homeState.value = HomeState.Success(
                        current.map { item ->
                            if (item.id == product.id) item.copy(isFavorite = favorite) else item
                        }
                    )
                }
                .onFailure { e ->
                    _homeState.value = HomeState.Error(e.message ?: "Favori non mis à jour")
                }
        }
    }

    private fun setupPusher() {
        pusherManager.init()
        pusherManager.subscribeToChannel("products-channel", "product-added") { loadProducts() }
        pusherManager.subscribeToChannel("products-channel", "product-updated") { loadProducts() }
        pusherManager.subscribeToChannel("products-channel", "product-deleted") { loadProducts() }
        pusherManager.subscribeToChannel("vendors-channel", "vendor-created") { loadProducts() }
        pusherManager.subscribeToChannel("promotions-channel", "promotion-updated") { loadPromotions() }
    }
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val products: List<ProductDto>) : HomeState()
    data class Error(val message: String) : HomeState()
}

sealed class HomeCategoriesState {
    object Loading : HomeCategoriesState()
    data class Success(val categories: List<CategoryDto>) : HomeCategoriesState()
    data class Error(val message: String) : HomeCategoriesState()
}

sealed class PromotionsState {
    object Loading : PromotionsState()
    data class Success(val promotions: List<PromotionDto>) : PromotionsState()
    data class Error(val message: String) : PromotionsState()
}
