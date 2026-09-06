package com.example.myapplication.data.remote

import com.example.myapplication.data.model.AuthResponse
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.VendorSummaryDto
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("email/verify")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthResponse

    /** Renvoyer l'OTP de vérification de compte (bouton "Renvoyer le code") */
    @POST("email/resend")
    suspend fun resendOtp(@Body request: Map<String, String>): Map<String, String>

    @POST("password/forgot")
    suspend fun forgotPassword(@Body request: Map<String, String>): Map<String, String>

    @POST("password/reset")
    suspend fun resetPassword(@Body request: Map<String, String>): Map<String, String>

    @GET("vendors")
    suspend fun getVendors(): List<VendorSummaryDto>
}

data class VerifyEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)
