package kb.inventory

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject

class InsertItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var itemCode: String
    lateinit var scanResult: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("mylog", "oc1")
        setContentView(R.layout.activity_insert_item)
        Log.v("mylog", "oc1")
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
            Response.Listener { response ->
                //textView.text = "Response: %s".format(response.toString())
                Log.v("mylog", "RESPONSE")
                Log.v("mylog", response.toString())
                if(response.toString() == "{}") {
                    Log.v("mylog", "insertNew")
                    insertNew()
                } else {
                    Log.v("mylog", "insertExisting")
                    scanResult = response
                    Log.v("mylog", "insertExisting1")
                    insertExisting()


                }
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
            }
        )

        // Add the request to the RequestQueue.
        mQueue.add(jsonObjectRequest)
    }

    private fun insertExisting() {
        val insertExistingItemView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_insert_existing_item, drawerLayout, false)
        drawerLayout.addView(insertExistingItemView)
        Log.v("mylog", "there")

        val button: Button = findViewById(R.id.btnOk)

        button.setOnClickListener {
            Log.v("mylog", "clicked")
            // Do something in response to button click
            val text = "Hello toast!"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this, text, duration)
            toast.show()
        }

    }

    private fun insertNew() {
        val insertNewItemView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_insert_new_item, this.drawerLayout, false)
        drawerLayout.addView(insertNewItemView)
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