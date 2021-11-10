package kb.inventory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class TakeOutItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var rootLinearLayout: LinearLayout
    lateinit var navView: NavigationView
    lateinit var itemCode: String
    lateinit var scanResult: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("mylog","activity indul")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_out_item)

        toolbar = findViewById(R.id.toolbar)
        rootLinearLayout = findViewById(R.id.takeOutItemLinearLayout)
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
        val url = "http://192.168.137.1:3000/item/info?code=$itemCode"

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                //textView.text = "Response: %s".format(response.toString())
                Log.v("mylog", "RESPONSE")
                Log.v("mylog", response.toString())
                if (response.toString() == "{}") {
                    Log.v("mylog", "insertNew")
                    takeOutNonExisting()
                } else {
                    Log.v("mylog", "insertExisting")
                    scanResult = response
                    Log.v("mylog", "insertExisting1")

                    var itemQuantity = scanResult.getInt("quantity")
                    if(itemQuantity == 0){
                        takeOutExistingZeroCount()
                    } else {
                        takeOutExistingNonZeroCount()
                    }



                }
            },
            { error ->
                // TODO: Handle error
            }
        )

        // Add the request to the RequestQueue.
        mQueue.add(jsonObjectRequest)
    }

    private fun takeOutExistingNonZeroCount() {
        Log.v("mylog", "takeoutExistingNonzero count")
        val takeOutExistingItemNNot0View: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_take_out_existing_item_n_not_0, rootLinearLayout, false)
        rootLinearLayout.addView(takeOutExistingItemNNot0View)


        val cancelButton: Button = findViewById(R.id.btnCancel)
        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                //putExtra("itemCode", intentResult.contents )
            }
            startActivity(intent)
        }

        Log.v("mylog", "there")

        val okButton: Button = findViewById(R.id.btnOk)

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
        Log.v("mylog", "there1")
        okButton.setOnClickListener {
            val quantityEditText : EditText = findViewById(R.id.etTakeOutQuantity)
            if(quantityEditText.text.isEmpty()){
                Toast.makeText(this, "Add meg a mennyiséget!", Toast.LENGTH_SHORT).show()
            } else {
                val queue = Volley.newRequestQueue(this)
                val url = "http://192.168.137.1:3000/item/take_out"

                //val requestBody = "code="+ itemCode + "&quantity="
                val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                reqMap["code"] = itemCode
                reqMap["quantity"] = quantityEditText.text.toString().toInt()

                if(quantityEditText.text.toString().toInt() > tvExistingQuantity.text.toString().toInt()){
                    Toast.makeText(this, "Nem lehet ennyit kivenni!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val reqBody : JSONObject = JSONObject(reqMap)

                Log.v("mylog", reqBody.toString())
                Log.v("mylog", reqBody.toString().toByteArray(Charset.defaultCharset()).toString())

                val stringReq : StringRequest =
                    object : StringRequest(Method.POST, url,
                        Response.Listener { response ->
                            // response
                            var strResp = response.toString()
                            Log.v("mylog", "RESP:" +"["+strResp+"]")
                            Log.d("API", strResp)
                            Toast.makeText(this, "Sikeres kivétel", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java).apply {
                                //putExtra("itemCode", intentResult.contents )
                            }
                            startActivity(intent)
                        },
                        Response.ErrorListener { error ->
                            Log.d("API", "error => $error")
                        }
                    ){
                        override fun getBody(): ByteArray {
                            return reqBody.toString().toByteArray(Charset.defaultCharset())
                        }
                    }
                queue.add(stringReq)

            }

        }
    }

    private fun takeOutExistingZeroCount() {
        Log.v("mylog", "takeoutExisting zero count")

        val takeOutExistingItemNIs0View: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_take_out_existing_item_n_is_0, rootLinearLayout, false)
        rootLinearLayout.addView(takeOutExistingItemNIs0View)


        val okButton: Button = findViewById(R.id.btnOk)
        okButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                //putExtra("itemCode", intentResult.contents )
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

    private fun takeOutNonExisting() {
        Log.v("mylog", "takeout NON Existing")

        val takeOutNonExistingItem: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_take_out_not_existing_item, rootLinearLayout, false)
        rootLinearLayout.addView(takeOutNonExistingItem)


        val okButton: Button = findViewById(R.id.btnOk)
        okButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                //putExtra("itemCode", intentResult.contents )
            }
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
                val intent = Intent(this, ViewCategoriesActivity::class.java).apply {}
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