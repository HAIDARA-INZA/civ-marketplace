package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.VendorSummaryDto
import com.example.myapplication.ui.components.CIVTextField
import com.example.myapplication.ui.components.ProductCard

@Composable
fun SearchScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToVendorStore: (Int, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val productsState by viewModel.homeState
    val vendorsState by authViewModel.vendorsState

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) authViewModel.loadVendors()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Rechercher",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Produits") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Vendeurs") }
            )
        }

        Spacer(Modifier.height(12.dp))

        CIVTextField(
            value = query,
            onValueChange = { value ->
                query = value
                if (selectedTab == 0) viewModel.loadProducts(value)
            },
            label = if (selectedTab == 0) {
                "Nom du produit"
            } else {
                "Nom du vendeur ou de la boutique"
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        )

        Spacer(Modifier.height(12.dp))

        if (selectedTab == 0) {
            ProductSearchResults(
                state = productsState,
                query = query,
                onNavigateToDetail = onNavigateToDetail,
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
        } else {
            VendorSearchResults(
                state = vendorsState,
                query = query,
                onNavigateToVendorStore = onNavigateToVendorStore,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProductSearchResults(
    state: HomeState,
    query: String,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            HomeState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is HomeState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is HomeState.Success -> {
                val products = state.products.filter {
                    it.name.contains(query, ignoreCase = true)
                }

                if (products.isEmpty()) {
                    Text(
                        text = "Aucun produit trouvé",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onClick = {
                                    onNavigateToDetail(product.id.toString())
                                },
                                modifier = Modifier.padding(6.dp),
                                onFavoriteClick = { favorite ->
                                    viewModel.setFavorite(product, favorite)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorSearchResults(
    state: VendorsState,
    query: String,
    onNavigateToVendorStore: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            VendorsState.Idle,
            VendorsState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is VendorsState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is VendorsState.Success -> {
                val vendors = state.vendors.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                val visibleVendors = if (query.isBlank()) {
                    vendors.take(6)
                } else {
                    vendors
                }

                if (visibleVendors.isEmpty()) {
                    Text(
                        text = "Aucun vendeur trouvé",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleVendors, key = { it.id }) { vendor ->
                            VendorCard(vendor) { onNavigateToVendorStore(vendor.id, vendor.name) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorCard(vendor: VendorSummaryDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(vendor.name, fontWeight = FontWeight.Bold)

            val location = listOfNotNull(
                vendor.city,
                vendor.commune
            ).joinToString(" • ")

            if (location.isNotBlank()) {
                Text(
                    text = location,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            vendor.vendorCategories
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    Text(
                        text = it.joinToString(", "),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
        }
    }
}
