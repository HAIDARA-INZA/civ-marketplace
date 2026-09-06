package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.ChatMessageDto
import com.example.myapplication.data.model.ConversationDto

interface ChatRepository {
    suspend fun getConversations(): Result<List<ConversationDto>>
    suspend fun getMessages(userId: Int): Result<List<ChatMessageDto>>
    suspend fun sendMessage(receiverId: Int, message: String): Result<ChatMessageDto>
    suspend fun sendAttachment(receiverId: Int, message: String, attachmentUri: String, attachmentType: String): Result<ChatMessageDto>
}
