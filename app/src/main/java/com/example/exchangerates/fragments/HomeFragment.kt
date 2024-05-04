package com.example.exchangerates.fragments

import android.app.DownloadManager.Request
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.exchangerates.Currency
import com.example.exchangerates.CurrencyViewModel
import com.example.exchangerates.R
import com.example.exchangerates.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.DecimalFormat


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val model:CurrencyViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            requestCurrencyData()
            updateCurrentHome()

    }
    //отправляю ссылку на сервер и получаю JSON объект
    private fun requestCurrencyData(){
        val url = "https://v6.exchangerate-api.com/v6/ce3f218c1a3de000df76286f/latest/RUB"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            com.android.volley.Request.Method.GET,
            url,
            {
                result -> parseCur(result)
            },
            {
                error -> Log.d("MyLog", "$error")
            }
        )
        queue.add(request)
    }
    //парсим валюту на домашний фрагмент
    private fun parseCurrencyHome(mainObject:JSONObject){
        val item = Currency(
            mainObject.getJSONObject("conversion_rates").getString("USD"),
            mainObject.getJSONObject("conversion_rates").getString("EUR"),
            mainObject.getJSONObject("conversion_rates").getString("CNY"),
            mainObject.getJSONObject("conversion_rates").getString("JPY"),
            mainObject.getJSONObject("conversion_rates").getString("GBP"),
            mainObject.getJSONObject("conversion_rates").getString("CHF")
        )
        val changeCurrency = changeCurrency(item)
        model.liveDataCurrent.value = changeCurrency
    }
    //изменяю полученные данные с отношения к рублю на отношение к нужной валюте
    private fun changeCurrency(currency: Currency): Currency{
        val decimalFormat = DecimalFormat("1.1111")
        return Currency(
            decimalFormat.format(1/currency.usd.toFloat()).toString(),
            decimalFormat.format(1/currency.eur.toFloat()).toString(),
            decimalFormat.format(1/currency.cny.toFloat()).toString(),
            decimalFormat.format(1/currency.jpy.toFloat()).toString(),
            decimalFormat.format(1/currency.gbp.toFloat()).toString(),
            decimalFormat.format(1/currency.chf.toFloat()).toString(),
        )
    }

    //передаем данные в MutableLiveData и показываем на экране
    private fun updateCurrentHome() = with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            tvUsdRub.text = it.usd
            tvEurRub.text = it.eur
            tvCnyRub.text = it.cny
            tvJpyRub.text = it.jpy
            tvGbpRub.text = it.gbp
            tvChfRub.text = it.chf
        }
    }

    //запускаю парсинг
    private fun parseCur(result:String){
        val mainObject =JSONObject(result)
        parseCurrencyHome(mainObject)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}