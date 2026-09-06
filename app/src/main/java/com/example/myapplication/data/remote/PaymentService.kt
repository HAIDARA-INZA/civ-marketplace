package com.example.myapplication.data.remote

import com.example.myapplication.data.model.CheckoutRequest
import com.example.myapplication.data.model.CheckoutResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentService {
    @POST("checkout")
    suspend fun createCheckout(@Body request: CheckoutRequest): CheckoutResponse
}
