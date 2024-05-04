package com.example.exchangerates

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exchangerates.fragments.ConverterFragment
import com.example.exchangerates.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //открываю фрагмент хоум на активити
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment.newInstance()).commit()
        }
        //делаю переключение между фрагментами через кнопки нижнего меню
        val bNavView:BottomNavigationView = findViewById(R.id.bNav)
        bNavView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.menuHome -> {
                    supportFragmentManager.beginTransaction().
                    replace(R.id.fragment_container, HomeFragment.newInstance()).commit()
                    true
                }
                R.id.menuConverter -> {
                    supportFragmentManager.beginTransaction().
                    replace(R.id.fragment_container, ConverterFragment.newInstance()).commit()
                    true
                }
                else -> false
            }
        }



    }
}