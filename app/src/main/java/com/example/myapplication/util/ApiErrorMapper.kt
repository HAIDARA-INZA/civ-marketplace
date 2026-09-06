package com.example.myapplication.util

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** Transforme les erreurs réseau/API en messages courts et compréhensibles. */
object ApiErrorMapper {
    fun toException(error: Throwable): Exception = Exception(messageFor(error))

    fun messageFor(error: Throwable): String = when (error) {
        is UnknownHostException, is ConnectException -> "Aucune connexion Internet. Vérifiez votre réseau puis réessayez."
        is SocketTimeoutException -> "La connexion est trop lente. Réessayez dans quelques instants."
        is SSLException -> "Connexion sécurisée impossible. Vérifiez la date de votre téléphone ou réessayez plus tard."
        is HttpException -> httpMessage(error)
        is IOException -> "Impossible de joindre le serveur. Vérifiez votre connexion puis réessayez."
        else -> "Une erreur inattendue est survenue. Réessayez dans quelques instants."
    }

    private fun httpMessage(error: HttpException): String {
        val apiMessage = runCatching {
            val json = JSONObject(error.response()?.errorBody()?.string().orEmpty())
            val fields = json.optJSONObject("errors")
            fields?.keys()?.asSequence()?.firstOrNull()?.let { key ->
                fields.optJSONArray(key)?.optString(0)
            } ?: json.optString("message")
        }.getOrNull()?.takeUnless { it.isNullOrBlank() || it.startsWith("validation.") }
        if (apiMessage != null) return apiMessage

        return when (error.code()) {
            400, 422 -> "Vérifiez les informations saisies puis réessayez."
            401 -> "Votre session a expiré. Connectez-vous de nouveau."
            403 -> "Vous n’êtes pas autorisé à effectuer cette action."
            404 -> "L’élément demandé est introuvable."
            408 -> "La demande a expiré. Réessayez."
            429 -> "Trop de tentatives. Patientez un instant avant de réessayer."
            in 500..599 -> "Le service est temporairement indisponible. Réessayez dans un instant."
            else -> "La demande n’a pas pu être traitée. Réessayez."
        }
    }
}
