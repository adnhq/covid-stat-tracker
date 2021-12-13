package com.zubex.covidstats

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView

import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.gson.GsonBuilder
import com.zubex.covidstats.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.text.NumberFormat
import java.util.*
import kotlin.reflect.typeOf
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var mAdView : AdView
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        MobileAds.initialize(this) {}
        //Banner Ad
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //Spinner
        val countriesList = listOf("Afghanistan", "Albania","Algeria","Andorra","Angola","Anguilla","Antigua-and-Barbuda","Argentina","Armenia","Aruba","Australia","Austria","Azerbaijan","Bahamas","Bahrain","Bangladesh","Barbados","Belarus","Belgium","Belize","Benin","Bermuda","Bhutan","Bolivia","Bosnia-and-Herzegovina","Botswana","Brazil","British-Virgin-Islands","Brunei","Bulgaria","Burkina-Faso","Burundi","Cabo-Verde","Cambodia","Cameroon","Canada","CAR","Caribbean-Netherlands","Cayman-Islands","Chad","Channel-Islands","Chile","China","Colombia","Comoros","Congo","Costa-Rica","Croatia","Cuba","Cyprus","Czechia","Denmark","Diamond-Princess","Djibouti","Dominica","Dominican-Republic","DRC","Ecuador","Egypt","El-Salvador","Equatorial-Guinea","Eritrea","Estonia","Eswatini","Ethiopia","Faeroe-Islands","Falkland-Islands","Fiji","Finland","France","French-Guiana","French-Polynesia","Gabon","Gambia","Georgia","Germany","Ghana","Gibraltar","Greece","Greenland","Grenada","Guadeloupe","Guam","Guatemala","Guinea","Guinea-Bissau","Guyana","Haiti","Honduras","Hong-Kong","Hungary","Iceland","India","Indonesia","Iran","Iraq","Ireland","Isle-of-Man","Israel","Italy","Ivory-Coast","Jamaica","Japan","Jordan","Kazakhstan","Kenya","Kuwait","Kyrgyzstan","Laos","Latvia","Lebanon","Lesotho","Liberia","Libya","Liechtenstein","Lithuania","Luxembourg","Macao","Madagascar","Malawi","Malaysia","Maldives","Mali","Malta","Marshall-Islands","Martinique","Mauritania","Mauritius","Mayotte","Mexico","Micronesia","Moldova","Monaco","Mongolia","Montenegro","Montserrat","Morocco","Mozambique","MS-Zaandam","MS-Zaandam-","Myanmar","Namibia","Nepal","Netherlands","New-Caledonia","New-Zealand","Nicaragua","Niger","Nigeria","North-Macedonia","Norway","Oman","Pakistan","Palestine","Panama","Papua-New-Guinea","Paraguay","Peru","Philippines","Poland","Portugal","Puerto-Rico","Qatar","Romania","Russia","Rwanda","S-Korea","Saint-Kitts-and-Nevis","Saint-Lucia","Saint-Martin","Saint-Pierre-Miquelon","Samoa","San-Marino","Sao-Tome-and-Principe","Saudi-Arabia","Senegal","Serbia","Seychelles","Sierra-Leone","Singapore","Sint-Maarten","Slovakia","Slovenia","Solomon-Islands","Somalia","South-Africa","South-Sudan","Spain","Sri-Lanka","St-Barth","St-Vincent-Grenadines","Sudan","Suriname","Sweden","Switzerland","Syria","Taiwan","Tajikistan","Tanzania","Thailand","Timor-Leste","Togo","Trinidad-and-Tobago","Tunisia","Turkey","Turks-and-Caicos","UAE","Uganda","UK","Ukraine","Uruguay","US-Virgin-Islands","USA","Uzbekistan","Vanuatu","Vatican-City","Venezuela","Vietnam","Wallis-and-Futuna","Western-Sahara","Yemen","Zambia","Zimbabwe")
        val adapter = ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, countriesList)
        binding.spinner.adapter = adapter
        //RecyclerView List
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        //Spinner Adapter
        var counter: Int = 0
        loadAds()

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val country: String = parent?.getItemAtPosition(position).toString()
                if (counter%3==0){
                    showAds()
                }
                fetchJson(country)
                loadAds()
                counter++
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    fun loadAds(){
        var adRequest2 = AdRequest.Builder().build()
        InterstitialAd.load(this,"ca-app-pub-1119565004073519/7621798908", adRequest2, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showAds(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this@MainActivity)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    fun fetchJson(c:String){
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("https://covid-193.p.rapidapi.com/statistics?country=$c")
                .get()
                .addHeader("x-rapidapi-key", "5344b63e66mshbd235ab75be8436p17bd3bjsnee1289cdaac5")
                .addHeader("x-rapidapi-host", "covid-193.p.rapidapi.com")
                .build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    val failureList = mutableListOf(CountryStats(" Could not retrieve data.\n Please check your internet connection and restart", ""))
                    binding.recyclerView.adapter = ItemAdapter(failureList)
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val home = gson.fromJson(body, Home::class.java)
                runOnUiThread {
                    var homeObj = home.response[0]
                    var reportList = mutableListOf(
                        CountryStats("New Cases (${homeObj.day})", homeObj.cases.new ?: "N/A"),
                        CountryStats("New Deaths (${homeObj.day})", homeObj.deaths.new ?: "N/A"),
                        CountryStats("Total Cases", if (homeObj.cases.total>1000000) "%.2f".format(homeObj.cases.total.toDouble()/1000000).toString()+"M" else numFormatter(homeObj.cases.total)),
                            CountryStats("Active Cases", if (homeObj.cases.active>1000000) "%.2f".format(homeObj.cases.active.toDouble()/1000000).toString()+"M" else numFormatter(homeObj.cases.active)),
                            CountryStats("Recovered", if (homeObj.cases.recovered>1000000) "%.2f".format(homeObj.cases.recovered.toDouble()/1000000).toString()+"M" else numFormatter(homeObj.cases.recovered)),
                        CountryStats("Total Deaths", if (homeObj.deaths.total>1000000) "%.2f".format(homeObj.deaths.total.toDouble()/1000000).toString()+"M" else numFormatter(homeObj.deaths.total)),
                        CountryStats("Total Tests", if (homeObj.tests.total>1000000) "%.2f".format(homeObj.tests.total.toDouble()/1000000).toString()+"M" else numFormatter(homeObj.tests.total)),
                        CountryStats("Population", populationFormatter(homeObj.population))
                    )
                    val adapterRv = ItemAdapter(reportList)
                    binding.recyclerView.adapter = adapterRv
                }
            }
        })
    }
    fun numFormatter(n: Int): String{
        return NumberFormat.getNumberInstance(Locale.US).format(n).toString()
    }
    fun populationFormatter(n: Int): String{
        return when {
            n in 1000000 until 1000000000 -> {
                "%.2f".format(n.toDouble()/1000000).toString()+"M"
            }
            n>=1000000000 -> {
                "%.2f".format(n.toDouble()/1000000000).toString()+"B"
            }
            else -> {
                numFormatter(n)
            }
        }
    }
}

class Home(val response: List<Rspn>)
class Rspn(val continent: String, val country: String, val population: Int, val day: String, val cases: Cases, val deaths: Deaths, val tests: Tests)
class Cases(val new: String, val active: Int, val critical: Any, val recovered: Int, val sth : String, val total: Int)
class Deaths(val new:String, val sth: String, val total: Int)
class Tests(val sth: String, val total: Int)
