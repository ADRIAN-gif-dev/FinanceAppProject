package com.example.financeappproject.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.financeappproject.GeminiClient
import com.example.financeappproject.RetrofitClient
import com.example.financeappproject.SupabaseConfig
import com.example.financeappproject.models.Debt
import com.example.financeappproject.models.GeminiRequest
import com.example.financeappproject.models.GeminiResponse
import com.example.financeappproject.models.Transactions
import com.example.financeappproject.ui.components.FinanceBottomBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("user_id", "") ?: ""

    var isFabExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf(sharedPrefs.getString("user_name", "") ?: "") }
    val firstName = fullName.split(" ").firstOrNull() ?: fullName

    val transactions = remember { mutableStateListOf<Transaction>() }
    val alerts = remember { mutableStateListOf<BillAlert>() }

    var aiInsightText by remember { mutableStateOf("Analyzing your financial health...") }
    var isAiLoading by remember { mutableStateOf(false) }

    val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
    val totalBalance = totalIncome - totalExpense

    val fetchAiInsights = { currentBalance: Double, currentExpense: Double ->
        isAiLoading = true
        val prompt = """
            You are Pochiwise, a witty, supportive, and clever personal finance AI coach based in Kenya.
            Analyze this user's current situation:
            - Current Account Balance: Ksh $currentBalance
            - Total Amount Spent: Ksh $currentExpense
            
            Provide a smart, conversational 1-sentence finance tip or warning based on these exact values. Be encouraging, precise, and friendly.
        """.trimIndent()

        val request = GeminiRequest(prompt)

        GeminiClient.getGeminiApi().getInsights(geminiUrl, SupabaseConfig.GEMINI_API_KEY, request)
            .enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                    isAiLoading = false
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            aiInsightText = response.body()!!.candidates[0].content.parts[0].text
                        } catch (e: Exception) {
                            aiInsightText = "You've got Ksh ${String.format("%.2f", currentBalance)} safe. Let's look at your options to maximize your returns!"
                        }
                    } else {
                        aiInsightText = "Your current balance is sitting at Ksh ${String.format("%.2f", currentBalance)}. Tap here to chat with me and plan your next money move!"
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    isAiLoading = false
                    aiInsightText = "Your balance is safely calculated at Ksh ${String.format("%.2f", currentBalance)}. Tap here to chat and look over your strategies together!"
                }
            })
    }

    val refreshData = {
        if (userId.isNotEmpty()) {
            isLoading = true
            val api = RetrofitClient.getSupabaseApi()

            api.getTransactions(
                SupabaseConfig.API_KEY,
                "Bearer ${SupabaseConfig.API_KEY}",
                "eq.$userId"
            ).enqueue(object : Callback<List<Transactions>> {
                override fun onResponse(call: Call<List<Transactions>>, response: Response<List<Transactions>>) {
                    isLoading = false
                    if (response.isSuccessful && response.body() != null) {
                        transactions.clear()
                        response.body()!!.sortedByDescending { it.timestamp ?: "" }.forEach { t ->
                            transactions.add(Transaction(
                                id = t.trans_id.hashCode(),
                                title = t.type ?: "Transaction",
                                amount = t.amount,
                                date = formatDate(t.timestamp),
                                status = "Paid"
                            ))
                        }
                        fetchAiInsights(totalBalance, totalExpense)
                    }
                }
                override fun onFailure(call: Call<List<Transactions>>, t: Throwable) { isLoading = false }
            })

            api.getDebts(
                SupabaseConfig.API_KEY,
                "Bearer ${SupabaseConfig.API_KEY}",
                "eq.$userId"
            ).enqueue(object : Callback<List<Debt>> {
                override fun onResponse(call: Call<List<Debt>>, response: Response<List<Debt>>) {
                    if (response.isSuccessful && response.body() != null) {
                        alerts.clear()
                        response.body()!!.filter { it.status == "Active" }.forEach { d ->
                            alerts.add(BillAlert(d.debt_name, d.remaining_balance, "Due: ${d.due_date}"))
                        }
                    }
                }
                override fun onFailure(call: Call<List<Debt>>, t: Throwable) {}
            })
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { refreshData() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showNameDialog) {
        var tempName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Welcome!") },
            text = {
                Column {
                    Text("What should we call you?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Your Name") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (tempName.isNotBlank()) {
                        fullName = tempName
                        sharedPrefs.edit().putString("user_name", tempName).apply()
                        showNameDialog = false
                    }
                }) { Text("Save") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "${getGreeting()}, $firstName", style = MaterialTheme.typography.bodySmall)
                        Text(text = "PochiWise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.Person, contentDescription = "User Profile") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Profile") }, onClick = { navController.navigate("profile") })
                        DropdownMenuItem(text = { Text("Settings") }, onClick = { navController.navigate("settings") })
                        DropdownMenuItem(text = { Text("Refresh Data") }, onClick = { showMenu = false; refreshData() })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Logout") }, onClick = { navController.navigate("login") { popUpTo(0) } })
                    }
                }
            )
        },
        bottomBar = { FinanceBottomBar(navController) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (isFabExpanded) {
                    FabOption(Icons.Default.TrendingUp, "Add Income", Color(0xFF4CAF50)) {
                        isFabExpanded = false; navController.navigate("add_income")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FabOption(Icons.Default.TrendingDown, "Add Expense", Color(0xFFF44336)) {
                        isFabExpanded = false; navController.navigate("add_expense")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FabOption(Icons.Default.MoneyOff, "Add Debt", Color(0xFFFF9800)) {
                        isFabExpanded = false; navController.navigate("add_debt")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded }) {
                    Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item { SummaryCard(income = totalIncome, expense = totalExpense, balance = totalBalance) }
                item {
                    AiInsightCard(
                        insightText = aiInsightText,
                        isLoading = isAiLoading,
                        onCardClick = { navController.navigate("chat") },
                        onRefreshClick = { refreshData() }
                    )
                }
                if (alerts.isNotEmpty()) { item { UpcomingBillsSection(alerts) } }
                item {
                    Text(text = "Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                if (transactions.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No transactions yet.", color = Color.Gray)
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionListItem(transaction) { transactions.remove(transaction) }
                        HorizontalDivider()
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
            if (isLoading && transactions.isEmpty()) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
        }
    }
}

@Composable
fun SummaryCard(income: Double, expense: Double, balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Balance", style = MaterialTheme.typography.bodyMedium)
            Text("Ksh ${String.format("%.2f", balance)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem("Income", "Ksh ${String.format("%.0f", income)}", Color(0xFF4CAF50))
                SummaryItem("Expenses", "Ksh ${String.format("%.0f", expense)}", Color(0xFFF44336))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightCard(insightText: String, isLoading: Boolean, onCardClick: () -> Unit, onRefreshClick: () -> Unit) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Coach", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Smart Insights & Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onRefreshClick, enabled = !isLoading) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Recalculate advice", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Pochiwise is calculating money moves...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                Text(text = insightText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Tap to talk with your AI advisor →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(amount, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun UpcomingBillsSection(alerts: List<BillAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Due Date Alerts", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            alerts.forEach { alert ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(alert.name, fontWeight = FontWeight.Medium)
                        Text(alert.dueDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("Ksh ${alert.amount}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: Transaction, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(transaction.title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(transaction.date, style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            val color = if (transaction.amount > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            val prefix = if (transaction.amount > 0) "+" else ""
            Text("$prefix Ksh ${abs(transaction.amount)}", color = color, fontWeight = FontWeight.Bold)
        }
    )
}

@Composable
fun FabOption(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(onClick = onClick, containerColor = color, contentColor = Color.White, modifier = Modifier.size(44.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

data class Transaction(val id: Int, val title: String, val amount: Double, val date: String, val status: String)
data class BillAlert(val name: String, val amount: Double, val dueDate: String)

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

fun formatDate(timestamp: String?): String {
    if (timestamp == null) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timestamp)
        SimpleDateFormat("MMM dd", Locale.getDefault()).format(date!!)
    } catch (e: Exception) {
        timestamp.split("T").firstOrNull() ?: timestamp
    }
}