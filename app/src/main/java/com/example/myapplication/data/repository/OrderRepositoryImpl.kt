package com.example.myapplication.data.repository

import com.example.myapplication.data.model.OrderDto
import com.example.myapplication.data.model.SellerStatsDto
import com.example.myapplication.data.remote.OrderService
import com.example.myapplication.domain.repository.OrderRepository
import com.example.myapplication.util.ApiErrorMapper
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderService: OrderService
) : OrderRepository {

    override suspend fun getOrders(): Result<List<OrderDto>> {
        return try {
            Result.success(orderService.getOrders())
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    override suspend fun updateOrderStatus(id: Int, status: String): Result<OrderDto> {
        return try {
            Result.success(orderService.updateOrderStatus(id, status))
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    override suspend fun getSellerStats(): Result<SellerStatsDto> {
        return try {
            Result.success(orderService.getSellerStats())
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    private fun handleError(e: Exception): Exception {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                try {
                    val jsonObject = JSONObject(errorBody)
                    return Exception(jsonObject.optString("message", "Erreur serveur"))
                } catch (_: Exception) {
                }
            }
        }
        return Exception(e.message ?: "Erreur réseau")
    }
}
