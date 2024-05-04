package com.example.exchangerates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//класс отправляет данные на экран когда готовы вью фрагмента
class CurrencyViewModel: ViewModel() {
    val liveDataCurrent = MutableLiveData<Currency>()
    val liveDataConvert = MutableLiveData<CurrencyConvert>()
}