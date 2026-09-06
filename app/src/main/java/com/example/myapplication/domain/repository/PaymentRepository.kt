package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.CheckoutRequest
import com.example.myapplication.data.model.CheckoutResponse

interface PaymentRepository {
    suspend fun createCheckout(request: CheckoutRequest): Result<CheckoutResponse>
}
