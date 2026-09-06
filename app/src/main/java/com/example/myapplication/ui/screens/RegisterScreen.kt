package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.VendorSummaryDto
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.components.CIVButton
import com.example.myapplication.ui.components.CIVTextField
import com.example.myapplication.util.RoleUtils
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }
    var quarter by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("client") }
    var sellerSelectionMode by remember { mutableStateOf("all") }
    var vendorSearch by remember { mutableStateOf("") }

    val selectedSellerIds = remember { mutableStateListOf<Int>() }
    val selectedCategories = remember { mutableStateListOf<String>() }
    val authState by viewModel.authState
    val vendorsState by viewModel.vendorsState
    val categoriesState by viewModel.categoriesState

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
        viewModel.loadCategories()
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.NeedVerification) {
            onNavigateToVerify(email)
            viewModel.resetState()
        }
    }

    LaunchedEffect(role, sellerSelectionMode) {
        if (RoleUtils.canBuy(role) && sellerSelectionMode == "specific") {
            viewModel.loadVendors()
        }
        if (!RoleUtils.canBuy(role)) selectedSellerIds.clear()
        if (!RoleUtils.canSell(role)) selectedCategories.clear()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 20 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Créer un compte",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Configurez votre expérience CIV Marketplace",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 28.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) + slideInVertically(initialOffsetY = { 30 })
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Je suis",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SelectionRow(
                        title = "Client",
                        subtitle = "Je veux acheter auprès des vendeurs",
                        selected = role == "client",
                        onClick = { role = "client" }
                    )
                    SelectionRow(
                        title = "Vendeur",
                        subtitle = "Je veux vendre mes produits",
                        selected = role == "vendeur",
                        onClick = { role = "vendeur" }
                    )
                    SelectionRow(
                        title = "Vendeur + client",
                        subtitle = "Je veux vendre et acheter",
                        selected = role == "vendeur_client",
                        onClick = { role = "vendeur_client" }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CIVTextField(value = name, onValueChange = { name = it }, label = "Nom complet")
                    Spacer(modifier = Modifier.height(14.dp))
                    CIVTextField(value = email, onValueChange = { email = it }, label = "E-mail")
                    Spacer(modifier = Modifier.height(14.dp))
                    CIVTextField(value = phone, onValueChange = { phone = it }, label = "Téléphone")
                    Spacer(modifier = Modifier.height(14.dp))
                    CIVTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Localité",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CIVTextField(value = city, onValueChange = { city = it }, label = "Ville")
                    Spacer(modifier = Modifier.height(14.dp))
                    CIVTextField(value = commune, onValueChange = { commune = it }, label = "Commune")
                    Spacer(modifier = Modifier.height(14.dp))
                    CIVTextField(value = quarter, onValueChange = { quarter = it }, label = "Quartier")
                    Spacer(modifier = Modifier.height(14.dp))
                    CIVTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Adresse / repère",
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    if (RoleUtils.canBuy(role)) {
                        ClientVendorPreferenceSection(
                            sellerSelectionMode = sellerSelectionMode,
                            onModeChange = { mode ->
                                sellerSelectionMode = mode
                                if (mode == "all") selectedSellerIds.clear()
                            },
                            vendorSearch = vendorSearch,
                            onVendorSearchChange = { vendorSearch = it },
                            vendorsState = vendorsState,
                            selectedSellerIds = selectedSellerIds
                        )
                    }

                    if (RoleUtils.canSell(role)) {
                        Spacer(modifier = Modifier.height(20.dp))
                        VendorCategoriesSection(
                            categoriesState = categoriesState,
                            selectedCategories = selectedCategories
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CIVButton(
                        text = "S'inscrire",
                        onClick = {
                            viewModel.register(
                                name = name,
                                email = email,
                                password = password,
                                role = role,
                                phone = phone,
                                city = city,
                                commune = commune,
                                quarter = quarter,
                                address = address,
                                sellerSelectionMode = sellerSelectionMode,
                                preferredSellerIds = selectedSellerIds.toList(),
                                vendorCategories = selectedCategories.toList()
                            )
                        },
                        isLoading = authState is AuthState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Déjà un compte ? Connectez-vous",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SelectionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun ClientVendorPreferenceSection(
    sellerSelectionMode: String,
    onModeChange: (String) -> Unit,
    vendorSearch: String,
    onVendorSearchChange: (String) -> Unit,
    vendorsState: VendorsState,
    selectedSellerIds: MutableList<Int>
) {
    Text(
        text = "Mes vendeurs",
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))

    SelectionRow(
        title = "Tous les vendeurs",
        subtitle = "Voir tous les vendeurs disponibles sur l'accueil",
        selected = sellerSelectionMode == "all",
        onClick = { onModeChange("all") }
    )
    SelectionRow(
        title = "Choisir mes vendeurs",
        subtitle = "Sélectionner des vendeurs particuliers",
        selected = sellerSelectionMode == "specific",
        onClick = { onModeChange("specific") }
    )

    if (sellerSelectionMode == "specific") {
        Spacer(modifier = Modifier.height(12.dp))
        CIVTextField(
            value = vendorSearch,
            onValueChange = onVendorSearchChange,
            label = "Rechercher un vendeur"
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (vendorsState) {
            is VendorsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is VendorsState.Success -> {
                val filteredVendors = vendorsState.vendors
                    .filter { vendor ->
                        vendorSearch.isBlank() ||
                            vendor.name.contains(vendorSearch, ignoreCase = true) ||
                            vendor.city.orEmpty().contains(vendorSearch, ignoreCase = true) ||
                            vendor.commune.orEmpty().contains(vendorSearch, ignoreCase = true)
                    }
                    .take(12)

                if (filteredVendors.isEmpty()) {
                    Text(
                        text = "Aucun vendeur trouvé",
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    filteredVendors.forEach { vendor ->
                        VendorSelectorRow(
                            vendor = vendor,
                            selected = selectedSellerIds.contains(vendor.id),
                            onToggle = {
                                if (selectedSellerIds.contains(vendor.id)) {
                                    selectedSellerIds.remove(vendor.id)
                                } else {
                                    selectedSellerIds.add(vendor.id)
                                }
                            }
                        )
                    }
                }
            }
            is VendorsState.Error -> {
                Text(
                    text = vendorsState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            VendorsState.Idle -> {
                Text(
                    text = "Chargement des vendeurs...",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun VendorSelectorRow(
    vendor: VendorSummaryDto,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = vendor.name, fontWeight = FontWeight.SemiBold)
            Text(
                text = listOfNotNull(vendor.city, vendor.commune, vendor.quarter).joinToString(" - "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
            if (!vendor.vendorCategories.isNullOrEmpty()) {
                Text(
                    text = vendor.vendorCategories.joinToString(", "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VendorCategoriesSection(
    categoriesState: CategoriesState,
    selectedCategories: MutableList<String>
) {
    Text(
        text = "Catégories de vente",
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(
        text = "Vous pourrez modifier ou ajouter des catégories plus tard.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )

    when (categoriesState) {
        CategoriesState.Idle, CategoriesState.Loading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CategoriesState.Error -> {
            Text(
                text = categoriesState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        is CategoriesState.Success -> {
            if (categoriesState.categories.isEmpty()) {
                Text(
                    text = "Aucune catégorie configurée sur le serveur",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                categoriesState.categories.forEach { category ->
                    CategorySelectorRow(
                        category = category.name,
                        selected = selectedCategories.contains(category.name),
                        onToggle = {
                            if (selectedCategories.contains(category.name)) {
                                selectedCategories.remove(category.name)
                            } else {
                                selectedCategories.add(category.name)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectorRow(
    category: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = category)
    }
}
