package com.example.myapplication.data.remote

import com.example.myapplication.data.model.OrderDto
import com.example.myapplication.data.model.SellerStatsDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderService {
    @GET("orders")
    suspend fun getOrders(): List<OrderDto>

    @PATCH("orders/{id}")
    suspend fun updateOrderStatus(@Path("id") id: Int, @Query("status") status: String): OrderDto

    @GET("seller/stats")
    suspend fun getSellerStats(): SellerStatsDto
}
