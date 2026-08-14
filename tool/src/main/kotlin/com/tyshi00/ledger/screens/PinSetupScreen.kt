package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.LedgerRepository
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PinSetupViewModel(private val repo: LedgerRepository) : LightViewModel<Boolean>() {
    val entered = MutableStateFlow("")
    val phase = MutableStateFlow("enter")
    val error = MutableStateFlow<String?>(null)
    val done = MutableStateFlow(false)
    private var firstPin = ""

    fun onDigit(digit: String) {
        if (entered.value.length >= 4) return
        entered.value = entered.value + digit
        error.value = null
        if (entered.value.length == 4) {
            if (phase.value == "enter") {
                firstPin = entered.value
                entered.value = ""
                phase.value = "confirm"
            } else {
                if (entered.value == firstPin) {
                    viewModelScope.launch(Dispatchers.IO) { repo.setPin(firstPin); done.value = true }
                } else {
                    entered.value = ""; phase.value = "enter"; firstPin = ""
                    error.value = "PINs didn't match, try again"
                }
            }
        }
    }

    fun onBackspace() {
        if (entered.value.isNotEmpty()) entered.value = entered.value.dropLast(1)
    }
}

class PinSetupScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Boolean, PinSetupViewModel>(sealedActivity) {

    override val viewModelClass: Class<PinSetupViewModel>
        get() = PinSetupViewModel::class.java
    override fun createViewModel() = PinSetupViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val entered by viewModel.entered.collectAsState()
        val phase by viewModel.phase.collectAsState()
        val error by viewModel.error.collectAsState()
        val done by viewModel.done.collectAsState()

        if (done) { goBack(true); return }

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(false) }),
                    center = LightTopBarCenter.Text(if (phase == "enter") "Choose a PIN" else "Confirm PIN"),
                )
                Spacer(modifier = Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    repeat(4) { index ->
                        if (index < entered.length) {
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(LightThemeTokens.colors.content))
                        } else {
                            Box(modifier = Modifier.size(14.dp).border(1.5.dp, LightThemeTokens.colors.content, CircleShape))
                        }
                    }
                }

                error?.let {
                    LightText(text = it, variant = LightTextVariant.Detail, lighten = true, modifier = Modifier.padding(bottom = 16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                PinKeypad(onDigit = { viewModel.onDigit(it) }, onBackspace = { viewModel.onBackspace() })
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
