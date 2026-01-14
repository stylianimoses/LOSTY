package com.fyp.losty.messaging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fyp.losty.AppViewModel
import com.fyp.losty.Message
import com.fyp.losty.MessagesState
import com.fyp.losty.ui.components.PullToRefreshBox

@Composable
fun ChatScreen(conversationId: String, appViewModel: AppViewModel = viewModel()) {
    var text by remember { mutableStateOf("") }
    val messagesState by appViewModel.messagesState.collectAsState()
    val isRefreshing by remember { mutableStateOf(false) } 

    LaunchedEffect(conversationId) {
        appViewModel.loadMessages(conversationId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = messagesState) {
            is MessagesState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            }
            is MessagesState.Success -> {
                PullToRefreshBox(isRefreshing = isRefreshing, modifier = Modifier.weight(1f)) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        items(state.messages) { msg ->
                            Text(text = "${msg.senderName}: ${msg.text}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
            is MessagesState.Error -> {
                Text(text = state.message, modifier = Modifier.fillMaxSize())
            }
        }


        Row(modifier = Modifier.padding(8.dp)) {
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                appViewModel.sendMessage(conversationId, text)
                text = ""
            }) {
                Text("Send")
            }
        }
    }
}
