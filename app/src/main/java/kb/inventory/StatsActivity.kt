package kb.inventory

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color.blue
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.navigation.NavigationView
import com.google.gson.JsonObject
import org.json.JSONObject
import java.security.KeyStore
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class StatsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var scanResult: JSONObject
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        sharedPref = getSharedPreferences("kb.inventory.settings", Context.MODE_PRIVATE)

        currentServerIP = sharedPref.getString("server_ip", "0.0.0.0")!!
        currentPort = sharedPref.getInt("server_port", 3000).toString()
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        val mQueue = Volley.newRequestQueue(this)
        val url = "http://$currentServerIP:$currentPort/stats"

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    //textView.text = "Response: %s".format(response.toString())


                    scanResult = response
                    val itemCount = response.getInt("allItemCount")
                    val itemDiversity = response.getInt("itemDiversity")

                    val tvItemCount : TextView = findViewById(R.id.tvItemCount)
                    tvItemCount.text = itemCount.toString()

                    val tvItemDiversity : TextView = findViewById(R.id.tvItemDiversity)
                    tvItemDiversity.text = itemDiversity.toString()
                    setLineChartData()
                },
                { error ->
                    // TODO: Handle error
                }
        )

        // Add the request to the RequestQueue.
        mQueue.add(jsonObjectRequest)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLineChartData(){
        val lineEntry = ArrayList<Entry>()

        val dataPoints = scanResult.getJSONArray("changes")

        for(i in 0 until dataPoints.length()){
            val dataPoint : JSONObject = dataPoints.get(i) as JSONObject
            val updatedAt: String = dataPoint.getString("updatedAt")
            var updatedDate: Date = Calendar.getInstance().time
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                updatedDate = Date.from(Instant.parse(updatedAt))
            } else {

            }
            val quantity: Int = dataPoint.getInt("quantity")
            lineEntry.add(Entry(updatedDate.toInstant().toEpochMilli().toFloat(),quantity.toFloat()))
        }

        val lineDataset: LineDataSet = LineDataSet(lineEntry, "Összes tétel száma")
        lineDataset.color = resources.getColor(R.color.blue)
        lineDataset.setDrawCircles(false)
        lineDataset.setDrawValues(false)
        lineDataset.lineWidth = 1.5F
        val data = LineData(lineDataset)
        val lineChart: LineChart = findViewById(R.id.lineChart)
        lineChart.data = data
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        lineChart.animateXY(200,200)
        lineChart.description.text = ""
        lineChart.xAxis.textColor = resources.getColor(R.color.white)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_landing -> {
                val intent = Intent(this, MainActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.nav_all_items -> {
                val intent = Intent(this, ViewAllItemsActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.nav_categories -> {
                val intent = Intent(this, ViewCategoriesActivity::class.java).apply {
                    putExtra("category_code", "" )
                    putExtra("category_path", "")
                }
                startActivity(intent)
            }
            R.id.nav_stats -> {

            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {}
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}