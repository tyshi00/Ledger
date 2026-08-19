package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.tyshi00.ledger.util.LedgerLockManager
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.tyshi00.ledger.screens.PinLockGate
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.gridUnitsAsDp

class DeleteCategoryScreen(
    sealedActivity: SealedLightActivity,
    private val categoryName: String,
    private val groupName: String,
) : SimpleLightScreen<String>(sealedActivity) {

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            PinLockGate {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(null) }),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 2f.gridUnitsAsDp()),
                    contentAlignment = Alignment.Center,
                ) {
                    LightText(
                        text = "Delete \"$categoryName\" from $groupName?",
                        variant = LightTextVariant.Subheading,
                        align = TextAlign.Center,
                    )
                }
                LightBottomBar(items = listOf(
                    LightBarButton.Text(text = "CONFIRM", onClick = {
                        navigateTo(
                            screenFactory = { DeleteEntriesScreen(it, categoryName) },
                            resultCallback = { deleteEntries ->
                                goBack(if (deleteEntries == true) "all" else "category")
                            },
                        )
                    }),
                ))
            }
        }
    }
}

class DeleteEntriesScreen(
    sealedActivity: SealedLightActivity,
    private val categoryName: String,
) : SimpleLightScreen<Boolean>(sealedActivity) {

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(false) }),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 2f.gridUnitsAsDp()),
                    contentAlignment = Alignment.Center,
                ) {
                    LightText(
                        text = "Also delete all entries using \"$categoryName\"?",
                        variant = LightTextVariant.Subheading,
                        align = TextAlign.Center,
                    )
                }
                LightBottomBar(items = listOf(
                    LightBarButton.LightIcon(icon = LightIcons.CLOSE, onClick = { goBack(false) }),
                    LightBarButton.LightIcon(icon = LightIcons.ACCEPT, onClick = { goBack(true) }),
                ))
            }
            }
        }
    }
}
