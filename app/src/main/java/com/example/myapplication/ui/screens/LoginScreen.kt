package com.example.myapplication.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.components.CIVButton
import com.example.myapplication.ui.components.CIVTextField
import com.example.myapplication.ui.theme.Blue600

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayedError by rememberSaveable { mutableStateOf<String?>(null) }
    val authState by viewModel.authState
    val focusManager = LocalFocusManager.current
    val emailError = remember(email) {
        if (email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else "Format d'e-mail invalide"
    }
    val passwordError = remember(password) {
        if (password.isBlank() || password.length >= 6) null else "Le mot de passe doit contenir au moins 6 caractères"
    }

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            AuthState.NeedVerification -> {
                onNavigateToVerify(email.trim())
                viewModel.resetState()
            }
            is AuthState.Error -> displayedError = (authState as AuthState.Error).message
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.inza),
            contentDescription = "Logo CIV Marketplace",
            modifier = Modifier.size(88.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(24.dp))
        Text("Connexion", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Connectez-vous pour continuer vos achats.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, fontSize = 15.sp)
        Spacer(Modifier.height(32.dp))

        CIVTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail",
            isError = emailError != null,
            errorMessage = emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(16.dp))
        CIVTextField(
            value = password,
            onValueChange = { password = it },
            label = "Mot de passe",
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            errorMessage = passwordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onNavigateToForgotPassword) { Text("Mot de passe oublié ?", color = Blue600) }
        }

        if (displayedError != null) {
            Text(displayedError!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(Modifier.height(16.dp))
        CIVButton(
            text = "Se connecter",
            onClick = {
                focusManager.clearFocus()
                displayedError = null
                viewModel.login(email.trim(), password)
            },
            isLoading = authState is AuthState.Loading,
            enabled = email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
        )
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pas encore de compte ?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onNavigateToRegister) { Text("Inscrivez-vous", color = Blue600, fontWeight = FontWeight.Bold) }
        }
    }
}
