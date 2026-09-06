package com.example.myapplication.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ProductDto
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.util.RoleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf<ProductDetailState>(ProductDetailState.Loading)
    val state: State<ProductDetailState> = _state

    private val _canBuy = mutableStateOf(true)
    val canBuy: State<Boolean> = _canBuy

    init {
        viewModelScope.launch {
            _canBuy.value = RoleUtils.canBuy(authRepository.getRole() ?: "client")
        }
    }

    fun loadProduct(id: Int) {
        viewModelScope.launch {
            _state.value = ProductDetailState.Loading
            productRepository.getProductDetails(id)
                .onSuccess { _state.value = ProductDetailState.Success(it) }
                .onFailure { _state.value = ProductDetailState.Error(it.message ?: "Une erreur est survenue") }
        }
    }

    fun addToCart(product: ProductDto) {
        viewModelScope.launch {
            productRepository.addToCart(product)
        }
    }

    fun setFavorite(product: ProductDto, favorite: Boolean) {
        viewModelScope.launch {
            productRepository.setFavorite(product.id, favorite)
                .onSuccess {
                    _state.value = ProductDetailState.Success(product.copy(isFavorite = favorite))
                }
                .onFailure { e ->
                    _state.value = ProductDetailState.Error(e.message ?: "Favori non mis a jour")
                }
        }
    }
}

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: ProductDto) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}
