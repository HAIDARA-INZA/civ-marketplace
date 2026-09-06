package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStoreScreen(
    vendorId: Int,
    vendorName: String,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.homeState
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(vendorName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            }
        )
    }) { padding ->
        when (state) {
            HomeState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is HomeState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text((state as HomeState.Error).message, color = MaterialTheme.colorScheme.error) }
            is HomeState.Success -> {
                val products = (state as HomeState.Success).products.filter { it.getActualSellerId() == vendorId }
                if (products.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Cette boutique n'a pas encore de produit.") }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onNavigateToDetail(product.id.toString()) },
                                onFavoriteClick = { viewModel.setFavorite(product, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
