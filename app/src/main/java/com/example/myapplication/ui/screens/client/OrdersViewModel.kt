package com.example.myapplication.ui.screens.client

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.OrderDto
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.OrderRepository
import com.example.myapplication.util.PusherManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val pusherManager: PusherManager
) : ViewModel() {

    private val _state = mutableStateOf<OrdersState>(OrdersState.Loading)
    val state: State<OrdersState> = _state

    private var currentUserId: Int? = null

    init {
        viewModelScope.launch {
            currentUserId = authRepository.getUserId()
            setupRealtime()
            loadOrders()
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _state.value = OrdersState.Loading
            orderRepository.getOrders()
                .onSuccess { _state.value = OrdersState.Success(it) }
                .onFailure { e ->
                    val errorMessage = if (e.message?.contains("401") == true || e.message?.contains("Unauthenticated") == true) {
                        "Votre session a expiré. Veuillez vous reconnecter."
                    } else {
                        e.message ?: "Erreur de chargement des commandes"
                    }
                    _state.value = OrdersState.Error(errorMessage)
                }
        }
    }

    private fun setupRealtime() {
        pusherManager.init()
        // Utilise le canal privé par user_id pour ne recevoir que SES commandes
        val userId = currentUserId ?: return
        pusherManager.subscribeToUserChannel(userId, "new-order") { data ->
            loadOrders()
        }
        pusherManager.subscribeToUserChannel(userId, "order-updated") { data ->
            loadOrders()
        }
    }

    private fun refreshIfOrderBelongsToUser(data: String) {
        val userId = currentUserId ?: return
        val json = runCatching { JSONObject(data) }.getOrNull() ?: return
        val clientId = json.optInt("client_id", 0)
        if (clientId == 0 || clientId == userId) {
            loadOrders()
        }
    }
}

sealed class OrdersState {
    object Loading : OrdersState()
    data class Success(val orders: List<OrderDto>) : OrdersState()
    data class Error(val message: String) : OrdersState()
}
