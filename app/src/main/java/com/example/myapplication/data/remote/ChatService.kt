package com.example.myapplication.data.remote

import com.example.myapplication.data.model.ChatMessageDto
import com.example.myapplication.data.model.ConversationDto
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatService {

    /** Liste des conversations de l'utilisateur */
    @GET("conversations")
    suspend fun getConversations(): List<ConversationDto>

    /** Messages d'une conversation — route réelle du backend */
    @GET("conversations/{userId}")
    suspend fun getMessages(@Path("userId") userId: Int): List<ChatMessageDto>

    /** Envoyer un message texte */
    @POST("messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): ChatMessageDto

    /** Envoyer un message avec pièce jointe */
    @Multipart
    @POST("messages/send")
    suspend fun sendMessageWithAttachment(
        @Part("receiver_id") receiverId: RequestBody,
        @Part("message") message: RequestBody,
        @Part("attachment_type") attachmentType: RequestBody,
        @Part attachment: MultipartBody.Part
    ): ChatMessageDto
}

data class SendMessageRequest(
    @SerializedName("receiver_id")
    val receiverId: Int,
    val message: String
)
