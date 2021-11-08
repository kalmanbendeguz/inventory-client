package kb.inventory

import android.content.Intent
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

class ViewItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var rootLinearLayout: LinearLayout
    lateinit var navView: NavigationView
    lateinit var itemCode: String
    lateinit var scanResult: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_item)

        Log.v("mylog","oncreate1")
        toolbar = findViewById(R.id.toolbar)
        rootLinearLayout = findViewById(R.id.viewItemLinearLayout)
        setSupportActionBar(toolbar)

        Log.v("mylog","oncreate2")
        drawerLayout = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        Log.v("mylog","oncreate3")
        var intent: Intent = intent
        itemCode = intent.getStringExtra("itemCode")!!
        Log.v("mylog", "ITEMCODE")
        Log.v("mylog", itemCode)

        // Instantiate the RequestQueue.
        val mQueue = Volley.newRequestQueue(this)
        val url = "http://192.168.0.114:3000/item/info?code=$itemCode"

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                //textView.text = "Response: %s".format(response.toString())
                scanResult = response
                viewItem()
            },
            { error ->
                // TODO: Handle error
            }
        )

        // Add the request to the RequestQueue.
        mQueue.add(jsonObjectRequest)
    }

    private fun viewItem() {
        Log.v("mylog", "viewItem")

        val viewItemView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_view_item, rootLinearLayout, false)
        rootLinearLayout.addView(viewItemView)


        val tvName : TextView = findViewById(R.id.tvName)
        tvName.text = scanResult.getString("name")

        val tvCode : TextView = findViewById(R.id.tvCode)
        tvCode.text = scanResult.getString("code")

        val categoryArray : JSONArray = scanResult.getJSONArray("categoryStringArray")
        val tvCategory : TextView = findViewById(R.id.tvCategory)
        for (i in 0 until categoryArray.length()){

            tvCategory.append(" / "+categoryArray[i].toString())
        }
        if(categoryArray.length() == 0) {
            tvCategory.append(" / ")
        }

        val tvQuantity : TextView = findViewById(R.id.tvQuantity)
        tvQuantity.text = scanResult.getString("quantity")
        Log.v("mylog", "viewItem1")
        val tvLastChanged : TextView = findViewById(R.id.tvLastChanged)
        Log.v("mylog", "viewItem3")
        //Log.v("mylog", scanResult.toString())
        tvLastChanged.text = scanResult.getString("lastChanged")
        Log.v("mylog", "viewItem2")

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_landing -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_all_items -> {
                Toast.makeText(this, "Orders clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_categories -> {
                Toast.makeText(this, "About us clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_stats -> {
                Toast.makeText(this, "Terms and conditions clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}