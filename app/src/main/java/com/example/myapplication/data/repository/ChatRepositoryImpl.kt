package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.model.ChatMessageDto
import com.example.myapplication.data.model.ConversationDto
import com.example.myapplication.data.remote.ChatService
import com.example.myapplication.data.remote.SendMessageRequest
import com.example.myapplication.domain.repository.ChatRepository
import com.example.myapplication.util.ApiErrorMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatService: ChatService,
    @ApplicationContext private val context: Context
) : ChatRepository {

    override suspend fun getConversations(): Result<List<ConversationDto>> {
        return try {
            Result.success(chatService.getConversations())
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    override suspend fun getMessages(userId: Int): Result<List<ChatMessageDto>> {
        return try {
            Result.success(chatService.getMessages(userId))
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    override suspend fun sendMessage(receiverId: Int, message: String): Result<ChatMessageDto> {
        return try {
            val response = chatService.sendMessage(SendMessageRequest(receiverId, message))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    override suspend fun sendAttachment(
        receiverId: Int,
        message: String,
        attachmentUri: String,
        attachmentType: String
    ): Result<ChatMessageDto> {
        return try {
            val uri  = Uri.parse(attachmentUri)
            val file = getFileFromUri(uri) ?: return Result.failure(Exception("Impossible de lire le fichier"))

            val attachmentBody  = file.asRequestBody(attachmentType.toMediaTypeOrNull())
            val attachmentPart  = MultipartBody.Part.createFormData("attachment", file.name, attachmentBody)
            val receiverBody    = receiverId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val messageBody     = message.toRequestBody("text/plain".toMediaTypeOrNull())
            val attachmentTypeBody = attachmentType.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = chatService.sendMessageWithAttachment(
                receiverId     = receiverBody,
                message        = messageBody,
                attachmentType = attachmentTypeBody,
                attachment     = attachmentPart
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ApiErrorMapper.toException(e))
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val file = File(context.cacheDir, "upload_file_${System.currentTimeMillis()}")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun handleError(e: Exception): Exception {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                try {
                    val json = JSONObject(errorBody)
                    return Exception(json.getString("message"))
                } catch (_: Exception) {}
            }
        }
        return e
    }
}
