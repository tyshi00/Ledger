package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.CategoryGroup
import com.tyshi00.ledger.data.LedgerRepository
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
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

data class AddCustomState(
    val label: String = "",
    val saved: Boolean = false,
)

class AddCustomCategoryViewModel(
    private val repo: LedgerRepository,
    private val group: CategoryGroup,
) : LightViewModel<Unit>() {

    private val _state = MutableStateFlow(AddCustomState())
    val state: StateFlow<AddCustomState> = _state.asStateFlow()

    fun setLabel(text: String) {
        _state.value = _state.value.copy(label = text)
    }

    fun save() {
        val label = _state.value.label.trim()
        if (label.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repo.addCustomCategory(label, group)
            _state.value = _state.value.copy(saved = true)
        }
    }
}

class AddCustomCategoryScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
    private val group: CategoryGroup,
) : LightScreen<Unit, AddCustomCategoryViewModel>(sealedActivity) {

    override val viewModelClass: Class<AddCustomCategoryViewModel>
        get() = AddCustomCategoryViewModel::class.java

    override fun createViewModel() = AddCustomCategoryViewModel(repo, group)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.state.collectAsState()

        if (state.saved) {
            goBack(Unit)
            return
        }

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(
                        icon = LightIcons.BACK,
                        onClick = { goBack(null) },
                    ),
                    center = LightTopBarCenter.Text("Add to ${group.label}"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    LightTextField(
                        label = "Category name",
                        value = state.label,
                        placeholder = "Tap to enter name",
                        onClick = {
                            navigateTo(
                                screenFactory = {
                                    TextInputScreen(it, "Category Name", state.label)
                                },
                                resultCallback = { result ->
                                    if (result != null) viewModel.setLabel(result)
                                },
                            )
                        },
                    )
                }

                LightBottomBar(
                    items = listOf(
                        LightBarButton.Text(
                            text = "SAVE",
                            onClick = { viewModel.save() },
                        ),
                    ),
                )
            }
        }
    }
}
