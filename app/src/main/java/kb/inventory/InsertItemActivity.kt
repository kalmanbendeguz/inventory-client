package kb.inventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import kb.inventory.thirdparty.UploadUtility
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset


class InsertItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var rootLinearLayout: LinearLayout
    lateinit var navView: NavigationView
    lateinit var itemCode: String
    lateinit var scanResult: JSONObject
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_insert_item)
        sharedPref = getSharedPreferences("kb.inventory.settings", Context.MODE_PRIVATE)

        currentServerIP = sharedPref.getString("server_ip", "0.0.0.0")!!
        currentPort = sharedPref.getInt("server_port", 3000).toString()

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
                        insertNew()
                    } else {
                        scanResult = response
                        insertExisting()
                    }
                },
                { error -> }
        )

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
                val url = "http://$currentServerIP:$currentPort/item/insert_new"

                val nameEditText: EditText = findViewById(R.id.etName)
                val categoryEditText: EditText = findViewById(R.id.etCategory)

                val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                reqMap["code"] = itemCode
                reqMap["name"] = nameEditText.text.toString()
                var categoryArray: List<String> = categoryEditText.text.toString().split("/").map { it.trim() }
                categoryArray = categoryArray.filter{!it.isEmpty()}
                reqMap["quantity"] = quantityEditText.text.toString().toInt()
                var imageStr: String? = null

                if(!categoryArray.isEmpty()){
                    reqMap["category"] = categoryArray
                } else {
                    reqMap["category"] = "[]"
                }

                val reqBody : JSONObject = JSONObject(reqMap)

                val stringReq : StringRequest =
                    object : StringRequest(Method.POST, url,
                            Response.Listener { response ->

                                Toast.makeText(this, "Sikeres bevitel", Toast.LENGTH_SHORT).show()

                                if(photoFile != null) {
                                    UploadUtility(this, "http://$currentServerIP:$currentPort/item/upload_image", photoFile!!).uploadFile() // Either Uri, File or String file path
                                }

                                val intent = Intent(this, MainActivity::class.java).apply {
                                }
                                startActivity(intent)
                            },
                            Response.ErrorListener { error -> }
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
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }

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

            tvCategory.append(" / " + categoryArray[i].toString())
        }
        if(categoryArray.length() == 0) {
            tvCategory.append(" / ")
        }

        okButton.setOnClickListener {
            val quantityEditText : EditText = findViewById(R.id.etInsertQuantity)
            if(quantityEditText.text.isEmpty()){
                Toast.makeText(this, "Add meg a mennyiséget!", Toast.LENGTH_SHORT).show()
            } else {
                val queue = Volley.newRequestQueue(this)
                val url = "http://$currentServerIP:$currentPort/item/insert_existing"

                val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                reqMap["code"] = itemCode
                reqMap["quantity"] = quantityEditText.text.toString().toInt()
                val reqBody : JSONObject = JSONObject(reqMap)

                val stringReq : StringRequest =
                    object : StringRequest(Method.POST, url,
                            Response.Listener { response ->
                                Toast.makeText(this, "Sikeres bevitel", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java).apply {
                                }
                                startActivity(intent)
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