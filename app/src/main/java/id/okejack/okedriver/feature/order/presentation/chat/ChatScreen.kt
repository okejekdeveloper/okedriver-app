package id.okejack.okedriver.feature.order.presentation.chat

import id.okejack.okedriver.core.designsystem.component.DriverButton as Button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.theme.*
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    orderId: Long,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(orderId, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) { viewModel.loadMessages(orderId); delay(5_000) }
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Order #$orderId",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = state.draftText,
                isSending = state.isSending,
                sendError = state.sendError,
                onTextChange = { viewModel.setDraft(it) },
                onSend = { viewModel.sendMessage(orderId, state.draftText) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OkeBg)
                .padding(padding)
        ) {
            when {
                state.isLoading && state.messages.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OkePrimary
                    )
                }
                state.errorMessage != null && state.messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OkeTextSecondary
                        )
                        Button(
                            onClick = { viewModel.loadMessages(orderId) },
                            colors = ButtonDefaults.buttonColors(containerColor = OkePrimary)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                state.messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Belum ada pesan",
                            style = MaterialTheme.typography.titleMedium,
                            color = OkeTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kirim pesan pertama ke pelanggan",
                            style = MaterialTheme.typography.bodySmall,
                            color = OkeTextSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // The server's messages may carry no id (the legacy model has none); equal keys would crash the list.
                        itemsIndexed(items = state.messages, key = { index, message -> message.id.takeIf { it > 0 } ?: -(index + 1L) }) { _, message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isDriver = message.isFromDriver
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isDriver) Arrangement.End else Arrangement.Start
    ) {
        if (!isDriver) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(OkePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.firstOrNull()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.labelMedium,
                    color = OkePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isDriver) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isDriver && message.senderName.isNotBlank()) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = OkeTextHint,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isDriver) 16.dp else 4.dp,
                            bottomEnd = if (isDriver) 4.dp else 16.dp
                        )
                    )
                    .background(if (isDriver) OkePrimary else OkeSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDriver) Color.White else OkeTextPrimary
                )
            }
            if (message.createdAt.isNotBlank()) {
                Text(
                    text = message.createdAt.take(16).replace("T", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = OkeTextHint,
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    isSending: Boolean,
    sendError: String?,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OkeSurface)
            .navigationBarsPadding()
            .imePadding()
    ) {
        if (text.isBlank() && !isSending) {
            Text("Pesan cepat · pilih, lalu kirim", modifier = Modifier.padding(start = 16.dp, top = 10.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Saya menuju lokasi jemput.", "Saya sudah sampai di lokasi jemput.", "Mohon tunggu sebentar, ya.").forEach { reply ->
                    SuggestionChip(onClick = { onTextChange(reply) }, label = { Text(reply) })
                }
            }
        }
        if (sendError != null) {
            Text(
                text = sendError,
                style = MaterialTheme.typography.labelSmall,
                color = OkeDanger,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        HorizontalDivider(color = OkeDivider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Tulis pesan...",
                        color = OkeTextHint,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                maxLines = 4,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OkePrimary,
                    unfocusedBorderColor = OkeDivider
                )
            )
            Button(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending,
                modifier = Modifier.heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OkePrimary)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Kirim")
                }
            }
        }
    }
}
