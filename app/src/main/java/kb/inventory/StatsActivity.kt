package kb.inventory

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import java.util.*

class StatsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

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
        val url = "http://192.168.137.1:3000/stats"

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    //textView.text = "Response: %s".format(response.toString())
                    Log.v("mylog", "RESPONSE")
                    Log.v("mylog", response.toString())
                    val itemCount = response.getInt("allItemCount")
                    val itemDiversity = response.getInt("itemDiversity")

                    val tvItemCount : TextView = findViewById(R.id.tvItemCount)
                    tvItemCount.text = itemCount.toString()

                    val tvItemDiversity : TextView = findViewById(R.id.tvItemDiversity)
                    tvItemDiversity.text = itemDiversity.toString()
                },
                { error ->
                    // TODO: Handle error
                }
        )

        // Add the request to the RequestQueue.
        mQueue.add(jsonObjectRequest)

        val c = Calendar.getInstance()
        var year = c.get(Calendar.YEAR)
        var month = c.get(Calendar.MONTH)
        var day = c.get(Calendar.DAY_OF_MONTH)

        val btnStartDate : Button = findViewById(R.id.btnPickStartDate)
        val tvStartDate : TextView = findViewById(R.id.tvStartDate)
        btnStartDate.setOnClickListener {
            val startDatePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{view, sYear, sMonth, sDay ->
                tvStartDate.text = "$sYear/$sMonth/$sDay"
            }, year, month, day)

            startDatePickerDialog.show()
        }

        val btnEndDate : Button = findViewById(R.id.btnPickEndDate)
        val tvEndDate : TextView = findViewById(R.id.tvEndDate)
        btnEndDate.setOnClickListener {
            val endDatePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{view, sYear, sMonth, sDay ->
                tvEndDate.text = "$sYear/$sMonth/$sDay"
            }, year, month, day)

            endDatePickerDialog.show()
        }
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