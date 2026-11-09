package com.zict.tooltrail.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zict.tooltrail.data.entity.BorrowingEntity
import com.zict.tooltrail.data.entity.EquipmentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DASHBOARD = "dashboard"
private const val EQUIPMENT = "equipment"
private const val LOANS = "loans"
private const val HISTORY = "history"
private const val ADD_EQUIPMENT = "add_equipment"
private const val BORROW = "borrow"
private val TABS = listOf(DASHBOARD, EQUIPMENT, LOANS, HISTORY)

private fun tabLabel(route: String) = when (route) {
    DASHBOARD -> "Dashboard"
    EQUIPMENT -> "Equipment"
    LOANS -> "Loans"
    else -> "History"
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTrailApp(vm: ToolTrailViewModel = viewModel()) {
    val equipment by vm.equipment.collectAsStateWithLifecycle()
    val active by vm.activeBorrowings.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val activeIds by vm.activeEquipmentIds.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    var route by rememberSaveable { mutableStateOf(DASHBOARD) }
    var selectedEquipmentId by rememberSaveable { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    // Back from a form returns to the equipment list; back from a tab returns to the dashboard.
    BackHandler(enabled = route == ADD_EQUIPMENT || route == BORROW) { route = EQUIPMENT }
    BackHandler(enabled = route in TABS && route != DASHBOARD) { route = DASHBOARD }

    val title = when (route) {
        ADD_EQUIPMENT -> "Add equipment"
        BORROW -> "Borrow equipment"
        else -> tabLabel(route)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (route in TABS) {
                NavigationBar {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab,
                            onClick = { route = tab },
                            icon = {},
                            label = { Text(tabLabel(tab)) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (route == EQUIPMENT) {
                FloatingActionButton(onClick = { route = ADD_EQUIPMENT }) { Text("+") }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val activeIdSet = activeIds.toSet()
            val nameById = equipment.associate { it.id to it.name }
            when (route) {
                DASHBOARD -> DashboardScreen(
                    total = equipment.size,
                    available = equipment.count { it.id !in activeIdSet },
                    activeLoans = active.size,
                    overdue = active.count { it.dueDate < System.currentTimeMillis() }
                )
                EQUIPMENT -> EquipmentScreen(
                    equipment = equipment,
                    onLoanIds = activeIdSet,
                    onBorrow = { selectedEquipmentId = it.id; route = BORROW }
                )
                LOANS -> LoansScreen(
                    loans = active,
                    nameById = nameById,
                    onReturn = { vm.returnEquipment(it.id) }
                )
                HISTORY -> HistoryScreen(history, nameById)
                ADD_EQUIPMENT -> AddEquipmentScreen { name, cat, desc, cond ->
                    vm.addEquipment(name, cat, desc, cond) { route = EQUIPMENT }
                }
                BORROW -> BorrowScreen(
                    equipmentName = nameById[selectedEquipmentId] ?: "Unknown item"
                ) { borrower, contact, hours ->
                    vm.borrow(selectedEquipmentId, borrower, contact, hours) { route = LOANS }
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(total: Int, available: Int, activeLoans: Int, overdue: Int) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total equipment", total, Modifier.weight(1f))
            StatCard("Available", available, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Active loans", activeLoans, Modifier.weight(1f))
            StatCard("Overdue", overdue, Modifier.weight(1f))
        }
        if (total == 0) {
            Text("No equipment yet. Open the Equipment tab and tap + to add your first item.")
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineLarge)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EquipmentScreen(
    equipment: List<EquipmentEntity>,
    onLoanIds: Set<Long>,
    onBorrow: (EquipmentEntity) -> Unit
) {
    if (equipment.isEmpty()) {
        EmptyState("No equipment registered yet. Tap + to add some.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(equipment, key = { it.id }) { item ->
            val onLoan = item.id in onLoanIds
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text("${item.category} - Condition: ${item.condition}")
                    if (item.description.isNotBlank()) Text(item.description)
                    Text(
                        if (onLoan) "On loan" else "Available",
                        color = if (onLoan) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { onBorrow(item) },
                        enabled = !onLoan,
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Borrow") }
                }
            }
        }
    }
}

@Composable
private fun LoansScreen(
    loans: List<BorrowingEntity>,
    nameById: Map<Long, String>,
    onReturn: (BorrowingEntity) -> Unit
) {
    if (loans.isEmpty()) {
        EmptyState("No active loans.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(loans, key = { it.id }) { loan ->
            val overdue = loan.dueDate < System.currentTimeMillis()
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(nameById[loan.equipmentId] ?: "Unknown item",
                        style = MaterialTheme.typography.titleMedium)
                    Text("Borrower: ${loan.borrowerName} (${loan.borrowerContact})")
                    Text("Due: ${formatDate(loan.dueDate)}")
                    if (overdue) {
                        Text("OVERDUE", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Button(onClick = { onReturn(loan) }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Mark returned")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(history: List<BorrowingEntity>, nameById: Map<Long, String>) {
    if (history.isEmpty()) {
        EmptyState("No completed borrowings yet.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(history, key = { it.id }) { h ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(nameById[h.equipmentId] ?: "Unknown item",
                        style = MaterialTheme.typography.titleMedium)
                    Text("Borrower: ${h.borrowerName}")
                    Text("Borrowed: ${formatDate(h.borrowedAt)}")
                    Text("Returned: ${h.returnedAt?.let { formatDate(it) } ?: "-"}")
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp)) {
        Text(text, modifier = Modifier.padding(top = 24.dp))
    }
}

@Composable
private fun AddEquipmentScreen(onSave: (String, String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var condition by rememberSaveable { mutableStateOf("Good") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name, onValueChange = { name = it; nameError = null },
            label = { Text("Name *") }, isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = category, onValueChange = { category = it; categoryError = null },
            label = { Text("Category *") }, isError = categoryError != null,
            supportingText = { categoryError?.let { Text(it) } },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Description") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = condition, onValueChange = { condition = it },
            label = { Text("Condition") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            nameError = if (name.isBlank()) "Name is required" else null
            categoryError = if (category.isBlank()) "Category is required" else null
            if (nameError == null && categoryError == null) {
                onSave(name, category, description, condition)
            }
        }) { Text("Save equipment") }
    }
}

@Composable
private fun BorrowScreen(
    equipmentName: String,
    onConfirm: (String, String, Double) -> Unit
) {
    var borrower by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var hours by rememberSaveable { mutableStateOf("24") }
    var borrowerError by rememberSaveable { mutableStateOf<String?>(null) }
    var contactError by rememberSaveable { mutableStateOf<String?>(null) }
    var hoursError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Item: $equipmentName", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = borrower, onValueChange = { borrower = it; borrowerError = null },
            label = { Text("Borrower name *") }, isError = borrowerError != null,
            supportingText = { borrowerError?.let { Text(it) } },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contact, onValueChange = { contact = it; contactError = null },
            label = { Text("Borrower contact *") }, isError = contactError != null,
            supportingText = { contactError?.let { Text(it) } },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = hours, onValueChange = { hours = it; hoursError = null },
            label = { Text("Return in how many hours? *") }, isError = hoursError != null,
            supportingText = { hoursError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val h = hours.toDoubleOrNull()
            borrowerError = if (borrower.isBlank()) "Borrower name is required" else null
            contactError = if (contact.isBlank()) "Contact is required" else null
            hoursError = if (h == null || h <= 0.0) "Enter a number greater than 0" else null
            if (borrowerError == null && contactError == null && hoursError == null && h != null) {
                onConfirm(borrower, contact, h)
            }
        }) { Text("Confirm borrowing") }
    }
}
