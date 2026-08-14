package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PinEntryViewModel(private val repo: LedgerRepository) : LightViewModel<Boolean>() {
    val entered = MutableStateFlow("")
    val error = MutableStateFlow<String?>(null)
    val unlocked = MutableStateFlow(false)
    private var attempts = 0

    fun onDigit(digit: String) {
        if (entered.value.length >= 4) return
        entered.value = entered.value + digit
        error.value = null
        if (entered.value.length == 4) verify()
    }

    fun onBackspace() {
        if (entered.value.isNotEmpty()) {
            entered.value = entered.value.dropLast(1)
            error.value = null
        }
    }

    private fun verify() {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.verifyPin(entered.value)) {
                unlocked.value = true
            } else {
                attempts++
                entered.value = ""
                error.value = if (attempts >= 3) "Too many attempts" else "Incorrect PIN"
            }
        }
    }

    override fun onBackPressed(): Boolean = true
}

class PinEntryScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Boolean, PinEntryViewModel>(sealedActivity) {

    override val viewModelClass: Class<PinEntryViewModel>
        get() = PinEntryViewModel::class.java
    override fun createViewModel() = PinEntryViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val entered by viewModel.entered.collectAsState()
        val error by viewModel.error.collectAsState()
        val unlocked by viewModel.unlocked.collectAsState()

        if (unlocked) { goBack(true); return }

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                LightText(text = "Enter PIN", variant = LightTextVariant.Heading, modifier = Modifier.padding(bottom = 24.dp))

                // Hollow circles - outlined when empty, filled when entered
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    repeat(4) { index ->
                        if (index < entered.length) {
                            // Filled dot
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(LightThemeTokens.colors.content))
                        } else {
                            // Hollow circle
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

@Composable
fun PinKeypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"), listOf("4", "5", "6"),
        listOf("7", "8", "9"), listOf("", "0", "\u232B"),
    )
    keys.forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            row.forEach { key ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp).then(
                        if (key.isNotEmpty()) Modifier.clickable {
                            if (key == "\u232B") onBackspace() else onDigit(key)
                        } else Modifier
                    ),
                ) {
                    if (key.isNotEmpty()) LightText(text = key, variant = LightTextVariant.Heading)
                }
            }
        }
    }
}
