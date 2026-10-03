package com.tyshi00.ledger.screens

import android.util.Log
import androidx.compose.foundation.background
import com.thelightphone.sdk.ui.lightClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.R
import com.tyshi00.ledger.data.EntryType
import com.tyshi00.ledger.data.LedgerDatabase
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.util.LedgerLockManager
import com.tyshi00.ledger.util.currentYearMonth
import com.tyshi00.ledger.util.formatCents
import com.tyshi00.ledger.util.monthLabel
import com.tyshi00.ledger.util.nextMonth
import com.tyshi00.ledger.util.previousMonth
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.buildDatabase
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightSurfaceScheme
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
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

data class HomeState(
    val year: Int = 0, val month: Int = 0,
    val incomeTotal: Long = 0, val incomeBudget: Long = 0,
    val fixedTotal: Long = 0, val fixedBudget: Long = 0,
    val variableTotal: Long = 0, val variableBudget: Long = 0,
    val debtTotal: Long = 0, val debtBudget: Long = 0,
    val currency: String = "$",
)

class HomeViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    private var yearMonthInitialized = false

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        // Initialize the global lock manager (idempotent)
        LedgerLockManager.init(repo)

        viewModelScope.launch(Dispatchers.IO) {
            val invert = repo.getInvertColors()
            if (invert) LightThemeController.setLightTheme() else LightThemeController.setDarkTheme()

            if (!yearMonthInitialized) {
                yearMonthInitialized = true
                val (y, m) = currentYearMonth()
                _state.value = _state.value.copy(year = y, month = m)
            }

            LedgerLockManager.refreshPinState()
            reload()
        }
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            if (s.year == 0) return@launch
            val currency = repo.getCurrencySymbol()
            _state.value = s.copy(
                incomeTotal = repo.sumByType(s.year, s.month, EntryType.INCOME),
                incomeBudget = repo.totalBudgetByType(EntryType.INCOME, s.year, s.month),
                fixedTotal = repo.sumByType(s.year, s.month, EntryType.FIXED),
                fixedBudget = repo.totalBudgetByType(EntryType.FIXED, s.year, s.month),
                variableTotal = repo.sumByType(s.year, s.month, EntryType.VARIABLE),
                variableBudget = repo.totalBudgetByType(EntryType.VARIABLE, s.year, s.month),
                debtTotal = repo.sumByType(s.year, s.month, EntryType.DEBT),
                debtBudget = repo.totalBudgetByType(EntryType.DEBT, s.year, s.month),
                currency = currency,
            )
        }
    }

    fun setMonth(year: Int, month: Int) { _state.value = _state.value.copy(year = year, month = month); reload() }
    fun prevMonth() { val (y, m) = previousMonth(_state.value.year, _state.value.month); setMonth(y, m) }
    fun nextMonth() { val (y, m) = nextMonth(_state.value.year, _state.value.month); setMonth(y, m) }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, HomeViewModel>(sealedActivity) {

    private val repo by lazy {
        LedgerRepository.getInstance {
            lightContext.buildDatabase(LedgerDatabase::class.java, "ledger.db")
        }
    }

    constructor(sealedActivity: SealedLightActivity, repo: LedgerRepository) : this(sealedActivity)

    override val viewModelClass: Class<HomeViewModel>
        get() = HomeViewModel::class.java
    override fun createViewModel() = HomeViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.state.collectAsState()

        LightTheme(colors = themeColors) {
            // PinLockGate overlays PIN pad when app is locked
            PinLockGate {
                val isDark = LightThemeTokens.surfaceScheme == LightSurfaceScheme.Dark
                val calendarRes = if (isDark) R.drawable.ic_calendar_white else R.drawable.ic_calendar_black

                Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                    LightTopBar(center = LightTopBarCenter.Text("Ledger"), modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp()),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightIcon(icon = LightIcons.BACK, modifier = Modifier.lightClickable { viewModel.prevMonth() })
                        LightText(text = if (state.year > 0) monthLabel(state.year, state.month) else "", variant = LightTextVariant.Subheading)
                        LightIcon(icon = LightIcons.ARROW_RIGHT, modifier = Modifier.lightClickable { viewModel.nextMonth() })
                    }

                    Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))

                    LightScrollView(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                        BudgetRow("(+) Income", state.incomeTotal, state.incomeBudget, state.currency) {
                            navigateTo(screenFactory = { HistoryScreen(it, repo, state.year, state.month, EntryType.INCOME) }, resultCallback = { viewModel.reload() })
                        }
                        BudgetRow("(-) Fixed Expenses", state.fixedTotal, state.fixedBudget, state.currency) {
                            navigateTo(screenFactory = { HistoryScreen(it, repo, state.year, state.month, EntryType.FIXED) }, resultCallback = { viewModel.reload() })
                        }
                        BudgetRow("(-) Variable Expenses", state.variableTotal, state.variableBudget, state.currency) {
                            navigateTo(screenFactory = { HistoryScreen(it, repo, state.year, state.month, EntryType.VARIABLE) }, resultCallback = { viewModel.reload() })
                        }
                        BudgetRow("(-) Debts", state.debtTotal, state.debtBudget, state.currency) {
                            navigateTo(screenFactory = { HistoryScreen(it, repo, state.year, state.month, EntryType.DEBT) }, resultCallback = { viewModel.reload() })
                        }
                        Spacer(modifier = Modifier.height(1f.gridUnitsAsDp()))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 0.5f.gridUnitsAsDp()), horizontalArrangement = Arrangement.SpaceBetween) {
                            val rem = state.incomeTotal - state.fixedTotal - state.variableTotal - state.debtTotal
                            LightText(text = "Remaining", variant = LightTextVariant.Subheading)
                            LightText(text = formatCents(rem, state.currency), variant = LightTextVariant.Subheading)
                        }
                    }

                    LightBottomBar(items = listOf(
                        LightBarButton.LightIcon(icon = LightIcons.SETTINGS, onClick = {
                            navigateTo(screenFactory = { SettingsScreen(it, repo) }, resultCallback = { viewModel.reload() })
                        }),
                        LightBarButton.LightIcon(icon = LightIcons.ADD, onClick = {
                            navigateTo(screenFactory = { AddEntryScreen(it, repo, state.year, state.month) }, resultCallback = { viewModel.reload() })
                        }),
                        LightBarButton.Icon(painter = painterResource(calendarRes), onClick = {
                            navigateTo(screenFactory = { MonthPickerScreen(it) }, resultCallback = { r -> if (r != null) viewModel.setMonth(r.first, r.second) })
                        }, contentDescription = "Select month"),
                        LightBarButton.LightIcon(icon = LightIcons.LIST, onClick = {
                            navigateTo(screenFactory = { HistoryScreen(it, repo, state.year, state.month, null) }, resultCallback = { viewModel.reload() })
                        }),
                    ))
                }
            }
        }
    }
}

@Composable
private fun BudgetRow(label: String, spent: Long, budget: Long, currency: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().lightClickable { onClick() }.padding(vertical = 0.75f.gridUnitsAsDp())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            LightText(text = label, variant = LightTextVariant.Copy)
            LightText(text = formatCents(spent, currency), variant = LightTextVariant.Copy)
        }
        if (budget > 0) {
            val frac = (spent.toFloat() / budget.toFloat()).coerceIn(0f, 1.5f)
            val rem = budget - spent
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(LightThemeTokens.colors.content.copy(alpha = 0.1f))) {
                Box(modifier = Modifier.fillMaxWidth(frac.coerceAtMost(1f)).height(4.dp).background(
                    if (frac > 1f) LightThemeTokens.colors.content.copy(alpha = 0.8f) else LightThemeTokens.colors.content.copy(alpha = 0.4f)
                ))
            }
            Spacer(modifier = Modifier.height(2.dp))
            LightText(
                text = if (rem >= 0) "${formatCents(rem, currency)} of ${formatCents(budget, currency)} left"
                else "${formatCents(-rem, currency)} over ${formatCents(budget, currency)} budget",
                variant = LightTextVariant.Fine, lighten = true,
            )
        }
    }
}
