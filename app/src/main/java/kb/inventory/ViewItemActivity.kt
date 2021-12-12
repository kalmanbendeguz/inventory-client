package kb.inventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
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


class ViewItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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
        setContentView(R.layout.activity_view_item)
        sharedPref = getSharedPreferences("kb.inventory.settings", Context.MODE_PRIVATE)

        currentServerIP = sharedPref.getString("server_ip", "0.0.0.0")!!
        currentPort = sharedPref.getInt("server_port", 3000).toString()

        toolbar = findViewById(R.id.toolbar)
        rootLinearLayout = findViewById(R.id.viewItemLinearLayout)
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
                    val url = "http://$currentServerIP:$currentPort/item/rename"

                    val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                    reqMap["code"] = itemCode
                    reqMap["new_name"] = newName

                    val reqBody : JSONObject = JSONObject(reqMap)

                    val stringReq : StringRequest =
                            object : StringRequest(Method.POST, url,
                                    Response.Listener { response ->

                                        findViewById<TextView>(R.id.tvName).text = newName
                                        Toast.makeText(this, "Sikeres átnevezés", Toast.LENGTH_SHORT).show()

                                    },
                                    Response.ErrorListener { error -> }
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

        val editCategoryButton: ImageButton = findViewById(R.id.btnEditCategory)
        editCategoryButton.setOnClickListener {
            val editCategoryDialog = AlertDialog.Builder(this@ViewItemActivity)
            editCategoryDialog.setTitle("Kategóriaváltás")

            val oldCategoryString = findViewById<TextView>(R.id.tvCategory).text.toString()
            var oldCategoryArray: List<String> = oldCategoryString.split("/").map { it.trim() }
            oldCategoryArray = oldCategoryArray.filter{!it.isEmpty()}

            val newCategoryInput = EditText(this@ViewItemActivity)
            newCategoryInput.inputType = InputType.TYPE_CLASS_TEXT
            newCategoryInput.setText(oldCategoryString)

            editCategoryDialog.setView(newCategoryInput)

            editCategoryDialog.setPositiveButton("OK") { dialogInterface, i ->

                val newCategoryString = newCategoryInput.text.toString()
                var newCategoryArray: List<String> = newCategoryString.split("/").map { it.trim() }
                newCategoryArray = newCategoryArray.filter{!it.isEmpty()}

                if(oldCategoryArray == newCategoryArray) {
                    Toast.makeText(this@ViewItemActivity, "Nem változott!", Toast.LENGTH_SHORT).show()
                } else {
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://$currentServerIP:$currentPort/item/change_category"

                    val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                    reqMap["code"] = itemCode
                    if(!newCategoryArray.isEmpty()){
                        reqMap["category"] = newCategoryArray
                    } else {
                        reqMap["category"] = "[]"
                    }

                    val reqBody : JSONObject = JSONObject(reqMap)

                    val stringReq : StringRequest =
                            object : StringRequest(Method.POST, url,
                                    Response.Listener { response ->
                                        findViewById<TextView>(R.id.tvCategory).text = newCategoryString
                                        Toast.makeText(this, "Sikeres kategóriaváltás", Toast.LENGTH_SHORT).show()
                                    },
                                    Response.ErrorListener { error -> }
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

        val mQueue = Volley.newRequestQueue(this)
        val url = "http://$currentServerIP:$currentPort/item/info?code=$itemCode"

        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    scanResult = response
                    viewItem()
                },
                { error -> }
        )

        mQueue.add(jsonObjectRequest)
    }

    private fun viewItem() {

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

        val tvLastChanged : TextView = findViewById(R.id.tvLastChanged)
        tvLastChanged.text = scanResult.getString("lastChanged")

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