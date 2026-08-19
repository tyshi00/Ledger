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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.CategoryGroup
import com.tyshi00.ledger.data.CategoryItem
import com.tyshi00.ledger.data.DefaultCategory
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.data.toItem
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

data class CategoriesState(
    val hidden: Set<String> = emptySet(),
    val customByGroup: Map<CategoryGroup, List<CategoryItem>> = emptyMap(),
)

class CategoriesViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) { reload() }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            val hidden = repo.getHiddenCategories()
            val customMap = mutableMapOf<CategoryGroup, List<CategoryItem>>()
            CategoryGroup.entries.forEach { group ->
                val custom = repo.getCustomCategoriesByGroup(group)
                    .map { CategoryItem(it.id, it.label, group, isCustom = true) }
                customMap[group] = custom
            }
            _state.value = CategoriesState(hidden = hidden, customByGroup = customMap)
        }
    }

    fun toggleCategory(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repo.getHiddenCategories().toMutableSet()
            if (id in current) current.remove(id) else current.add(id)
            repo.setHiddenCategories(current)
            _state.value = _state.value.copy(hidden = current)
        }
    }
}

class CategoriesScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Unit, CategoriesViewModel>(sealedActivity) {

    override val viewModelClass: Class<CategoriesViewModel>
        get() = CategoriesViewModel::class.java

    override fun createViewModel() = CategoriesViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.state.collectAsState()
        var expandedGroups by remember { mutableStateOf(setOf<CategoryGroup>()) }

        LightTheme(colors = themeColors) {
            PinLockGate {
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
                    center = LightTopBarCenter.Text("Categories"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    CategoryGroup.entries.forEach { group ->
                        val isExpanded = group in expandedGroups

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedGroups = if (isExpanded) expandedGroups - group
                                    else expandedGroups + group
                                }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(
                                text = "${group.label} (${group.type.label})",
                                variant = LightTextVariant.Subheading,
                                modifier = Modifier.weight(1f),
                            )
                            LightIcon(icon = if (isExpanded) LightIcons.UP else LightIcons.DOWN)
                        }

                        if (isExpanded) {
                            // Default categories
                            DefaultCategory.entries.filter { it.group == group }.forEach { cat ->
                                val isHidden = cat.name in state.hidden
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleCategory(cat.name) }
                                        .padding(
                                            start = 1f.gridUnitsAsDp(),
                                            top = 0.5f.gridUnitsAsDp(),
                                            bottom = 0.5f.gridUnitsAsDp(),
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    LightIcon(
                                        icon = if (isHidden) LightIcons.TOGGLE_ON else LightIcons.TOGGLE_OFF,
                                    )
                                    Spacer(modifier = Modifier.width(0.75f.gridUnitsAsDp()))
                                    LightText(
                                        text = cat.label,
                                        variant = LightTextVariant.Copy,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            // Custom categories
                            state.customByGroup[group]?.forEach { custom ->
                                val isHidden = custom.id in state.hidden
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleCategory(custom.id) }
                                        .padding(
                                            start = 1f.gridUnitsAsDp(),
                                            top = 0.5f.gridUnitsAsDp(),
                                            bottom = 0.5f.gridUnitsAsDp(),
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    LightIcon(
                                        icon = if (isHidden) LightIcons.TOGGLE_ON else LightIcons.TOGGLE_OFF,
                                    )
                                    Spacer(modifier = Modifier.width(0.75f.gridUnitsAsDp()))
                                    LightText(
                                        text = custom.label,
                                        variant = LightTextVariant.Copy,
                                        lighten = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            // Add custom
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navigateTo(
                                            screenFactory = { AddCustomCategoryScreen(it, repo, group) },
                                            resultCallback = { viewModel.reload() },
                                        )
                                    }
                                    .padding(
                                        start = 1f.gridUnitsAsDp(),
                                        top = 0.5f.gridUnitsAsDp(),
                                        bottom = 0.5f.gridUnitsAsDp(),
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LightIcon(icon = LightIcons.ADD)
                                Spacer(modifier = Modifier.width(0.75f.gridUnitsAsDp()))
                                LightText(
                                    text = "Add custom",
                                    variant = LightTextVariant.Copy,
                                    lighten = true,
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
