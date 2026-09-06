package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.NotificationDto
import com.example.myapplication.data.model.ProfileDto
import com.example.myapplication.data.model.UpdateProfileRequest
import com.example.myapplication.data.model.UpdateUserSettingsRequest
import com.example.myapplication.data.model.UserSettingsDto

interface UserRepository {
    suspend fun getProfile(): Result<ProfileDto>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<ProfileDto>
    suspend fun updateFcmToken(token: String): Result<Unit>
    suspend fun getSettings(): Result<UserSettingsDto>
    suspend fun updateSettings(request: UpdateUserSettingsRequest): Result<UserSettingsDto>
    suspend fun getNotifications(): Result<List<NotificationDto>>
    suspend fun markNotificationRead(id: Int): Result<NotificationDto>
    suspend fun markAllNotificationsRead(): Result<Unit>
}
