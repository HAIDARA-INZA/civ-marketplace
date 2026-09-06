package com.example.myapplication.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.components.CIVButton
import com.example.myapplication.ui.components.CIVTextField
import com.example.myapplication.ui.screens.seller.SellerViewModel
import com.example.myapplication.ui.screens.seller.UploadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    viewModel: SellerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val uploadState by viewModel.uploadState
    val categoriesState by authViewModel.categoriesState

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(Unit) {
        authViewModel.loadCategories()
    }

    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Success) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un produit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // imePadding() pousse le contenu au-dessus du clavier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Cliquez pour choisir une image")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CIVTextField(value = name, onValueChange = { name = it }, label = "Nom du produit")
            Spacer(modifier = Modifier.height(16.dp))
            CIVTextField(
                value = price,
                onValueChange = { price = it },
                label = "Prix (FCFA)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CIVTextField(
                value = stock,
                onValueChange = { stock = it },
                label = "Stock disponible",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Catégorie", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            when (val state = categoriesState) {
                CategoriesState.Idle, CategoriesState.Loading -> CircularProgressIndicator()
                is CategoriesState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is CategoriesState.Success -> {
                    if (state.categories.isEmpty()) {
                        Text("Aucune catégorie configurée sur le serveur", color = MaterialTheme.colorScheme.outline)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.categories) { item ->
                                FilterChip(
                                    selected = category == item.name,
                                    onClick = { category = item.name },
                                    label = { Text(item.name) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CIVTextField(value = description, onValueChange = { description = it }, label = "Description", singleLine = false)

            if (uploadState is UploadState.Error) {
                Text(
                    text = (uploadState as UploadState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            CIVButton(
                text = "Publier le produit",
                onClick = {
                    viewModel.addProduct(
                        name        = name,
                        description = description,
                        price       = price,
                        stock       = stock,
                        category    = category,
                        imageUri    = imageUri ?: ""
                    )
                },
                isLoading = uploadState is UploadState.Loading
            )
        }
    }
}
