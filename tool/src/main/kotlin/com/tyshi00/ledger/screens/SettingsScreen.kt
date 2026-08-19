package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.util.LedgerLockManager
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcon
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
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    val invertColors = MutableStateFlow(false)
    val pinEnabled = MutableStateFlow(false)
    val currency = MutableStateFlow("$")

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        viewModelScope.launch(Dispatchers.IO) {
            invertColors.value = repo.getInvertColors()
            pinEnabled.value = repo.isPinEnabled()
            currency.value = repo.getCurrencySymbol()
        }
    }

    fun toggleInvertColors() {
        viewModelScope.launch(Dispatchers.IO) {
            val newValue = !invertColors.value
            repo.setInvertColors(newValue)
            invertColors.value = newValue
            if (newValue) LightThemeController.setLightTheme() else LightThemeController.setDarkTheme()
        }
    }
}

class SettingsScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Unit, SettingsViewModel>(sealedActivity) {

    override val viewModelClass: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java

    override fun createViewModel() = SettingsViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val invertColors by viewModel.invertColors.collectAsState()
        val pinEnabled by viewModel.pinEnabled.collectAsState()
        val currency by viewModel.currency.collectAsState()

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
                    center = LightTopBarCenter.Text("Settings"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp())
                        ,
                ) {
                    // Invert colors toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleInvertColors() }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightIcon(
                            icon = if (invertColors) LightIcons.TOGGLE_OFF else LightIcons.TOGGLE_ON,
                        )
                        Spacer(modifier = Modifier.width(1f.gridUnitsAsDp()))
                        LightText(
                            text = "Invert colors",
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Categories
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigateTo(screenFactory = { CategoriesScreen(it, repo) })
                            }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightText(
                            text = "Categories",
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.weight(1f),
                        )
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }

                    // Custom Categories
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigateTo(screenFactory = { CustomCategoriesScreen(it, repo) })
                            }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightText(
                            text = "Custom Categories",
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.weight(1f),
                        )
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }

                    // Budget Setup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigateTo(screenFactory = { BudgetSetupScreen(it, repo) })
                            }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightText(
                            text = "Monthly Budgets",
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.weight(1f),
                        )
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }

                    // Currency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigateTo(screenFactory = { CurrencyPickerScreen(it, repo) })
                            }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightText(text = "Currency", variant = LightTextVariant.Copy, modifier = Modifier.weight(1f))
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }

                    // Security
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigateTo(screenFactory = { SecurityScreen(it, repo) })
                            }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LightText(text = "Security", variant = LightTextVariant.Copy)
                            LightText(
                                text = if (pinEnabled) "PIN enabled" else "No PIN",
                                variant = LightTextVariant.Fine,
                                lighten = true,
                            )
                        }
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }
                }
            }
            }
        }
    }
}
