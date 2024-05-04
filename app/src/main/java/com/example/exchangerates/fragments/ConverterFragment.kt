package com.example.exchangerates.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.exchangerates.CurrencyConvert
import com.example.exchangerates.CurrencyJsonData
import com.example.exchangerates.CurrencyViewModel
import com.example.exchangerates.R
import com.example.exchangerates.databinding.FragmentConverterBinding
import com.example.exchangerates.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.URLEncoder


class ConverterFragment : Fragment() {
    private lateinit var binding:FragmentConverterBinding
    private val model: CurrencyViewModel by activityViewModels()
    private lateinit var autoCompleteTextView1: AutoCompleteTextView
    private lateinit var autoCompleteTextView2: AutoCompleteTextView
    private lateinit var currencyAdapter:ArrayAdapter<String>
    private lateinit var editText:EditText
    private lateinit var textView:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentConverterBinding.inflate(inflater, container, false)
        val view = binding.root

        // Загружаю JSON-массив с данными о валютах из папки assets
        val jsonString = requireContext().assets.open("currencies.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)

        // Создаю список названий валют
        val currencyList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()){
            val jsonObject:JSONObject = jsonArray.getJSONObject(i)
            val currencyName = jsonObject.getString("name")
            currencyList.add(currencyName)
        }

        // Инициализирую AutoCompleteTextView и адаптер для них
        currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,currencyList)
        autoCompleteTextView1 = view.findViewById(R.id.val1) ?:
        throw IllegalStateException("AutoCompleteTextView with id val1 not found in the fragment layout")
        autoCompleteTextView1.setAdapter(currencyAdapter)
        autoCompleteTextView2 = view.findViewById(R.id.val2) ?:
        throw IllegalStateException("AutoCompleteTextView with id val2 not found in the fragment layout")
        autoCompleteTextView2.setAdapter(currencyAdapter)
        editText = view.findViewById(R.id.kol1)
        textView = view.findViewById(R.id.kol2)

        // Запросить фокус ввода для AutoCompleteTextView
        autoCompleteTextView1.setOnClickListener{
            autoCompleteTextView1.showDropDown()
        }
        autoCompleteTextView1.onFocusChangeListener = View.OnFocusChangeListener{_, hasFocus ->
            if (hasFocus){
                autoCompleteTextView1.showDropDown()
            }
        }
        autoCompleteTextView2.setOnClickListener{
            autoCompleteTextView2.showDropDown()
        }
        autoCompleteTextView2.onFocusChangeListener = View.OnFocusChangeListener{_, hasFocus ->
            if (hasFocus){
                autoCompleteTextView2.showDropDown()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //запускаю анимацию при нажатии кнопки
        val convertButton:Button = view.findViewById(R.id.bConvert)
        val animView:LottieAnimationView = view.findViewById(R.id.anim)
        convertButton.setOnClickListener {
            animView.playAnimation()
            requestCurrencyConvert()
            //updateConvert()
        }
        //слушатель нажатия для кнопки "очистить"
        val clearButton:Button = view.findViewById(R.id.clearButton) ?:
        throw IllegalStateException("Button with id clearButton not found in the fragment layout")
        clearButton.setOnClickListener {
            autoCompleteTextView1.text = null
            autoCompleteTextView2.text = null
            editText.text = null
            textView.text = null
        }
    }

    // Чтение содержимого файла currencies.json
    private fun readCurJson(context: Context):String{
        val inputStream:InputStream = context.assets.open("currencies.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer)
    }

    // Поиск кода валюты по названию
    private fun findCurrencyCode(currencyName:String, currencies: List<CurrencyJsonData>):String?{
        return currencies.find { it.name == currencyName }?.code
    }

    //отправляю ссылку с нужной парой валют на сервер
    private fun requestCurrencyConvert(){
        val userInput1 = binding.val1.text?.toString()?.trim()?:""
        val encodeInput1 = if (userInput1.isNotEmpty())URLEncoder.encode(userInput1, "UTF-8") else ""
        val userInput2 = binding.val2.text?.toString()?.trim()?:""
        val encodeInput2 = if (userInput2.isNotEmpty())URLEncoder.encode(userInput2, "UTF-8") else ""
        val quantity = binding.kol1.text?.toString()?.trim()?:""
        val quantityEncode = if (quantity.isNotEmpty())URLEncoder.encode(quantity, "UTF-8") else ""
        if (encodeInput1.isEmpty() || encodeInput2.isEmpty() || quantityEncode.isEmpty()) {
                Toast.makeText(activity, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }
        val currenciesJson = readCurJson(requireContext())
        val type = object : TypeToken<List<CurrencyJsonData>>() {}.type
        val currencies = Gson().fromJson<List<CurrencyJsonData>>(currenciesJson, type)
        val currencyCode1 = findCurrencyCode(userInput1, currencies)
        val currencyCode2 = findCurrencyCode(userInput2, currencies)
        if (currencyCode1 == null || currencyCode2 == null) {
            Toast.makeText(activity, "Введенная валюта не найдена", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://v6.exchangerate-api.com/v6/ce3f218c1a3de000df76286f/pair/$currencyCode1/$currencyCode2/$quantityEncode"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                    result ->
                parseCurConvert(result)
                updateConvert()
            },
            {
                    error -> Toast.makeText(activity, "Произошла ошибка, повторите попытку позже", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
    //парсинг
    private fun parseConvert(mainObject:JSONObject){
        val itemCur = CurrencyConvert(
            mainObject.getString("conversion_result")
        )
        model.liveDataConvert.value = itemCur
    }
    //передаю данные на экран
    private fun updateConvert(){
        model.liveDataConvert.observe(viewLifecycleOwner){ currencyConvert ->
            binding.kol2.setText(currencyConvert.res)
        }
    }
    //запускаю парсинг
    private fun parseCurConvert(result:String){
        val mainObject = JSONObject(result)
        parseConvert(mainObject)
    }

    companion object {

        @JvmStatic
        fun newInstance() = ConverterFragment()
    }
}