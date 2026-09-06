package com.example.myapplication.ui.screens.seller

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ProductDto
import com.example.myapplication.data.model.SellerStatsDto
import com.example.myapplication.domain.repository.OrderRepository
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.util.PusherManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val pusherManager: PusherManager
) : ViewModel() {

    private val _state = mutableStateOf<SellerState>(SellerState.Loading)
    val state: State<SellerState> = _state

    private val _uploadState = mutableStateOf<UploadState>(UploadState.Idle)
    val uploadState: State<UploadState> = _uploadState

    private var latestStats: SellerStatsDto? = null
    private var latestProducts: List<ProductDto> = emptyList()

    init {
        loadDashboard()
        setupPusher()
    }

    private fun setupPusher() {
        pusherManager.init()
        pusherManager.subscribeToChannel("products-channel", "product-added") { loadDashboard() }
        pusherManager.subscribeToChannel("products-channel", "product-updated") { loadDashboard() }
    }

    /** Appelé une fois que le profil vendeur est chargé et que l'ID est connu */
    fun subscribeToPrivateOrdersChannel(sellerId: Int) {
        pusherManager.subscribeToPrivateChannel(sellerId, "notification-created") {
            loadDashboard()
        }
    }

    /**
     * Charge stats + produits EN PARALLÈLE, puis met à jour l'état UNE SEULE FOIS
     * avec les deux résultats → plus de race condition / écran flou.
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _state.value = SellerState.Loading
            val statsResult   = kotlin.runCatching { orderRepository.getSellerStats().getOrThrow() }
            val productsResult = kotlin.runCatching { productRepository.getSellerProducts().getOrThrow() }

            val stats    = statsResult.getOrNull()
            val products = productsResult.getOrNull() ?: emptyList()

            if (stats != null) {
                latestStats    = stats
                latestProducts = products
                _state.value   = SellerState.Success(stats, products)
            } else {
                _state.value = SellerState.Error(
                    statsResult.exceptionOrNull()?.message ?: "Impossible de charger le tableau de bord"
                )
            }
        }
    }

    fun loadStats()    { loadDashboard() }
    fun loadMyProducts() { loadDashboard() }

    fun addProduct(
        name: String,
        description: String,
        price: String,
        stock: String,
        category: String,
        imageUri: String
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val cleanName = name.trim()
            val cleanDescription = description.trim()
            val cleanCategory = category.trim()
            val priceDouble = parseProductPrice(price)
            val stockInt = stock.trim().toIntOrNull()?.coerceAtLeast(0) ?: 0

            when {
                cleanName.isBlank() -> {
                    _uploadState.value = UploadState.Error("Le nom du produit est obligatoire")
                    return@launch
                }
                cleanCategory.isBlank() -> {
                    _uploadState.value = UploadState.Error("La catégorie est obligatoire")
                    return@launch
                }
                cleanDescription.isBlank() -> {
                    _uploadState.value = UploadState.Error("La description est obligatoire")
                    return@launch
                }
                priceDouble == null || priceDouble <= 0.0 -> {
                    _uploadState.value = UploadState.Error("Le prix doit être supérieur à 0")
                    return@launch
                }
                imageUri.isBlank() -> {
                    _uploadState.value = UploadState.Error("Ajoutez une image du produit")
                    return@launch
                }
            }

            productRepository.createProduct(
                name = cleanName,
                description = cleanDescription,
                price = priceDouble!!,
                stock = stockInt,
                category = cleanCategory,
                imageUri = imageUri
            ).onSuccess {
                _uploadState.value = UploadState.Success
                loadStats()
                loadMyProducts()
            }.onFailure { e ->
                _uploadState.value = UploadState.Error(e.message ?: "Erreur d'envoi")
            }
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
                .onSuccess { loadStats() }
                .onFailure { e ->
                    _state.value = SellerState.Error(e.message ?: "Statut de commande non mis à jour")
                }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
                .onSuccess { loadDashboard() }
                .onFailure { _state.value = SellerState.Error(it.message ?: "Suppression impossible") }
        }
    }

    fun updateProduct(product: ProductDto, name: String, description: String, price: String, stock: String, category: String) {
        viewModelScope.launch {
            val parsedPrice = parseProductPrice(price)
            val parsedStock = stock.toIntOrNull()
            if (name.isBlank() || description.isBlank() || category.isBlank() || parsedPrice == null || parsedPrice <= 0 || parsedStock == null || parsedStock < 0) {
                _state.value = SellerState.Error("Vérifiez le nom, la description, le prix, le stock et la catégorie")
                return@launch
            }
            productRepository.updateProduct(product.id, name.trim(), description.trim(), parsedPrice, parsedStock, category.trim())
                .onSuccess { loadDashboard() }
                .onFailure { _state.value = SellerState.Error(it.message ?: "Modification impossible") }
        }
    }

    private fun parseProductPrice(value: String): Double? {
        return value
            .replace("FCFA", "", ignoreCase = true)
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace(",", ".")
            .toDoubleOrNull()
    }
}

sealed class SellerState {
    object Loading : SellerState()
    data class Success(
        val stats: SellerStatsDto,
        val myProducts: List<ProductDto> = emptyList()
    ) : SellerState()
    data class Error(val message: String) : SellerState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}
