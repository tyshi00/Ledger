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
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.CategoryGroup
import com.tyshi00.ledger.data.CategoryItem
import com.tyshi00.ledger.data.EntryType
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryPickerViewModel(
    private val repo: LedgerRepository,
    private val type: EntryType,
) : LightViewModel<CategoryItem>() {
    private val _categories = MutableStateFlow<Map<CategoryGroup, List<CategoryItem>>>(emptyMap())
    val categories: StateFlow<Map<CategoryGroup, List<CategoryItem>>> = _categories.asStateFlow()

    override fun onScreenShow(screen: SimpleLightScreen<CategoryItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            val groups = CategoryGroup.entries.filter { it.type == type }
            val map = mutableMapOf<CategoryGroup, List<CategoryItem>>()
            for (group in groups) {
                map[group] = repo.getCategoriesForGroup(group)
            }
            _categories.value = map
        }
    }
}

class CategoryPickerScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
    private val type: EntryType,
) : LightScreen<CategoryItem, CategoryPickerViewModel>(sealedActivity) {

    override val viewModelClass: Class<CategoryPickerViewModel>
        get() = CategoryPickerViewModel::class.java
    override fun createViewModel() = CategoryPickerViewModel(repo, type)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val categories by viewModel.categories.collectAsState()

        // Auto-expand if there's only one group (Income, Debts)
        val groups = categories.keys.toList()
        val isSingleGroup = groups.size == 1
        var expandedGroups by remember(isSingleGroup) {
            mutableStateOf(if (isSingleGroup) groups.toSet() else emptySet())
        }

        LightTheme(colors = themeColors) {
            PinLockGate {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(null) }),
                    center = LightTopBarCenter.Text(type.label),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                    categories.forEach { (group, items) ->
                        val isExpanded = group in expandedGroups

                        // Only show group header if there are multiple groups
                        if (!isSingleGroup) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        expandedGroups = if (isExpanded) expandedGroups - group else expandedGroups + group
                                    }
                                    .padding(vertical = 0.75f.gridUnitsAsDp()),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LightText(text = group.label, variant = LightTextVariant.Subheading, modifier = Modifier.weight(1f))
                                LightIcon(icon = if (isExpanded) LightIcons.UP else LightIcons.DOWN)
                            }
                        }

                        if (isExpanded || isSingleGroup) {
                            items.forEach { item ->
                                LightText(
                                    text = item.label,
                                    variant = LightTextVariant.Copy,
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { goBack(item) }
                                        .padding(
                                            start = if (isSingleGroup) 0f.gridUnitsAsDp() else 1f.gridUnitsAsDp(),
                                            top = 0.6f.gridUnitsAsDp(),
                                            bottom = 0.6f.gridUnitsAsDp(),
                                        ),
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
