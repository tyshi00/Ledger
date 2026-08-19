package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.EntryType
import com.tyshi00.ledger.data.Frequency
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.util.formatCents
import com.tyshi00.ledger.util.monthLabel
import com.tyshi00.ledger.util.LedgerLockManager
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.tyshi00.ledger.screens.PinLockGate
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryEntry(
    val id: Long, val label: String, val amountCents: Long,
    val frequency: String, val note: String?,
    val day: Int, val month: Int, val year: Int, val type: String,
)

data class HistoryState(
    val entries: List<HistoryEntry> = emptyList(),
    val currency: String = "$",
    val totalCents: Long = 0,
)

class HistoryViewModel(
    private val repo: LedgerRepository, private val year: Int,
    private val month: Int, private val typeFilter: EntryType?,
) : LightViewModel<Boolean>() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    override fun onScreenShow(screen: SimpleLightScreen<Boolean>) { reload() }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            val raw = if (typeFilter != null) repo.getEntriesByMonthAndType(year, month, typeFilter)
            else repo.getEntriesByMonth(year, month)
            val resolved = raw.map { entry ->
                HistoryEntry(
                    id = entry.id, label = repo.resolveCategoryLabel(entry.categoryId),
                    amountCents = entry.amountCents, frequency = entry.frequency,
                    note = entry.note, day = entry.day, month = entry.month,
                    year = entry.year, type = entry.type,
                )
            }
            _state.value = HistoryState(entries = resolved, currency = repo.getCurrencySymbol(), totalCents = resolved.sumOf { it.amountCents })
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repo.deleteEntry(id); reload() }
    }
}

class HistoryScreen(
    sealedActivity: SealedLightActivity, private val repo: LedgerRepository,
    private val year: Int, private val month: Int, private val typeFilter: EntryType?,
) : LightScreen<Boolean, HistoryViewModel>(sealedActivity) {

    override val viewModelClass: Class<HistoryViewModel>
        get() = HistoryViewModel::class.java
    override fun createViewModel() = HistoryViewModel(repo, year, month, typeFilter)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.state.collectAsState()
        val title = typeFilter?.label ?: monthLabel(year, month)

        LightTheme(colors = themeColors) {
            PinLockGate {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(true) }),
                    center = LightTopBarCenter.Text(title),
                    modifier = Modifier.padding(bottom = 0.5f.gridUnitsAsDp()),
                )

                if (state.entries.isEmpty()) {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                        LightText(text = "No entries yet", variant = LightTextVariant.Copy, lighten = true,
                            modifier = Modifier.padding(top = 1f.gridUnitsAsDp()))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp(), vertical = 0.5f.gridUnitsAsDp()),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        LightText(text = "Total", variant = LightTextVariant.Detail, lighten = true)
                        LightText(text = formatCents(state.totalCents, state.currency), variant = LightTextVariant.Detail, lighten = true)
                    }

                    LightScrollView(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                        var lastDay = -1
                        state.entries.forEach { entry ->
                            if (entry.day != lastDay) {
                                lastDay = entry.day
                                Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))
                                LightText(text = "${entry.month}/${entry.day}", variant = LightTextVariant.Detail, lighten = true)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        // Confirm before deleting
                                        navigateTo(
                                            screenFactory = {
                                                ConfirmScreen(
                                                    it,
                                                    message = "Delete ${entry.label} (${formatCents(entry.amountCents, state.currency)})?",
                                                    confirmLabel = "CONFIRM",
                                                )
                                            },
                                            resultCallback = { confirmed ->
                                                if (confirmed == true) viewModel.deleteEntry(entry.id)
                                            },
                                        )
                                    }
                                    .padding(vertical = 0.4f.gridUnitsAsDp()),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    LightText(text = entry.label, variant = LightTextVariant.Copy)
                                    val freqLabel = Frequency.entries.firstOrNull { it.name == entry.frequency }?.label ?: ""
                                    val parts = mutableListOf<String>()
                                    if (freqLabel.isNotEmpty()) parts.add(freqLabel)
                                    if (!entry.note.isNullOrBlank()) parts.add(entry.note)
                                    if (parts.isNotEmpty()) {
                                        LightText(text = parts.joinToString(" \u00B7 "), variant = LightTextVariant.Fine, lighten = true)
                                    }
                                }
                                LightText(text = formatCents(entry.amountCents, state.currency), variant = LightTextVariant.Copy)
                            }
                        }
                        Spacer(modifier = Modifier.height(1f.gridUnitsAsDp()))
                    }
                }
            }
            }
        }
    }
}
