package com.example.myapplication.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ChatMessageDto
import com.example.myapplication.data.model.ConversationDto
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.ChatRepository
import com.example.myapplication.util.PusherManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val pusherManager: PusherManager
) : ViewModel() {

    private val _conversationsState = mutableStateOf<ChatState<List<ConversationDto>>>(ChatState.Loading)
    val conversationsState: State<ChatState<List<ConversationDto>>> = _conversationsState

    private val _messagesState = mutableStateOf<ChatState<List<ChatMessageDto>>>(ChatState.Idle)
    val messagesState: State<ChatState<List<ChatMessageDto>>> = _messagesState

    // Stubs pour les fonctionnalités à implanter côté backend plus tard
    private val _typingState = mutableStateOf(false)
    val typingState: State<Boolean> = _typingState

    private val _hasMoreState = mutableStateOf(false)
    val hasMoreState: State<Boolean> = _hasMoreState

    // PresenceDto allway null — endpoint /presence/{userId} non encore disponible
    private val _presenceState = mutableStateOf<com.example.myapplication.data.model.PresenceDto?>(null)
    val presenceState: State<com.example.myapplication.data.model.PresenceDto?> = _presenceState

    private val _currentUserIdState = mutableStateOf<Int?>(null)
    val currentUserIdState: State<Int?> = _currentUserIdState

    private var openConversationUserId: Int? = null

    init {
        viewModelScope.launch {
            _currentUserIdState.value = authRepository.getUserId()
            setupPusher()
            loadConversations()
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.value = ChatState.Loading
            chatRepository.getConversations()
                .onSuccess { _conversationsState.value = ChatState.Success(it) }
                .onFailure { _conversationsState.value = ChatState.Error(it.message ?: "Erreur") }
        }
    }

    fun loadMessages(userId: Int) {
        openConversationUserId = userId
        viewModelScope.launch {
            _messagesState.value = ChatState.Loading
            chatRepository.getMessages(userId)
                .onSuccess { _messagesState.value = ChatState.Success(it) }
                .onFailure { _messagesState.value = ChatState.Error(it.message ?: "Erreur") }
        }
    }

    fun sendMessage(receiverId: Int, message: String) {
        val cleanMessage = message.trim()
        if (receiverId <= 0 || cleanMessage.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(receiverId, cleanMessage)
                .onSuccess {
                    loadMessages(receiverId)
                    loadConversations()
                }
                .onFailure { _messagesState.value = ChatState.Error(it.message ?: "Message non envoyé") }
        }
    }

    fun sendAttachment(receiverId: Int, message: String, attachmentUri: String, attachmentType: String) {
        if (receiverId <= 0 || attachmentUri.isBlank() || attachmentType.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendAttachment(receiverId, message.trim(), attachmentUri, attachmentType)
                .onSuccess {
                    loadMessages(receiverId)
                    loadConversations()
                }
                .onFailure { _messagesState.value = ChatState.Error(it.message ?: "Envoi du fichier impossible") }
        }
    }

    /** No-op : endpoint non disponible côté backend pour l'instant */
    fun sendTyping(receiverId: Int, isTyping: Boolean) { /* TODO: ajouter la route backend */ }
    fun updateMessage(messageId: Int, message: String) { /* TODO: ajouter la route backend */ }
    fun deleteMessage(messageId: Int) { /* TODO: ajouter la route backend */ }
    fun loadMoreMessages() { /* TODO: pagination backend */ }
    fun loadPresence(userId: Int) { /* TODO: GET /presence/{userId} non encore disponible */ }

    private fun setupPusher() {
        pusherManager.init()
        pusherManager.subscribeToChannel("messages-channel", "message-sent") { data ->
            val json       = runCatching { JSONObject(data) }.getOrNull() ?: return@subscribeToChannel
            val senderId   = json.optInt("sender_id", 0)
            val receiverId = json.optInt("receiver_id", 0)
            val userId     = _currentUserIdState.value ?: return@subscribeToChannel

            if (senderId == userId || receiverId == userId) {
                loadConversations()
                val openUser = openConversationUserId
                if (openUser != null && (senderId == openUser || receiverId == openUser)) {
                    loadMessages(openUser)
                }
            }
        }
    }
}

sealed class ChatState<out T> {
    object Idle    : ChatState<Nothing>()
    object Loading : ChatState<Nothing>()
    data class Success<T>(val data: T) : ChatState<T>()
    data class Error(val message: String) : ChatState<Nothing>()
}
