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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tyshi00.ledger.util.MONTH_NAMES
import com.tyshi00.ledger.util.currentYearMonth
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcon
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

class MonthPickerScreen(
    sealedActivity: SealedLightActivity,
) : SimpleLightScreen<Pair<Int, Int>>(sealedActivity) {

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val (currentYear, _) = currentYearMonth()
        var selectedYear by remember { mutableStateOf(currentYear) }

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(null) }),
                    center = LightTopBarCenter.Text("Select Month"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp()),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LightIcon(icon = LightIcons.BACK, modifier = Modifier.clickable { selectedYear-- })
                    LightText(text = selectedYear.toString(), variant = LightTextVariant.Subheading)
                    LightIcon(icon = LightIcons.ARROW_RIGHT, modifier = Modifier.clickable { selectedYear++ })
                }

                LightScrollView(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    MONTH_NAMES.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { goBack(Pair(selectedYear, index + 1)) }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(text = name, variant = LightTextVariant.Copy)
                        }
                    }
                }
            }
        }
    }
}
