package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.CategoryGroup
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.util.currentYearMonth
import com.tyshi00.ledger.util.formatCents
import com.tyshi00.ledger.util.monthLabel
import com.tyshi00.ledger.util.parseCentsFromInput
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

data class BudgetSetupState(
    val budgets: Map<CategoryGroup, Long> = emptyMap(),
    val currency: String = "$",
    val year: Int = 0,
    val month: Int = 0,
)

class BudgetSetupViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    private val _state = MutableStateFlow(BudgetSetupState())
    val state: StateFlow<BudgetSetupState> = _state.asStateFlow()

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        val (y, m) = currentYearMonth()
        _state.value = _state.value.copy(year = y, month = m)
        reload()
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            val map = mutableMapOf<CategoryGroup, Long>()
            CategoryGroup.entries.forEach { group ->
                map[group] = repo.getBudgetForGroup(group, s.year, s.month)
            }
            _state.value = s.copy(budgets = map, currency = repo.getCurrencySymbol())
        }
    }

    fun saveBudget(group: CategoryGroup, amountText: String) {
        val cents = parseCentsFromInput(amountText) ?: 0L
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            repo.setBudgetForGroup(group, s.year, s.month, cents)
            reload()
        }
    }
}

class BudgetSetupScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Unit, BudgetSetupViewModel>(sealedActivity) {

    override val viewModelClass: Class<BudgetSetupViewModel>
        get() = BudgetSetupViewModel::class.java

    override fun createViewModel() = BudgetSetupViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.state.collectAsState()

        LightTheme(colors = themeColors) {
            PinLockGate {
            LightScrollView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(
                        icon = LightIcons.BACK,
                        onClick = { goBack() },
                    ),
                    center = LightTopBarCenter.Text(
                        if (state.year > 0) "Budgets" else "Budgets"
                    ),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp())
                        ,
                ) {
                    CategoryGroup.entries.forEach { group ->
                        val current = state.budgets[group] ?: 0L

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val initial = if (current > 0) (current / 100).toString() else ""
                                    navigateTo(
                                        screenFactory = {
                                            NumberInputScreen(it, group.label, initial)
                                        },
                                        resultCallback = { result ->
                                            if (result != null) {
                                                viewModel.saveBudget(group, result)
                                            }
                                        },
                                    )
                                }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(
                                text = group.label,
                                variant = LightTextVariant.Copy,
                                modifier = Modifier.weight(1f),
                            )
                            LightText(
                                text = if (current > 0) formatCents(current, state.currency) else "Not set",
                                variant = LightTextVariant.Copy,
                                lighten = current == 0L,
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
