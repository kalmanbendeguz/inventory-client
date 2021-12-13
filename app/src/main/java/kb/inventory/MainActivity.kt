package kb.inventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kb.inventory.qbarcode.Capture
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    var lastPressedButton: String = "none"
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        val insertButton: ImageButton = findViewById(R.id.btnInsert)
        insertButton.setOnClickListener {
            lastPressedButton = "insertButton"
            val intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val takeOutButton: ImageButton = findViewById(R.id.btnTakeOut)
        takeOutButton.setOnClickListener {
            lastPressedButton = "takeOutButton"
            val intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val checkButton: ImageButton = findViewById(R.id.btnCheck)
        checkButton.setOnClickListener {
            lastPressedButton = "checkButton"
            val intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val tvInventoryName: TextView = findViewById(R.id.tvInventoryName)
        val queue = Volley.newRequestQueue(this)
        val inventoryNameURL = "http://$currentServerIP:$currentPort/inventory/get_name"

        val stringRequest = StringRequest(
            Request.Method.GET, inventoryNameURL,
            { response ->
                if (response != "") {
                    tvInventoryName.text = "Raktár neve: " + response.toString()
                } else {

                    val nameInventoryDialog = AlertDialog.Builder(this@MainActivity)
                    nameInventoryDialog.setTitle("Adj nevet a raktárnak!")

                    val inventoryNameInput = EditText(this@MainActivity)
                    inventoryNameInput.inputType = InputType.TYPE_CLASS_TEXT
                    inventoryNameInput.setText("Raktáram")

                    nameInventoryDialog.setView(inventoryNameInput)

                    nameInventoryDialog.setPositiveButton("OK") { dialogInterface, i ->
                        val inventoryName = inventoryNameInput.text.toString()

                        if (inventoryName.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "Nem lehet üres a raktár neve!",
                                Toast.LENGTH_SHORT
                            ).show()
                            nameInventoryDialog.show()
                        } else {

                            try {
                                val requestQueue = Volley.newRequestQueue(this)
                                val URL = "http://$currentServerIP:$currentPort/inventory/set_name"
                                val jsonBody = JSONObject()
                                jsonBody.put("inventory_name", inventoryName)

                                val requestBody = jsonBody.toString()
                                val stringRequest: StringRequest = object : StringRequest(
                                    Method.POST, URL,
                                    Response.Listener { response ->
                                        Log.i("VOLLEY", response!!)
                                        tvInventoryName.text = "Raktár neve: "+ inventoryName
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Sikeres névadás!",
                                            Toast.LENGTH_SHORT
                                        ).show() },
                                    Response.ErrorListener { error ->
                                        Log.e(
                                            "VOLLEY",
                                            error.toString()
                                        )
                                    }) {
                                    override fun getBodyContentType(): String {
                                        return "application/json; charset=utf-8"
                                    }

                                    @Throws(AuthFailureError::class)
                                    override fun getBody(): ByteArray {
                                        return try {
                                            if (requestBody == null) null else requestBody.toByteArray(
                                                charset("utf-8")
                                            )
                                        } catch (uee: UnsupportedEncodingException) {
                                            VolleyLog.wtf(
                                                "Unsupported Encoding while trying to get the bytes of %s using %s",
                                                requestBody,
                                                "utf-8"
                                            )
                                            null
                                        }!!
                                    }

                                    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                                        var responseString = ""
                                        if (response != null) {
                                            responseString = response.statusCode.toString()
                                            // can get more details such as response.headers
                                        }
                                        return Response.success(
                                            responseString,
                                            HttpHeaderParser.parseCacheHeaders(response)
                                        )
                                    }
                                }
                                requestQueue.add(stringRequest)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                    }

                    nameInventoryDialog.show()
                }
            },
            { error -> })


        queue.add(stringRequest)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val intentResult: IntentResult = IntentIntegrator.parseActivityResult(
            requestCode,
            resultCode,
            data
        )
        if(intentResult.contents != null){

            when(lastPressedButton){
                "insertButton" -> {
                    val intent = Intent(this, InsertItemActivity::class.java).apply {
                        putExtra("itemCode", intentResult.contents)
                    }
                    startActivity(intent)
                }
                "takeOutButton" -> {
                    val intent = Intent(this, TakeOutItemActivity::class.java).apply {
                        putExtra("itemCode", intentResult.contents)
                    }
                    startActivity(intent)
                }
                "checkButton" -> {
                    val intent = Intent(this, CheckItemActivity::class.java).apply {
                        putExtra("itemCode", intentResult.contents)
                    }
                    startActivity(intent)
                }
            }

        } else {

            Toast.makeText(applicationContext, "Beolvasás megszakítva", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_landing -> {

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