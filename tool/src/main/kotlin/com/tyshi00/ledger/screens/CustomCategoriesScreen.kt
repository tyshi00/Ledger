package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.CategoryGroup
import com.tyshi00.ledger.data.LedgerRepository
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomCatDisplay(val id: String, val label: String, val groupLabel: String, val groupName: String)

class CustomCategoriesViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    private val _items = MutableStateFlow<List<CustomCatDisplay>>(emptyList())
    val items: StateFlow<List<CustomCatDisplay>> = _items.asStateFlow()

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) { reload() }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            val all = mutableListOf<CustomCatDisplay>()
            CategoryGroup.entries.forEach { group ->
                repo.getCustomCategoriesByGroup(group).forEach { custom ->
                    all.add(CustomCatDisplay(custom.id, custom.label, group.label, group.name))
                }
            }
            _items.value = all
        }
    }

    fun deleteCategoryOnly(id: String) {
        viewModelScope.launch(Dispatchers.IO) { repo.deleteCustomCategory(id); reload() }
    }

    fun deleteCategoryAndEntries(id: String) {
        viewModelScope.launch(Dispatchers.IO) { repo.deleteCategoryAndEntries(id); reload() }
    }

    fun renameCategory(id: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) { repo.renameCustomCategory(id, newName); reload() }
    }
}

class CustomCategoriesScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Unit, CustomCategoriesViewModel>(sealedActivity) {

    override val viewModelClass: Class<CustomCategoriesViewModel>
        get() = CustomCategoriesViewModel::class.java
    override fun createViewModel() = CustomCategoriesViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val items by viewModel.items.collectAsState()

        LightTheme(colors = themeColors) {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text("Custom Categories"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                if (items.isEmpty()) {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                        LightText(text = "No custom categories added yet", variant = LightTextVariant.Copy, lighten = true,
                            modifier = Modifier.padding(top = 1f.gridUnitsAsDp()))
                    }
                } else {
                    LightScrollView(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 0.75f.gridUnitsAsDp()),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    LightText(text = item.label, variant = LightTextVariant.Copy)
                                    LightText(text = item.groupLabel, variant = LightTextVariant.Fine, lighten = true)
                                }

                                // Edit (pencil) icon
                                LightIcon(
                                    icon = LightIcons.PENCIL,
                                    modifier = Modifier.size(22.dp).clickable {
                                        navigateTo(
                                            screenFactory = { TextInputScreen(it, "Rename", item.label) },
                                            resultCallback = { newName ->
                                                if (newName != null && newName.isNotBlank()) {
                                                    viewModel.renameCategory(item.id, newName)
                                                }
                                            },
                                        )
                                    },
                                )

                                Spacer(modifier = Modifier.width(24.dp))

                                // Trash icon
                                LightIcon(
                                    icon = LightIcons.TRASH,
                                    modifier = Modifier.size(22.dp).clickable {
                                        navigateTo(
                                            screenFactory = { DeleteCategoryScreen(it, item.label, item.groupLabel) },
                                            resultCallback = { result ->
                                                when (result) {
                                                    "category" -> viewModel.deleteCategoryOnly(item.id)
                                                    "all" -> viewModel.deleteCategoryAndEntries(item.id)
                                                }
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
