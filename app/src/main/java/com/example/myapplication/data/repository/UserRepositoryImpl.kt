package com.example.myapplication.data.repository

import com.example.myapplication.data.model.NotificationDto
import com.example.myapplication.data.model.ProfileDto
import com.example.myapplication.data.model.UpdateProfileRequest
import com.example.myapplication.data.model.UpdateUserSettingsRequest
import com.example.myapplication.data.model.UpdateFcmTokenRequest
import com.example.myapplication.data.model.UserSettingsDto
import com.example.myapplication.data.remote.UserService
import com.example.myapplication.domain.repository.UserRepository
import com.example.myapplication.util.ApiErrorMapper
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userService: UserService
) : UserRepository {

    override suspend fun getProfile(): Result<ProfileDto> {
        return runCatching { userService.getProfile() }.mapError()
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<ProfileDto> {
        return runCatching { userService.updateProfile(request) }.mapError()
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> {
        return runCatching {
            userService.updateFcmToken(UpdateFcmTokenRequest(token))
            Unit
        }.mapError()
    }

    override suspend fun getSettings(): Result<UserSettingsDto> {
        return runCatching { userService.getSettings() }.mapError()
    }

    override suspend fun updateSettings(request: UpdateUserSettingsRequest): Result<UserSettingsDto> {
        return runCatching { userService.updateSettings(request) }.mapError()
    }

    override suspend fun getNotifications(): Result<List<NotificationDto>> {
        return runCatching { userService.getNotifications() }.mapError()
    }

    override suspend fun markNotificationRead(id: Int): Result<NotificationDto> {
        return runCatching { userService.markNotificationRead(id) }.mapError()
    }

    override suspend fun markAllNotificationsRead(): Result<Unit> {
        return runCatching {
            userService.markAllNotificationsRead()
            Unit
        }.mapError()
    }

    private fun <T> Result<T>.mapError(): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(ApiErrorMapper.toException(it)) }
        )
    }

    private fun handleError(error: Throwable): Exception {
        if (error is HttpException) {
            if (error.code() >= 500) {
                return Exception("Le service est temporairement indisponible. Réessayez dans un instant.")
            }
            val errorBody = error.response()?.errorBody()?.string()
            if (errorBody != null) {
                try {
                    val jsonObject = JSONObject(errorBody)
                    val message = jsonObject.optString("message")
                    val errors = jsonObject.optJSONObject("errors")
                    val fieldMessage = errors?.keys()?.asSequence()?.firstOrNull()?.let { key ->
                        errors.optJSONArray(key)?.optString(0)
                    }
                    return Exception(fieldMessage?.takeUnless { it.startsWith("validation.") } ?: message.ifBlank { "La demande n'a pas pu être traitée." })
                } catch (_: Exception) {
                }
            }
        }
        return Exception(error.message ?: "Erreur reseau")
    }
}
