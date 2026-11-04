package com.trippilot.trippilotai.ui.screens.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.data.mock.MockDestinations
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.InfoBanner
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.components.StepperControl
import com.trippilot.trippilotai.ui.components.icon
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.formatDate
import com.trippilot.trippilotai.ui.util.formatInrCompact
import com.trippilot.trippilotai.ui.viewmodel.PlanField
import com.trippilot.trippilotai.ui.viewmodel.PlanTripUiState
import com.trippilot.trippilotai.ui.viewmodel.PlanTripViewModel
import com.trippilot.trippilotai.util.todayUtcMillis
import kotlin.math.roundToInt

@Composable
fun PlanTripScreen(
    onItineraryReady: () -> Unit,
    viewModel: PlanTripViewModel = viewModel(factory = PlanTripViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.generatedPlanReady) {
        if (state.generatedPlanReady) {
            viewModel.onNavigated()
            onItineraryReady()
        }
    }

    PlanTripContent(
        state = state,
        onDestinationTyped = viewModel::onDestinationTyped,
        onDestinationPicked = viewModel::onDestinationPicked,
        onDatesChange = viewModel::onDatesChange,
        onTravellersChange = viewModel::onTravellersChange,
        onBudgetChange = viewModel::onBudgetChange,
        onInterestToggle = viewModel::onInterestToggle,
        onGenerate = viewModel::generate,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanTripContent(
    state: PlanTripUiState,
    onDestinationTyped: (String) -> Unit,
    onDestinationPicked: (String) -> Unit,
    onDatesChange: (Long?, Long?) -> Unit,
    onTravellersChange: (Int) -> Unit,
    onBudgetChange: (Int, Int) -> Unit,
    onInterestToggle: (Interest) -> Unit,
    onGenerate: () -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { BackTopBar("Plan a trip", onBack = null) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(Modifier.padding(16.dp)) {
                    if (state.errors.isNotEmpty()) {
                        Text(
                            "Please fix the highlighted fields.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    PrimaryActionButton(
                        text = if (state.isGenerating) "Generating…" else "Generate AI Itinerary",
                        onClick = onGenerate,
                        enabled = !state.isGenerating,
                        icon = Icons.Default.AutoAwesome,
                    )
                }
            }
        },
    ) { padding ->
        if (state.isGenerating) {
            GeneratingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Tell us about your trip and we'll draft a day-by-day plan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FormSection("Destination") {
                DestinationField(state, onDestinationTyped, onDestinationPicked)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MockDestinations.all.forEach { destination ->
                        val selected = destination.name.equals(state.destination.trim(), ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { onDestinationPicked(destination.name) },
                            label = { Text(destination.name) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null,
                        )
                    }
                }
            }

            FormSection("Travel dates") {
                DatesField(state, onClick = { showDatePicker = true })
            }

            FormSection("Travellers") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (state.travellers == 1) "1 traveller" else "${state.travellers} travellers",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    StepperControl(
                        value = state.travellers,
                        onValueChange = onTravellersChange,
                        range = PlanTripViewModel.MIN_TRAVELLERS..PlanTripViewModel.MAX_TRAVELLERS,
                        label = "travellers",
                    )
                }
            }

            FormSection("Total budget range") {
                BudgetSlider(state.budgetMin, state.budgetMax, onBudgetChange)
            }

            FormSection("Interests") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Interest.entries.forEach { interest ->
                        FilterChip(
                            selected = interest in state.interests,
                            onClick = { onInterestToggle(interest) },
                            label = { Text(interest.label) },
                            leadingIcon = { Icon(interest.icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) },
                        )
                    }
                }
                state.errors[PlanField.INTERESTS]?.let { ErrorText(it) }
            }

            InfoBanner("Phase 1 preview: itineraries are generated from local mock data, not an AI service.")
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        DateRangeDialog(
            initialStart = state.startDateMillis,
            initialEnd = state.endDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                onDatesChange(start, end)
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

/** Free-text destination input with type-ahead suggestions from the known destinations. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationField(
    state: PlanTripUiState,
    onDestinationTyped: (String) -> Unit,
    onDestinationPicked: (String) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val suggestions = state.suggestions
    val focusManager = LocalFocusManager.current
    val expanded = menuOpen && suggestions.isNotEmpty()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { menuOpen = it }) {
        OutlinedTextField(
            value = state.destination,
            onValueChange = {
                onDestinationTyped(it)
                menuOpen = true
            },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            placeholder = { Text("Where do you want to go?") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
            singleLine = true,
            isError = PlanField.DESTINATION in state.errors,
            supportingText = state.errors[PlanField.DESTINATION]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { menuOpen = false }) {
            suggestions.forEach { destination ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(destination.name, style = MaterialTheme.typography.bodyLarge)
                            if (destination.region.isNotEmpty()) {
                                Text(
                                    destination.region,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    onClick = {
                        onDestinationPicked(destination.name)
                        menuOpen = false
                        focusManager.clearFocus()
                    },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
            Text(
                "Search data © OpenStreetMap contributors",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp).semantics { error(message) },
    )
}

@Composable
private fun DatesField(state: PlanTripUiState, onClick: () -> Unit) {
    val error = state.errors[PlanField.DATES]
    OutlinedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            DateColumn("Start", state.startDateMillis, Modifier.weight(1f))
            DateColumn("End", state.endDateMillis, Modifier.weight(1f))
        }
    }
    error?.let { ErrorText(it) }
}

@Composable
private fun DateColumn(label: String, millis: Long?, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(millis?.let { formatDate(it, "EEE, d MMM") } ?: "Select", style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    initialStart: Long?,
    initialEnd: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?, Long?) -> Unit,
) {
    val today = remember { todayUtcMillis() }
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart,
        initialSelectedEndDateMillis = initialEnd,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= today
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pickerState.selectedStartDateMillis, pickerState.selectedEndDateMillis) },
                enabled = pickerState.selectedStartDateMillis != null && pickerState.selectedEndDateMillis != null,
            ) { Text("Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DateRangePicker(
            state = pickerState,
            modifier = Modifier.weight(1f),
            title = { Text("Select travel dates", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
        )
    }
}

@Composable
private fun BudgetSlider(min: Int, max: Int, onChange: (Int, Int) -> Unit) {
    val floor = PlanTripViewModel.BUDGET_FLOOR.toFloat()
    val ceiling = PlanTripViewModel.BUDGET_CEILING.toFloat()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text("${formatInrCompact(min)} – ${formatInrCompact(max)}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text("for the group", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RangeSlider(
                value = min.toFloat()..max.toFloat(),
                onValueChange = { range ->
                    fun snap(v: Float) = ((v / 5_000f).roundToInt() * 5_000)
                    val start = snap(range.start).coerceAtMost(ceiling.toInt() - 5_000)
                    onChange(start, snap(range.endInclusive).coerceAtLeast(start + 5_000))
                },
                valueRange = floor..ceiling,
            )
            Row {
                Text(formatInrCompact(floor.toInt()), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(formatInrCompact(ceiling.toInt()), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun GeneratingState(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(Modifier.size(56.dp), strokeWidth = 5.dp)
        Spacer(Modifier.height(24.dp))
        Text("Crafting your itinerary…", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Matching activities to your interests and budget (mock data).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        AssistChip(onClick = {}, label = { Text("No AI service is called in Phase 1") }, enabled = false)
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun PlanTripPreview() {
    TripPilotAITheme {
        PlanTripContent(
            state = PlanTripUiState(
                destination = "Goa",
                errors = mapOf(PlanField.DATES to "Select your start and end dates"),
            ),
            onDestinationTyped = {}, onDestinationPicked = {}, onDatesChange = { _, _ -> }, onTravellersChange = {},
            onBudgetChange = { _, _ -> }, onInterestToggle = {}, onGenerate = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanTripGeneratingPreview() {
    TripPilotAITheme {
        PlanTripContent(
            state = PlanTripUiState(isGenerating = true),
            onDestinationTyped = {}, onDestinationPicked = {}, onDatesChange = { _, _ -> }, onTravellersChange = {},
            onBudgetChange = { _, _ -> }, onInterestToggle = {}, onGenerate = {},
        )
    }
}
