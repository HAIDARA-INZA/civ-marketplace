package com.example.myapplication.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.CategoryDto
import com.example.myapplication.data.model.VendorSummaryDto
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.domain.repository.UserRepository
import com.example.myapplication.util.RoleUtils
import com.example.myapplication.util.PusherManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val pusherManager: PusherManager
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    private val _vendorsState = mutableStateOf<VendorsState>(VendorsState.Idle)
    val vendorsState: State<VendorsState> = _vendorsState

    private val _categoriesState = mutableStateOf<CategoriesState>(CategoriesState.Idle)
    val categoriesState: State<CategoriesState> = _categoriesState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val cleanEmail = email.trim().lowercase()
            val result = authRepository.login(LoginRequest(cleanEmail, password))
            result.onSuccess { response ->
                val isVerified = response.user.emailVerifiedAt != null
                authRepository.saveAuthData(
                    userId = response.user.id,
                    token = response.token,
                    role = response.user.role,
                    name = response.user.name,
                    email = response.user.email,
                    isVerified = isVerified
                )
                registerFcmToken()
                _authState.value = if (isVerified) AuthState.Success else AuthState.NeedVerification
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        phone: String,
        city: String,
        commune: String,
        quarter: String,
        address: String,
        sellerSelectionMode: String,
        preferredSellerIds: List<Int>,
        vendorCategories: List<String>
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val cleanName = name.trim()
            val cleanEmail = email.trim().lowercase()
            val cleanPhone = phone.trim()
            val cleanCity = city.trim()
            val cleanCommune = commune.trim()
            val cleanQuarter = quarter.trim()
            val cleanAddress = address.trim()

            when {
                cleanName.isBlank() -> {
                    _authState.value = AuthState.Error("Le nom complet est obligatoire")
                    return@launch
                }
                cleanEmail.isBlank() -> {
                    _authState.value = AuthState.Error("L'e-mail est obligatoire")
                    return@launch
                }
                password.length < 6 -> {
                    _authState.value = AuthState.Error("Le mot de passe doit contenir au moins 6 caractères")
                    return@launch
                }
                cleanCity.isBlank() || cleanCommune.isBlank() || cleanQuarter.isBlank() -> {
                    _authState.value = AuthState.Error("Renseignez votre ville, commune et quartier")
                    return@launch
                }
                RoleUtils.canBuy(role) && sellerSelectionMode == "specific" && preferredSellerIds.isEmpty() -> {
                    _authState.value = AuthState.Error("Choisissez au moins un vendeur ou prenez tous les vendeurs")
                    return@launch
                }
                RoleUtils.canSell(role) && vendorCategories.isEmpty() -> {
                    _authState.value = AuthState.Error("Choisissez au moins une catégorie de vente")
                    return@launch
                }
            }

            val request = RegisterRequest(
                name = cleanName,
                email = cleanEmail,
                password = password,
                passwordConfirmation = password,
                role = role,
                phone = cleanPhone.ifBlank { null },
                city = cleanCity,
                commune = cleanCommune,
                quarter = cleanQuarter,
                address = cleanAddress.ifBlank { null },
                sellerSelectionMode = if (RoleUtils.canBuy(role)) sellerSelectionMode else null,
                preferredSellerIds = if (RoleUtils.canBuy(role) && sellerSelectionMode == "specific") preferredSellerIds else emptyList(),
                vendorCategories = if (RoleUtils.canSell(role)) vendorCategories else emptyList()
            )
            val result = authRepository.register(request)
            result.onSuccess { response ->
                authRepository.saveAuthData(
                    userId = response.user.id,
                    token = response.token,
                    role = response.user.role,
                    name = response.user.name,
                    email = response.user.email,
                    isVerified = false
                )
                registerFcmToken()
                _authState.value = AuthState.NeedVerification
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun loadVendors() {
        if (_vendorsState.value is VendorsState.Loading || _vendorsState.value is VendorsState.Success) return

        viewModelScope.launch {
            _vendorsState.value = VendorsState.Loading
            val result = authRepository.getVendors()
            result.onSuccess { vendors ->
                _vendorsState.value = VendorsState.Success(vendors)
            }.onFailure { e ->
                _vendorsState.value = VendorsState.Error(e.message ?: "Impossible de charger les vendeurs")
            }
        }
    }

    fun loadCategories() {
        if (_categoriesState.value is CategoriesState.Loading || _categoriesState.value is CategoriesState.Success) return

        viewModelScope.launch {
            _categoriesState.value = CategoriesState.Loading
            val result = productRepository.getCategories()
            result.onSuccess { categories ->
                _categoriesState.value = CategoriesState.Success(categories)
            }.onFailure { e ->
                _categoriesState.value = CategoriesState.Error(e.message ?: "Impossible de charger les catégories")
            }
        }
    }

    fun verifyEmail(email: String, otp: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.verifyEmail(email, otp)
            result.onSuccess { response ->
                authRepository.saveAuthData(
                    userId = response.user.id,
                    token = response.token,
                    role = response.user.role,
                    name = response.user.name,
                    email = response.user.email,
                    isVerified = true
                )
                registerFcmToken()
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Code invalide")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.forgotPassword(email.trim().lowercase())
            result.onSuccess {
                _authState.value = AuthState.OtpSent
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Erreur lors de l'envoi du code")
            }
        }
    }

    fun resetPassword(email: String, otp: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email.trim().lowercase(), otp, password)
            result.onSuccess {
                _authState.value = AuthState.ResetSuccess
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Échec de la réinitialisation")
            }
        }
    }

    /** ✅ Renvoie l'OTP de vérification de compte → POST /email/resend */
    fun resendOtp(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resendOtp(email.trim().lowercase())
            result.onSuccess {
                _authState.value = AuthState.OtpSent
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Impossible de renvoyer le code")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            pusherManager.disconnect()
            authRepository.logout()
        }
    }

    /** Enregistre aussi le token déjà existant, pas seulement ses futurs renouvellements. */
    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNotBlank()) {
                    viewModelScope.launch { userRepository.updateFcmToken(token) }
                }
            }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object NeedVerification : AuthState()
    object OtpSent : AuthState()
    object ResetSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class VendorsState {
    object Idle : VendorsState()
    object Loading : VendorsState()
    data class Success(val vendors: List<VendorSummaryDto>) : VendorsState()
    data class Error(val message: String) : VendorsState()
}

sealed class CategoriesState {
    object Idle : CategoriesState()
    object Loading : CategoriesState()
    data class Success(val categories: List<CategoryDto>) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}
