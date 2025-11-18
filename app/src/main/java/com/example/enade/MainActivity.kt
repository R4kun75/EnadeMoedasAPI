package com.example.enade

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class ConversionResponse(
    @SerializedName("amount") val amount: Double,
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, Double>
)

interface FrankfurterApi {
    @GET("currencies")
    suspend fun getCurrencies(): Map<String, String>

    @GET("latest")
    suspend fun convert(
        @Query("amount") amount: Double,
        @Query("from") from: String,
        @Query("to") to: String
    ): ConversionResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.frankfurter.app/"

    val api: FrankfurterApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FrankfurterApi::class.java)
    }
}


class CurrencyViewModel : ViewModel() {

    var currencies = mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var conversionResult = mutableStateOf<Double?>(null)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        fetchCurrencies()
    }

    private fun fetchCurrencies() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val list = RetrofitInstance.api.getCurrencies()
                currencies.value = list
            } catch (e: Exception) {
                errorMessage.value = "Erro ao carregar moedas: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun convertCurrency(amount: Double, from: String, to: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                if (from == to) {
                    conversionResult.value = amount
                    return@launch
                }

                val response = RetrofitInstance.api.convert(amount, from, to)
                conversionResult.value = response.rates[to]

            } catch (e: Exception) {
                errorMessage.value = "Erro na conversão: ${e.message}"
                Log.e("CurrencyApp", "Erro", e)
            } finally {
                isLoading.value = false
            }
        }
    }
}

@Composable
fun CurrencyConverterScreen(viewModel: CurrencyViewModel = viewModel()) {

    var amountText by remember { mutableStateOf("") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("BRL") }

    val currencies by remember { viewModel.currencies }
    val result by remember { viewModel.conversionResult }
    val loading by remember { viewModel.isLoading }
    val error by remember { viewModel.errorMessage }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Conversor de Moedas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Atividade Substitutiva - ENADE",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Valor") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CurrencyDropdown(
                label = "De",
                selectedCurrency = fromCurrency,
                currencies = currencies,
                onCurrencySelected = { fromCurrency = it },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )

            CurrencyDropdown(
                label = "Para",
                selectedCurrency = toCurrency,
                currencies = currencies,
                onCurrencySelected = { toCurrency = it },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amountDouble = amountText.toDoubleOrNull()
                if (amountDouble != null) {
                    viewModel.convertCurrency(amountDouble, fromCurrency, toCurrency)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !loading && currencies.isNotEmpty()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("CONVERTER")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(text = error!!, color = Color.Red)
        } else if (result != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Resultado", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = String.format("%.2f %s", result, toCurrency),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    label: String,
    selectedCurrency: String,
    currencies: Map<String, String>,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedCurrency,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, "content description",
                    Modifier.clickable { expanded = !expanded })
            },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false, // Desabilita edição manual, força clique no box
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            val priorityList = listOf("USD", "BRL", "EUR", "GBP", "JPY", "AUD", "CAD")
            val displayList = currencies.keys.filter { it in priorityList } +
                    currencies.keys.filter { it !in priorityList }

            displayList.take(50).forEach { currencyCode ->
                DropdownMenuItem(
                    text = { Text("$currencyCode - ${currencies[currencyCode]}") },
                    onClick = {
                        onCurrencySelected(currencyCode)
                        expanded = false
                    }
                )
            }
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CurrencyConverterScreen()
                }
            }
        }
    }
}