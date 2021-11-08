package kb.inventory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class InsertItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var rootLinearLayout: LinearLayout
    lateinit var navView: NavigationView
    lateinit var itemCode: String
    lateinit var scanResult: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("mylog", "oc1")
        setContentView(R.layout.activity_insert_item)
        Log.v("mylog", "oc1")
        toolbar = findViewById(R.id.toolbar)
        rootLinearLayout = findViewById(R.id.insertItemLinearLayout)
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
                if (response.toString() == "{}") {
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

    private fun insertNew() {
        val insertNewItemView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_insert_new_item, rootLinearLayout, false)
        rootLinearLayout.addView(insertNewItemView)

        val toolbarLabel : TextView = findViewById(R.id.toolbarLabel)
        toolbarLabel.text = "Új tétel"

        val cancelButton: Button = findViewById(R.id.btnCancel)
        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                //putExtra("itemCode", intentResult.contents )
            }
            startActivity(intent)
        }

        val okButton: Button = findViewById(R.id.btnOk)

        val tvCode : TextView = findViewById(R.id.tvCode)
        tvCode.text = itemCode

        val tvExistingQuantity : TextView = findViewById(R.id.tvExistingQuantity)
        tvExistingQuantity.text = "0"

        val tvName : TextView = findViewById(R.id.etName)
        tvName.text = itemCode

        okButton.setOnClickListener {
            val quantityEditText : EditText = findViewById(R.id.etInsertQuantity)
            if(quantityEditText.text.isEmpty()){
                Toast.makeText(this, "Add meg a mennyiséget!", Toast.LENGTH_SHORT).show()
            } else {
                val queue = Volley.newRequestQueue(this)
                val url = "http://192.168.0.114:3000/item/insert_new"

                val nameEditText: EditText = findViewById(R.id.etName)
                val categoryEditText: EditText = findViewById(R.id.etCategory)

                val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                reqMap["code"] = itemCode
                reqMap["name"] = nameEditText.text.toString()
                var categoryArray: List<String> = categoryEditText.text.toString().split("/").map { it.trim() }
                categoryArray = categoryArray.filter{!it.isEmpty()}
                reqMap["quantity"] = quantityEditText.text.toString().toInt()
                if(!categoryArray.isEmpty()){
                    reqMap["category"] = categoryArray
                } else {
                    reqMap["category"] = "[]"
                }


                val reqBody : JSONObject = JSONObject(reqMap)
                Log.v("mylog", reqBody.toString())
                val stringReq : StringRequest =
                    object : StringRequest(Method.POST, url,
                        Response.Listener { response ->
                            // response
                            var strResp = response.toString()
                            Log.v("mylog", "RESP:" +"["+strResp+"]")
                            Log.d("API", strResp)
                            Toast.makeText(this, "Sikeres bevitel", Toast.LENGTH_SHORT).show()
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
                            return reqBody.toString().toByteArray()
                        }
                    }
                queue.add(stringReq)

            }

        }
    }

    private fun insertExisting() {
        val insertExistingItemView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.content_insert_existing_item, rootLinearLayout, false)
        rootLinearLayout.addView(insertExistingItemView)


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
        val tvCategory :  TextView = findViewById(R.id.tvCategory)
        for (i in 0 until categoryArray.length()){

            tvCategory.append(" / "+categoryArray[i].toString())
        }

        okButton.setOnClickListener {
            val quantityEditText : EditText = findViewById(R.id.etInsertQuantity)
            if(quantityEditText.text.isEmpty()){
                Toast.makeText(this, "Add meg a mennyiséget!", Toast.LENGTH_SHORT).show()
            } else {
                val queue = Volley.newRequestQueue(this)
                val url = "http://192.168.0.114:3000/item/insert_existing"

                //val requestBody = "code="+ itemCode + "&quantity="
                val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                reqMap["code"] = itemCode
                reqMap["quantity"] = quantityEditText.text.toString().toInt()
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
                            Toast.makeText(this, "Sikeres bevitel", Toast.LENGTH_SHORT).show()
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