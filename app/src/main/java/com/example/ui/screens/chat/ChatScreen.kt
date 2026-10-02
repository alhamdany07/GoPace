package com.example.ui.screens.chat

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    driverName: String,
    driverPhone: String,
    driverWhatsapp: String,
    isCurrentCustomerView: Boolean,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoPaceGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCurrentCustomerView) driverName.take(1) else "C",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isCurrentCustomerView) driverName else "Pelanggan GoPace",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(StatusOnline)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Online (Merauke)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // WhatsApp Direct
                    IconButton(
                        onClick = {
                            try {
                                val url = "https://wa.me/$driverWhatsapp"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Buka WhatsApp ke $driverWhatsapp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }

                    // Direct Phone Call
                    IconButton(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhone")))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Telepon ke $driverPhone", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Telepon GSM", tint = GoPaceGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SoftBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                // Quick Replies Chips
                val quickReplies = if (isCurrentCustomerView) {
                    listOf(
                        "Saya sudah di titik jemput kak",
                        "Patokannya depan pohon pinang pagar biru",
                        "Samping kios mama Papua ya",
                        "Bisa minta tolong agak cepat ya pace?",
                        "Oke siap ditunggu"
                    )
                } else {
                    listOf(
                        "Saya sudah di titik jemput",
                        "Pesanan sedang diantre ya kak",
                        "Menuju lokasi antar sekarang",
                        "Mohon tunggu sebentar ya",
                        "Siap pace/mace!"
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    items(quickReplies) { reply ->
                        AssistChip(
                            onClick = { onSendMessage(reply) },
                            label = { Text(reply, fontSize = 11.sp) },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ketik pesan atau patokan...") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SoftBackground,
                            unfocusedContainerColor = SoftBackground,
                            focusedIndicatorColor = GoPaceGreen,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoPaceGreen)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = if (isCurrentCustomerView) msg.isFromCustomer else !msg.isFromCustomer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = msg.senderName,
                        fontSize = 10.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isMe) 14.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 14.dp
                        ),
                        color = if (isMe) GoPaceGreen else Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isMe) Color.White else TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
