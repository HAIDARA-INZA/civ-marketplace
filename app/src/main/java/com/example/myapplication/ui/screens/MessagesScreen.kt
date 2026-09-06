package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.ConversationDto
import com.example.myapplication.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigateToChat: (Int, String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.conversationsState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Discussions", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    windowInsets = WindowInsets.statusBars
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val conversationsState = state) {
                    is ChatState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ChatState.Success -> {
                        val data = conversationsState.data
                        if (data.isEmpty()) {
                            Text("Aucune conversation", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.outline)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(data) { conversation ->
                                    ConversationItem(
                                        conversation = conversation,
                                        onClick = { onNavigateToChat(conversation.otherUserId, conversation.otherUserName) }
                                    )
                                }
                            }
                        }
                    }
                    is ChatState.Error -> {
                        Text(conversationsState.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: ConversationDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            ,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.otherUserName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.otherUserName, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = TimeUtils.humanReadableDateTime(conversation.lastMessageTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("${conversation.unreadCount}", color = Color.White)
                    }
                }
            }
        }
    }
}
