package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.CIVButton
import com.example.myapplication.ui.components.CIVTextField

@Composable
fun ResetPasswordScreen(
    email: String,
    onSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val authState by viewModel.authState
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.ResetSuccess) {
            Toast.makeText(context, "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()
            onSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nouveau mot de passe",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        CIVTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            label = "Code reçu par e-mail"
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        CIVTextField(
            value = password,
            onValueChange = { password = it },
            label = "Nouveau mot de passe",
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        CIVTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmer le mot de passe",
            visualTransformation = PasswordVisualTransformation()
        )

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        CIVButton(
            text = "Réinitialiser",
            onClick = { 
                if (password == confirmPassword) {
                    viewModel.resetPassword(email, otp, password)
                } else {
                    Toast.makeText(context, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                }
            },
            isLoading = authState is AuthState.Loading
        )
    }
}
