package com.tyshi00.ledger.screens

import android.graphics.Color
import android.view.View
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tyshi00.ledger.util.LedgerLockManager
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightThemeTokens

@Composable
fun PinLockGate(content: @Composable () -> Unit) {
    val isLocked by LedgerLockManager.locked.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content always renders (preserves screen state)
        content()

        // Native black overlay: visibility controlled ONLY by LedgerLockManager
        // (NOT by Compose state — that's the whole point).
        // onPause sets VISIBLE, verifyPin sets GONE. No update block.
        AndroidView(
            factory = { ctx ->
                View(ctx).apply {
                    setBackgroundColor(Color.BLACK)
                    visibility = if (LedgerLockManager.locked.value) View.VISIBLE else View.GONE
                    LedgerLockManager.registerOverlay(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // PIN pad renders on top of the native overlay
        if (isLocked) {
            PinOverlay()
        }
    }
}

@Composable
private fun PinOverlay() {
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightThemeTokens.colors.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        LightText(text = "Enter PIN", variant = LightTextVariant.Heading, modifier = Modifier.padding(bottom = 24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            repeat(4) { i ->
                if (i < entered.length) {
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

        PinKeypad(
            onDigit = { digit ->
                if (entered.length >= 4) return@PinKeypad
                val next = entered + digit
                entered = next
                error = null
                if (next.length == 4) {
                    if (LedgerLockManager.verifyPin(next)) {
                        entered = ""
                        error = null
                        attempts = 0
                    } else {
                        attempts++
                        entered = ""
                        error = if (attempts >= 3) "Too many attempts" else "Incorrect PIN"
                    }
                }
            },
            onBackspace = {
                if (entered.isNotEmpty()) {
                    entered = entered.dropLast(1)
                    error = null
                }
            },
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
