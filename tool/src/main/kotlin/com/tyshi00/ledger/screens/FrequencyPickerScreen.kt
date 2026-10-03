package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.thelightphone.sdk.ui.lightClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tyshi00.ledger.R
import com.tyshi00.ledger.data.Frequency
import com.tyshi00.ledger.util.LedgerLockManager
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

class FrequencyPickerScreen(
    sealedActivity: SealedLightActivity,
    private val current: Frequency,
) : SimpleLightScreen<Frequency>(sealedActivity) {

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            PinLockGate {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(null) }),
                    center = LightTopBarCenter.Text("Frequency"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                    Frequency.entries.forEach { freq ->
                        val isSelected = freq == current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .lightClickable { goBack(freq) }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Circle with checkmark for selected
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
                                LightIcon(icon = LightIcons.CIRCLE)
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check_tick),
                                        contentDescription = "selected",
                                        tint = LightThemeTokens.colors.content,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(0.75f.gridUnitsAsDp()))
                            LightText(text = freq.label, variant = LightTextVariant.Heading)
                        }
                    }
                }
            }
            }
        }
    }
}
