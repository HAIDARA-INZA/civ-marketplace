package com.example.myapplication.util

import android.util.Log
import com.example.myapplication.data.local.TokenManager
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.channel.SubscriptionEventListener
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PusherManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private var pusher: Pusher? = null

    fun init() {
        if (pusher != null || Constants.PUSHER_KEY.isBlank()) return

        try {
            val token = runBlocking { tokenManager.getToken().first() }

            val options = PusherOptions().apply {
                setCluster(Constants.PUSHER_CLUSTER)

                // ✅ Authentification des canaux privés via le backend Laravel
                if (!token.isNullOrBlank()) {
                    val authorizer = HttpAuthorizer(
                        "${Constants.BASE_API_URL.trimEnd('/')}/broadcasting/auth"
                    )
                    authorizer.setHeaders(mapOf("Authorization" to "Bearer $token"))
                    setAuthorizer(authorizer)
                }
            }

            pusher = Pusher(Constants.PUSHER_KEY, options)
            pusher?.connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(change: ConnectionStateChange?) {
                    Log.d("PusherManager", "État: ${change?.previousState} → ${change?.currentState}")
                }
                override fun onError(msg: String?, code: String?, e: Exception?) {
                    Log.w("PusherManager", "Erreur connexion: $msg (code=$code)")
                }
            }, ConnectionState.ALL)

        } catch (e: Exception) {
            Log.w("PusherManager", "Impossible d'initialiser Pusher", e)
            pusher = null
        }
    }

    /**
     * Canal PUBLIC — données non sensibles (nouveaux produits, nouveaux vendeurs…).
     * Tout le monde reçoit ces événements.
     */
    fun subscribeToChannel(channelName: String, eventName: String, onEvent: (String) -> Unit) {
        try {
            val channel: Channel? = pusher?.subscribe(channelName)
            channel?.bind(eventName, object : SubscriptionEventListener {
                override fun onEvent(event: PusherEvent?) {
                    event?.data?.let { onEvent(it) }
                }
            })
        } catch (e: Exception) {
            Log.w("PusherManager", "Impossible de s'abonner à $channelName/$eventName", e)
        }
    }

    /**
     * Canal PRIVÉ par utilisateur : "private-user-{userId}".
     * ✅ Seul l'utilisateur authentifié reçoit ses propres notifications/commandes.
     * Correspond à MarketplaceNotifier Laravel qui diffuse sur "private-user-{userId}".
     */
    fun subscribeToPrivateChannel(userId: Int, eventName: String, onEvent: (String) -> Unit) {
        try {
            val channelName = "user-$userId" // Pusher ajoute "private-" automatiquement
            val channel = pusher?.subscribePrivate(channelName)
            channel?.bind(eventName, object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    event?.data?.let { onEvent(it) }
                }
                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    Log.e("PusherManager", "Auth canal privé échouée: $message", e)
                }
                override fun onSubscriptionSucceeded(channelName: String?) {
                    Log.d("PusherManager", "Abonné canal privé: $channelName")
                }
            })
        } catch (e: Exception) {
            Log.w("PusherManager", "Impossible de s'abonner au canal privé user-$userId", e)
        }
    }

    /** Alias pour la compatibilité avec UserViewModel */
    fun subscribeToUserChannel(userId: Int, eventName: String, onEvent: (String) -> Unit) {
        subscribeToPrivateChannel(userId, eventName, onEvent)
    }

    fun disconnect() {
        pusher?.disconnect()
        pusher = null
    }
}
