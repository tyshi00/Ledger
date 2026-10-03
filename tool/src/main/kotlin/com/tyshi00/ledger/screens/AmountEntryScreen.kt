package com.tyshi00.ledger.screens

import android.util.Log
import androidx.compose.foundation.background
import com.thelightphone.sdk.ui.lightClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.data.CategoryItem
import com.tyshi00.ledger.data.EntryType
import com.tyshi00.ledger.data.Frequency
import com.tyshi00.ledger.data.LedgerRepository
import com.tyshi00.ledger.util.parseCentsFromInput
import com.tyshi00.ledger.util.LedgerLockManager
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
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
import kotlinx.coroutines.launch
import java.util.Calendar

private const val TAG = "LedgerAmount"

class AmountEntryViewModel(
    private val repo: LedgerRepository,
    private val type: EntryType,
    private val category: CategoryItem,
    private val year: Int,
    private val month: Int,
) : LightViewModel<Boolean>() {

    // Use separate flows for each field to avoid race conditions
    val amountText = MutableStateFlow("")
    val noteText = MutableStateFlow("")
    val frequency = MutableStateFlow(Frequency.MONTHLY)
    val currency = MutableStateFlow("$")
    val error = MutableStateFlow<String?>(null)
    val saved = MutableStateFlow(false)

    override fun onScreenShow(screen: SimpleLightScreen<Boolean>) {
        // Only load currency, don't touch other fields
        viewModelScope.launch(Dispatchers.IO) {
            currency.value = repo.getCurrencySymbol()
        }
    }

    fun setAmount(text: String) {
        Log.d(TAG, "setAmount: '$text'")
        amountText.value = text
        error.value = null
    }

    fun setNote(text: String) { noteText.value = text }
    fun setFrequency(freq: Frequency) { frequency.value = freq }

    fun save() {
        val amt = amountText.value
        Log.d(TAG, "save() amountText='$amt'")
        val cents = parseCentsFromInput(amt)
        Log.d(TAG, "save() cents=$cents")
        if (cents == null || cents <= 0) {
            error.value = "Enter a valid amount"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            repo.addEntry(
                amountCents = cents,
                categoryId = category.id,
                type = type,
                frequency = frequency.value,
                note = noteText.value.ifBlank { null },
                year = year, month = month, day = day,
            )
            Log.d(TAG, "Entry saved: $cents cents")
            saved.value = true
        }
    }
}

class AmountEntryScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
    private val type: EntryType,
    private val category: CategoryItem,
    private val year: Int,
    private val month: Int,
) : LightScreen<Boolean, AmountEntryViewModel>(sealedActivity) {

    override val viewModelClass: Class<AmountEntryViewModel>
        get() = AmountEntryViewModel::class.java
    override fun createViewModel() = AmountEntryViewModel(repo, type, category, year, month)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val amountText by viewModel.amountText.collectAsState()
        val noteText by viewModel.noteText.collectAsState()
        val freq by viewModel.frequency.collectAsState()
        val currency by viewModel.currency.collectAsState()
        val error by viewModel.error.collectAsState()
        val saved by viewModel.saved.collectAsState()

        if (saved) { goBack(true); return }

        LightTheme(colors = themeColors) {
            PinLockGate {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack(false) }),
                    center = LightTopBarCenter.Text(category.label),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                    LightTextField(
                        label = "Amount ($currency)",
                        value = amountText,
                        placeholder = "Tap to enter amount",
                        onClick = {
                            navigateTo(
                                screenFactory = { NumberInputScreen(it, "Amount ($currency)", amountText) },
                                resultCallback = { result ->
                                    Log.d(TAG, "NumberInput returned: '$result'")
                                    if (result != null) viewModel.setAmount(result)
                                },
                            )
                        },
                    )

                    error?.let {
                        LightText(text = it, variant = LightTextVariant.Fine, lighten = true,
                            modifier = Modifier.padding(top = 0.25f.gridUnitsAsDp()))
                    }

                    Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))

                    LightText(text = "Frequency", variant = LightTextVariant.Detail, modifier = Modifier.padding(top = 0.5f.gridUnitsAsDp()))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .lightClickable {
                                navigateTo(
                                    screenFactory = { FrequencyPickerScreen(it, freq) },
                                    resultCallback = { if (it != null) viewModel.setFrequency(it) },
                                )
                            }
                            .padding(vertical = 0.75f.gridUnitsAsDp()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightText(text = freq.label, variant = LightTextVariant.Heading, modifier = Modifier.weight(1f))
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }

                    Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))

                    LightTextField(
                        label = "Note (optional)",
                        value = noteText,
                        placeholder = "Tap to add a note",
                        onClick = {
                            navigateTo(
                                screenFactory = { TextInputScreen(it, "Note", noteText) },
                                resultCallback = { if (it != null) viewModel.setNote(it) },
                            )
                        },
                    )
                }

                LightBottomBar(items = listOf(LightBarButton.Text(text = "SAVE", onClick = { viewModel.save() })))
            }
            }
        }
    }
}
