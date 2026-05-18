package com.example.financeappproject.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeappproject.GeminiClient
import com.example.financeappproject.SupabaseConfig
import com.example.financeappproject.models.GeminiRequest
import com.example.financeappproject.models.GeminiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onNavigateBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>().apply {
        add(ChatMessage("Hello! I'm your Pochiwise coach. Ask me anything about managing your money, savings goals, or budgets!", false))
    }}
    var isSending by remember { mutableStateOf(false) }

    // Explicit production endpoint URL string to prevent 404 compilation mismatches
    val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pochiwise AI Coach", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isUser) 16.dp else 0.dp,
                                bottomEnd = if (msg.isUser) 0.dp else 16.dp
                            ),
                            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            if (isSending) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask about budgeting, inflation, saving...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isSending
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val userPrompt = messageText
                            messages.add(ChatMessage(userPrompt, true))
                            messageText = ""
                            isSending = true

                            val contextPrompt = """
                                You are Pochiwise, a witty, supportive, and highly knowledgeable personal finance AI coach based in Kenya. 
                                Give short, accurate, smart, and friendly monetary tips.
                                User asks: $userPrompt
                            """.trimIndent()

                            GeminiClient.getGeminiApi().getInsights(geminiUrl, SupabaseConfig.GEMINI_API_KEY, GeminiRequest(contextPrompt))
                                .enqueue(object : Callback<GeminiResponse> {
                                    override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                                        isSending = false
                                        if (response.isSuccessful && response.body() != null) {
                                            try {
                                                val reply = response.body()!!.candidates[0].content.parts[0].text
                                                messages.add(ChatMessage(reply, false))
                                            } catch (e: Exception) {
                                                messages.add(ChatMessage("I parsed the response but couldn't read the layout data safely.", false))
                                                Log.e("GEMINI_ERROR", "Parsing layout extraction exception: ${e.message}")
                                            }
                                        } else {
                                            val errorBodyString = response.errorBody()?.string()
                                            Log.e("GEMINI_ERROR", "HTTP Code: ${response.code()}")
                                            Log.e("GEMINI_ERROR", "Server Response: $errorBodyString")
                                            messages.add(ChatMessage("Sorry, I encountered an issue checking that strategy (Error ${response.code()}). Try again!", false))
                                        }
                                    }

                                    override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                                        isSending = false
                                        Log.e("GEMINI_ERROR", "Network Failure Execution: ${t.message}")
                                        messages.add(ChatMessage("Network connection dropped. Please check your internet connection.", false))
                                    }
                                })
                        }
                    },
                    enabled = messageText.isNotBlank() && !isSending
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}