package kb.inventory

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
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

        Log.v("mylog", "oncreate1")
        toolbar = findViewById(R.id.toolbar)
        rootLinearLayout = findViewById(R.id.viewItemLinearLayout)
        setSupportActionBar(toolbar)

        Log.v("mylog", "oncreate2")
        drawerLayout = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        Log.v("mylog", "oncreate3")
        var intent: Intent = intent
        itemCode = intent.getStringExtra("itemCode")!!
        Log.v("mylog", "ITEMCODE")
        Log.v("mylog", itemCode)


        val editNameButton: ImageButton = findViewById(R.id.btnEditName)
        editNameButton.setOnClickListener {
            val renameItemDialog = AlertDialog.Builder(this@ViewItemActivity)
            renameItemDialog.setTitle("Átnevezés")

            val oldName : String = findViewById<TextView>(R.id.tvName).text.toString()

            val newNameInput = EditText(this@ViewItemActivity)
            newNameInput.inputType = InputType.TYPE_CLASS_TEXT
            newNameInput.setText(oldName)

            renameItemDialog.setView(newNameInput)

            renameItemDialog.setPositiveButton("OK") { dialogInterface, i ->
                val newName = newNameInput.text.toString()

                if(newName == oldName) {
                    Toast.makeText(this@ViewItemActivity, "Nem változott!", Toast.LENGTH_SHORT).show()
                } else {
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://192.168.137.1:3000/item/rename"

                    //val requestBody = "code="+ itemCode + "&quantity="
                    val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                    reqMap["code"] = itemCode
                    reqMap["new_name"] = newName

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
                                        findViewById<TextView>(R.id.tvName).text = newName
                                        Toast.makeText(this, "Sikeres átnevezés", Toast.LENGTH_SHORT).show()

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

            renameItemDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            renameItemDialog.show()

        }


        /////

        val editCategoryButton: ImageButton = findViewById(R.id.btnEditCategory)
        editCategoryButton.setOnClickListener {
            val editCategoryDialog = AlertDialog.Builder(this@ViewItemActivity)
            editCategoryDialog.setTitle("Kategóriaváltás")

            var oldCategoryString = findViewById<TextView>(R.id.tvCategory).text.toString()
            var oldCategoryArray: List<String> = oldCategoryString.split("/").map { it.trim() }
            oldCategoryArray = oldCategoryArray.filter{!it.isEmpty()}

            val newCategoryInput = EditText(this@ViewItemActivity)
            newCategoryInput.inputType = InputType.TYPE_CLASS_TEXT
            newCategoryInput.setText(oldCategoryString)

            editCategoryDialog.setView(newCategoryInput)

            editCategoryDialog.setPositiveButton("OK") { dialogInterface, i ->
                Log.v("mylog", "ok button")
                val newCategoryString = newCategoryInput.text.toString()
                var newCategoryArray: List<String> = newCategoryString.split("/").map { it.trim() }
                newCategoryArray = newCategoryArray.filter{!it.isEmpty()}

                if(oldCategoryArray == newCategoryArray) {
                    Toast.makeText(this@ViewItemActivity, "Nem változott!", Toast.LENGTH_SHORT).show()
                } else {
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://192.168.137.1:3000/item/change_category"

                    //val requestBody = "code="+ itemCode + "&quantity="
                    val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                    reqMap["code"] = itemCode
                    if(!newCategoryArray.isEmpty()){
                        reqMap["category"] = newCategoryArray
                    } else {
                        reqMap["category"] = "[]"
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
                                        findViewById<TextView>(R.id.tvCategory).text = newCategoryString
                                        Toast.makeText(this, "Sikeres kategóriaváltás", Toast.LENGTH_SHORT).show()

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

            editCategoryDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            editCategoryDialog.show()

        }

        /////

        /*val datasheetButton: Button = findViewById(R.id.btnDatasheet)
        datasheetButton.setOnClickListener {
            val intent = Intent(this, ViewItemActivity::class.java).apply {
                putExtra("itemCode", itemCode)
            }
            startActivity(intent)
        }*/

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

            tvCategory.append(" / " + categoryArray[i].toString())
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