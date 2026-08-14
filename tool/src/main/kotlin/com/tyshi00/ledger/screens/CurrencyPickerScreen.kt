package com.tyshi00.ledger.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.tyshi00.ledger.R
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
import kotlinx.coroutines.launch

data class CurrencyOption(val symbol: String, val name: String)
data class CurrencyRegion(val label: String, val currencies: List<CurrencyOption>)

private val REGIONS = listOf(
    CurrencyRegion("North America", listOf(
        CurrencyOption("$", "US Dollar"),
        CurrencyOption("C$", "Canadian Dollar"),
        CurrencyOption("MX$", "Mexican Peso"),
    )),
    CurrencyRegion("Central America & Caribbean", listOf(
        CurrencyOption("Q", "Guatemalan Quetzal"),
        CurrencyOption("L", "Honduran Lempira"),
        CurrencyOption("NIO", "Nicaraguan Cordoba"),
        CurrencyOption("CRC", "Costa Rican Colon"),
        CurrencyOption("B/.", "Panamanian Balboa"),
        CurrencyOption("J$", "Jamaican Dollar"),
        CurrencyOption("TT$", "Trinidad & Tobago Dollar"),
        CurrencyOption("RD$", "Dominican Peso"),
        CurrencyOption("BSD", "Bahamian Dollar"),
        CurrencyOption("BBD", "Barbadian Dollar"),
    )),
    CurrencyRegion("South America", listOf(
        CurrencyOption("R$", "Brazilian Real"),
        CurrencyOption("ARS", "Argentine Peso"),
        CurrencyOption("CLP", "Chilean Peso"),
        CurrencyOption("COP", "Colombian Peso"),
        CurrencyOption("PEN", "Peruvian Sol"),
        CurrencyOption("UYU", "Uruguayan Peso"),
        CurrencyOption("BOB", "Bolivian Boliviano"),
        CurrencyOption("PYG", "Paraguayan Guarani"),
        CurrencyOption("VES", "Venezuelan Bolivar"),
        CurrencyOption("GYD", "Guyanese Dollar"),
        CurrencyOption("SRD", "Surinamese Dollar"),
    )),
    CurrencyRegion("Europe", listOf(
        CurrencyOption("EUR", "Euro"),
        CurrencyOption("GBP", "British Pound"),
        CurrencyOption("CHF", "Swiss Franc"),
        CurrencyOption("SEK", "Swedish Krona"),
        CurrencyOption("NOK", "Norwegian Krone"),
        CurrencyOption("DKK", "Danish Krone"),
        CurrencyOption("PLN", "Polish Zloty"),
        CurrencyOption("CZK", "Czech Koruna"),
        CurrencyOption("HUF", "Hungarian Forint"),
        CurrencyOption("RON", "Romanian Leu"),
        CurrencyOption("BGN", "Bulgarian Lev"),
        CurrencyOption("TRY", "Turkish Lira"),
        CurrencyOption("ISK", "Icelandic Krona"),
    )),
    CurrencyRegion("Asia & Pacific", listOf(
        CurrencyOption("JPY", "Japanese Yen"),
        CurrencyOption("KRW", "South Korean Won"),
        CurrencyOption("NT$", "Taiwan Dollar"),
        CurrencyOption("S$", "Singapore Dollar"),
        CurrencyOption("RM", "Malaysian Ringgit"),
        CurrencyOption("INR", "Indian Rupee"),
        CurrencyOption("PHP", "Philippine Peso"),
        CurrencyOption("THB", "Thai Baht"),
        CurrencyOption("IDR", "Indonesian Rupiah"),
        CurrencyOption("VND", "Vietnamese Dong"),
        CurrencyOption("HK$", "Hong Kong Dollar"),
        CurrencyOption("A$", "Australian Dollar"),
        CurrencyOption("NZ$", "New Zealand Dollar"),
    )),
    CurrencyRegion("Africa", listOf(
        CurrencyOption("ZAR", "South African Rand"),
        CurrencyOption("NGN", "Nigerian Naira"),
        CurrencyOption("KES", "Kenyan Shilling"),
        CurrencyOption("GHS", "Ghanaian Cedi"),
        CurrencyOption("EGP", "Egyptian Pound"),
        CurrencyOption("MAD", "Moroccan Dirham"),
        CurrencyOption("TZS", "Tanzanian Shilling"),
        CurrencyOption("UGX", "Ugandan Shilling"),
        CurrencyOption("ETB", "Ethiopian Birr"),
        CurrencyOption("XOF", "West African CFA"),
        CurrencyOption("XAF", "Central African CFA"),
        CurrencyOption("MUR", "Mauritian Rupee"),
        CurrencyOption("BWP", "Botswana Pula"),
        CurrencyOption("RWF", "Rwandan Franc"),
    )),
    CurrencyRegion("Middle East", listOf(
        CurrencyOption("AED", "UAE Dirham"),
        CurrencyOption("SAR", "Saudi Riyal"),
        CurrencyOption("ILS", "Israeli Shekel"),
        CurrencyOption("QAR", "Qatari Riyal"),
        CurrencyOption("KWD", "Kuwaiti Dinar"),
        CurrencyOption("BHD", "Bahraini Dinar"),
        CurrencyOption("OMR", "Omani Rial"),
        CurrencyOption("JOD", "Jordanian Dinar"),
    )),
)

class CurrencyPickerViewModel(private val repo: LedgerRepository) : LightViewModel<Unit>() {
    val current = MutableStateFlow("$")
    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        viewModelScope.launch(Dispatchers.IO) { current.value = repo.getCurrencySymbol() }
    }
    fun select(symbol: String) {
        viewModelScope.launch(Dispatchers.IO) { repo.setCurrencySymbol(symbol); current.value = symbol }
    }
}

class CurrencyPickerScreen(
    sealedActivity: SealedLightActivity,
    private val repo: LedgerRepository,
) : LightScreen<Unit, CurrencyPickerViewModel>(sealedActivity) {

    override val viewModelClass: Class<CurrencyPickerViewModel>
        get() = CurrencyPickerViewModel::class.java
    override fun createViewModel() = CurrencyPickerViewModel(repo)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val current by viewModel.current.collectAsState()

        // Find which region contains the current selection and auto-expand it
        val currentRegion = REGIONS.firstOrNull { region -> region.currencies.any { it.symbol == current } }
        var expanded by remember { mutableStateOf(setOfNotNull(currentRegion?.label)) }

        LightTheme(colors = themeColors) {
            Column(modifier = Modifier.fillMaxSize().background(LightThemeTokens.colors.background)) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text("Currency"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )
                LightScrollView(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1f.gridUnitsAsDp())) {
                    REGIONS.forEach { region ->
                        val isExpanded = region.label in expanded

                        // Collapsible region header
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    expanded = if (isExpanded) expanded - region.label else expanded + region.label
                                }
                                .padding(vertical = 0.75f.gridUnitsAsDp()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LightText(text = region.label, variant = LightTextVariant.Subheading, modifier = Modifier.weight(1f))
                            LightIcon(icon = if (isExpanded) LightIcons.UP else LightIcons.DOWN)
                        }

                        if (isExpanded) {
                            region.currencies.forEach { option ->
                                val isSelected = option.symbol == current
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { viewModel.select(option.symbol) }
                                        .padding(
                                            start = 0.5f.gridUnitsAsDp(),
                                            top = 0.5f.gridUnitsAsDp(),
                                            bottom = 0.5f.gridUnitsAsDp(),
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
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
                                    LightText(text = option.name, variant = LightTextVariant.Copy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
