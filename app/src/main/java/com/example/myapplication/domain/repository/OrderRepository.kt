package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.OrderDto
import com.example.myapplication.data.model.SellerStatsDto

interface OrderRepository {
    suspend fun getOrders(): Result<List<OrderDto>>
    suspend fun updateOrderStatus(id: Int, status: String): Result<OrderDto>
    suspend fun getSellerStats(): Result<SellerStatsDto>
}
