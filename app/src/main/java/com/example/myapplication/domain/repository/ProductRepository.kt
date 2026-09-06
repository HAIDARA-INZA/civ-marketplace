package com.example.myapplication.domain.repository

import com.example.myapplication.data.local.CartEntity
import com.example.myapplication.data.model.CategoryDto
import com.example.myapplication.data.model.ProductDto
import com.example.myapplication.data.model.PromotionDto
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(query: String? = null): Result<List<ProductDto>>
    suspend fun getProductDetails(id: Int): Result<ProductDto>
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun getActivePromotions(): Result<List<PromotionDto>>
    suspend fun getFavoriteProducts(): Result<List<ProductDto>>
    suspend fun setFavorite(productId: Int, favorite: Boolean): Result<Boolean>
    suspend fun getSellerProducts(): Result<List<ProductDto>>
    
    suspend fun createProduct(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        imageUri: String?
    ): Result<ProductDto>
    suspend fun updateProduct(
        id: Int, name: String, description: String, price: Double, stock: Int,
        category: String, imageUri: String? = null
    ): Result<ProductDto>
    suspend fun deleteProduct(id: Int): Result<Unit>
    
    // Cart operations
    fun getCartItems(): Flow<List<CartEntity>>
    suspend fun addToCart(product: ProductDto)
    suspend fun removeFromCart(item: CartEntity)
}
