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
import com.tyshi00.ledger.data.CategoryItem
import com.tyshi00.ledger.data.EntryType
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.util.monthLabel
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
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlinx.coroutines.flow.MutableStateFlow

class AddEntryViewModel(private val repo: LedgerRepository) : LightViewModel<Boolean>()

class AddEntryScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
    private val year: Int,
    private val month: Int,
) : LightScreen<Boolean, AddEntryViewModel>(sealedActivity) {

    override val viewModelClass: Class<AddEntryViewModel>
        get() = AddEntryViewModel::class.java
    override fun createViewModel() = AddEntryViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(false) }),
                    center = LightTopBarCenter.Text("Add to ${monthLabel(year, month)}"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    EntryType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigateTo(
                                        screenFactory = { CategoryPickerScreen(it, repo, type) },
                                        resultCallback = { selected ->
                                            if (selected != null) {
                                                navigateTo(
                                                    screenFactory = { AmountEntryScreen(it, repo, type, selected, year, month) },
                                                    resultCallback = { saved -> if (saved == true) goBack(true) },
                                                )
                                            }
                                        },
                                    )
                                }
                                .padding(vertical = 1f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(text = type.label, variant = LightTextVariant.Subheading, modifier = Modifier.weight(1f))
                            
                        }
                    }
                }
            }
        }
    }
}
