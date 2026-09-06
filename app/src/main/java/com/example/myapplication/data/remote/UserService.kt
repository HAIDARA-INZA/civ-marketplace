package com.example.myapplication.data.remote

import com.example.myapplication.data.model.NotificationDto
import com.example.myapplication.data.model.ProfileDto
import com.example.myapplication.data.model.UpdateProfileRequest
import com.example.myapplication.data.model.UpdateUserSettingsRequest
import com.example.myapplication.data.model.UserSettingsDto
import com.example.myapplication.data.model.UpdateFcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("me")
    suspend fun getProfile(): ProfileDto

    @PUT("me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileDto

    @PATCH("me/fcm-token")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest)

    @GET("settings")
    suspend fun getSettings(): UserSettingsDto

    @PUT("settings")
    suspend fun updateSettings(@Body request: UpdateUserSettingsRequest): UserSettingsDto

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): NotificationDto

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead(): Map<String, String>
}
