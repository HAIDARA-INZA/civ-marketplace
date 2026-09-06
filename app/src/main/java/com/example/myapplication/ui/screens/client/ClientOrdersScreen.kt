package com.example.myapplication.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.OrderDto
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.theme.Blue400
import com.example.myapplication.ui.theme.Cyan500
import com.example.myapplication.ui.theme.glassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrdersScreen(
    onBack: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state by viewModel.state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedBackground()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Mes commandes", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    windowInsets = WindowInsets.statusBars
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (val ordersState = state) {
                    OrdersState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is OrdersState.Error -> Text(
                        ordersState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    is OrdersState.Success -> {
                        if (ordersState.orders.isEmpty()) {
                            Text("Aucune commande", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.outline)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(ordersState.orders) { order ->
                                    OrderItem(order)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: OrderDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Commande #${order.id}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = order.price, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
            Text(text = order.productName, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)

            // Quantité
            order.quantity?.let {
                Text(
                    text = "Quantité : $it",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Statut commande
            Text(
                text = readableOrderStatus(order.status),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )

            // Statut paiement
            order.paymentStatus?.let { ps ->
                val (label, color) = when (ps.lowercase()) {
                    "paid"    -> "Paiement confirmé ✅" to Cyan500
                    "failed"  -> "Paiement échoué ❌" to MaterialTheme.colorScheme.error
                    else      -> "Paiement en attente ⏳" to Blue400
                }
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            OrderStatusTimeline(status = order.status)
        }
    }
}

@Composable
fun OrderStatusTimeline(status: String) {
    val steps = listOf("Acceptée", "Expédiée", "Livrée")
    val currentIndex = when (status.uppercase()) {
        "EXPEDIE", "SHIPPED" -> 1
        "LIVRE", "DELIVERED" -> 2
        else -> 0
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (index <= currentIndex) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index <= currentIndex) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                }
                Text(
                    text = step, 
                    fontSize = 10.sp, 
                    fontWeight = if (index <= currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index <= currentIndex) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }
            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.weight(0.5f).padding(bottom = 12.dp),
                    thickness = 2.dp,
                    color = if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

private fun readableOrderStatus(status: String): String {
    return when (status.uppercase()) {
        "EN_ATTENTE", "PENDING" -> "Paiement ou validation en attente"
        "ACCEPTE", "ACCEPTED" -> "Commande acceptée"
        "EXPEDIE", "SHIPPED" -> "Commande expédiée"
        "LIVRE", "DELIVERED" -> "Commande livrée"
        else -> status
    }
}
