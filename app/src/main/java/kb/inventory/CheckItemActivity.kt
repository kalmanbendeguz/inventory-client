package kb.inventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject

class CheckItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var rootLinearLayout: LinearLayout
    lateinit var navView: NavigationView
    lateinit var itemCode: String
    lateinit var scanResult: JSONObject
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_item)

        sharedPref = getSharedPreferences("kb.inventory.settings", Context.MODE_PRIVATE)

        currentServerIP = sharedPref.getString("server_ip", "0.0.0.0")!!
        currentPort = sharedPref.getInt("server_port", 3000).toString()

        toolbar = findViewById(R.id.toolbar)
        rootLinearLayout = findViewById(R.id.checkItemLinearLayout)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        val intent: Intent = intent
        itemCode = intent.getStringExtra("itemCode")!!

        val mQueue = Volley.newRequestQueue(this)
        val url = "http://$currentServerIP:$currentPort/item/info?code=$itemCode"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                if (response.toString() == "{}") {
                    checkNotExisting()
                } else {
                    scanResult = response
                    checkExisting()
                }
            },
            { error -> }
        )

        mQueue.add(jsonObjectRequest)
    }

    private fun checkExisting() {

        val checkExistingView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_check_item_existing, rootLinearLayout, false)
        rootLinearLayout.addView(checkExistingView)


        val okButton: Button = findViewById(R.id.btnOk)
        okButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }

        val datasheetButton: Button = findViewById(R.id.btnDatasheet)
        datasheetButton.setOnClickListener {
            val intent = Intent(this, ViewItemActivity::class.java).apply {
                putExtra("itemCode", itemCode )
            }
            startActivity(intent)
        }

        val tvCode : TextView = findViewById(R.id.tvCode)
        tvCode.text = scanResult.getString("code")

        val tvExistingQuantity : TextView = findViewById(R.id.tvExistingQuantity)
        tvExistingQuantity.text = scanResult.getString("quantity")

        val tvName : TextView = findViewById(R.id.tvName)
        tvName.text = scanResult.getString("name")

        val categoryArray : JSONArray = scanResult.getJSONArray("categoryStringArray")
        val tvCategory : TextView = findViewById(R.id.tvCategory)
        for (i in 0 until categoryArray.length()){

            tvCategory.append(" / "+categoryArray[i].toString())
        }
        if(categoryArray.length() == 0) {
            tvCategory.append(" / ")
        }
    }

    private fun checkNotExisting() {
        val checkNonExistingItem: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_check_item_not_existing, rootLinearLayout, false)
        rootLinearLayout.addView(checkNonExistingItem)

        val okButton: Button = findViewById(R.id.btnOk)
        okButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }

        val tvCode : TextView = findViewById(R.id.tvCode)
        tvCode.text = itemCode
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
                    putExtra("category_code", "")
                    putExtra("category_path", "")
                }
                startActivity(intent)
            }
            R.id.nav_stats -> {
                val intent = Intent(this, StatsActivity::class.java).apply {}
                startActivity(intent)
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