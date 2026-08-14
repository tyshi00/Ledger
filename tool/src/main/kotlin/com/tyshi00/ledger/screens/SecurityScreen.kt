package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.tyshi00.ledger.data.LedgerRepository
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
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
import kotlinx.coroutines.launch

class SecurityViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    val pinEnabled = MutableStateFlow(false)

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        viewModelScope.launch(Dispatchers.IO) {
            pinEnabled.value = repo.isPinEnabled()
        }
    }

    fun removePin() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.clearPin()
            pinEnabled.value = false
        }
    }
}

class SecurityScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Unit, SecurityViewModel>(sealedActivity) {

    override val viewModelClass: Class<SecurityViewModel>
        get() = SecurityViewModel::class.java

    override fun createViewModel() = SecurityViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val pinEnabled by viewModel.pinEnabled.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(
                        icon = LightIcons.BACK,
                        onClick = { goBack() },
                    ),
                    center = LightTopBarCenter.Text("Security"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    LightText(
                        text = if (pinEnabled) "PIN is enabled" else "No PIN set",
                        variant = LightTextVariant.Detail,
                        lighten = true,
                        modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                    )

                    if (!pinEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigateTo(
                                        screenFactory = { PinSetupScreen(it, repo) },
                                        resultCallback = {
                                            viewModel.pinEnabled.value = true
                                        },
                                    )
                                }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(text = "Set PIN", variant = LightTextVariant.Copy)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigateTo(
                                        screenFactory = { PinSetupScreen(it, repo) },
                                        resultCallback = {
                                            viewModel.pinEnabled.value = true
                                        },
                                    )
                                }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(text = "Change PIN", variant = LightTextVariant.Copy)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.removePin() }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(text = "Remove PIN", variant = LightTextVariant.Copy)
                        }
                    }
                }
            }
        }
    }
}
