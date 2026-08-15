package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thelightphone.lp3Keyboard.ui.CapsLockedLayout
import com.thelightphone.lp3Keyboard.ui.DefaultLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.EmojiLayout
import com.thelightphone.lp3Keyboard.ui.ExtendedCharKeyboard
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.LowerCaseLayout
import com.thelightphone.lp3Keyboard.ui.NumberLayout
import com.thelightphone.lp3Keyboard.ui.SymbolsLayout
import com.thelightphone.lp3Keyboard.ui.UpperCaseLayout
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightTextInputEditor
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.keyboard.TextInputKeyboardCallback
import java.util.UUID

class NumberInputScreen(
    sealedActivity: SealedLightActivity,
    private val title: String,
    private val initialValue: String,
) : SimpleLightScreen<String>(sealedActivity) {

    private val editorKey = UUID.randomUUID().toString()

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val textState = rememberTextFieldState(initialValue)
        val keyboardOptionsFlow = rememberKeyboardOptions()
        val currentOnSubmit by rememberUpdatedState<(CharSequence) -> Unit> { goBack(it.toString().trim()) }

        val callback = remember(textState) {
            TextInputKeyboardCallback(
                state = textState,
                singleLine = true,
                onReturn = { currentOnSubmit(textState.text) },
            )
        }

        val keyboardVm = viewModel<DefaultLp3KeyboardViewModel>(
            key = "NumberInput-$editorKey",
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DefaultLp3KeyboardViewModel(
                        callback,
                        keyboardOptionsFlow = keyboardOptionsFlow,
                        initialLayout = NumberLayout,
                        optionsForLayout = {
                            LayoutOptions(it is EmojiLayout || it is ExtendedCharKeyboard)
                        },
                    ) as T
                }
            },
        )

        LightTheme(colors = themeColors) {
            LightTextInputEditor(
                title = title,
                state = textState,
                onSubmit = { result: CharSequence -> goBack(result.toString().trim()) },
                onBack = { goBack(null) },
                viewModel = keyboardVm,
                submitLabel = "DONE",
                singleLine = true,
                modifier = Modifier.background(LightThemeTokens.colors.background),
            )
        }
    }
}
