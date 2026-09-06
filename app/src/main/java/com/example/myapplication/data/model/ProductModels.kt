package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: String, 
    @SerializedName(value = "image_url", alternate = ["imageUrl", "image"])
    val imageUrl: String? = null,
    val stock: Int = 0,
    val category: String? = null,
    @SerializedName(value = "seller_id", alternate = ["sellerId"])
    val sellerId: Int? = null,
    @SerializedName(value = "seller_name", alternate = ["sellerName"])
    val sellerName: String? = null,
    val rating: Double? = null,
    @SerializedName(value = "views_count", alternate = ["viewsCount"])
    val viewsCount: Int? = null,
    @SerializedName(value = "is_favorite", alternate = ["isFavorite"])
    val isFavorite: Boolean = false
) {
    fun getDisplayImageUrl(): String? = imageUrl
    fun getActualSellerId(): Int? = sellerId
}

data class ProductResponse(
    val products: List<ProductDto>
)

data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String? = null
)

data class PromotionDto(
    val id: Int,
    val title: String,
    val subtitle: String? = null,
    @SerializedName(value = "image_url", alternate = ["imageUrl"])
    val imageUrl: String? = null,
    val category: String? = null,
    @SerializedName(value = "product_id", alternate = ["productId"])
    val productId: Int? = null,
    @SerializedName(value = "cta_label", alternate = ["ctaLabel"])
    val ctaLabel: String? = null
)

data class FavoriteResponse(
    @SerializedName(value = "is_favorite", alternate = ["isFavorite"])
    val isFavorite: Boolean
)
