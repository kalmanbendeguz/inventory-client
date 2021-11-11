package kb.inventory

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kb.inventory.adapter.CategoryAdapter
import kb.inventory.data.Category
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*

class ViewCategoriesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    lateinit var recyclerView: RecyclerView
    lateinit var categoriesList: MutableList<Category>
    lateinit var adapter: CategoryAdapter

    lateinit var currentCategoryCode: String
    lateinit var url: String
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("mylog", "viewcategories oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_categories)
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

        categoriesList = mutableListOf()

        var tvParentCategory: TextView = findViewById(R.id.tvParentCategory)
        if(intent.getStringExtra("category_path")!!.isEmpty()) {
            tvParentCategory.text = "/"
        } else {
            tvParentCategory.text = intent.getStringExtra("category_path")!!
        }

        adapter = CategoryAdapter(categoriesList, intent.getStringExtra("category_path")!!)
        Log.v("mylog", "viewcategories")
        recyclerView = findViewById(R.id.categoriesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        currentCategoryCode = intent.getStringExtra("category_code")!!
        url = "http://$currentServerIP:$currentPort/category/get_subcategories?parent_category=$currentCategoryCode"
        if(currentCategoryCode.isEmpty()){
            url = "http://$currentServerIP:$currentPort/category/get_subcategories"
        }
        //url = "http://$currentServerIP:$currentPort/category/get_subcategories?parent_category=6179e29491f511fe16bb19e5"
        Log.v("mylog", "oncreate before extract")
        extractCategories()

        val fab : FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val newCategoryDialog = AlertDialog.Builder(this@ViewCategoriesActivity)
            newCategoryDialog.setTitle("Új kategória")

            val newCategoryInput = EditText(this@ViewCategoriesActivity)
            newCategoryInput.inputType = InputType.TYPE_CLASS_TEXT

            newCategoryDialog.setView(newCategoryInput)

            newCategoryDialog.setPositiveButton("OK") { dialogInterface, i ->
                val newCategory = newCategoryInput.text.toString()

                if(newCategory.isEmpty()) {
                    Toast.makeText(this@ViewCategoriesActivity, "Nem lehet üres a kategórianév!", Toast.LENGTH_SHORT).show()
                } else {
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://$currentServerIP:$currentPort/category/new"

                    //val requestBody = "code="+ itemCode + "&quantity="
                    val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                    if(!currentCategoryCode.isEmpty()){
                        reqMap["parent_category_id"] = currentCategoryCode
                    }
                    reqMap["name"] = newCategory

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
                                        //findViewById<TextView>(R.id.tvName).text = newName
                                        categoriesList.clear()
                                        extractCategories()
                                        adapter.notifyDataSetChanged()
                                        Toast.makeText(this, "Kategória létrehozva", Toast.LENGTH_SHORT).show()

                                    },
                                    Response.ErrorListener { error ->
                                        Log.d("mylog", "error => $error")
                                    }
                            ){
                                override fun getBody(): ByteArray {
                                    return reqBody.toString().toByteArray(Charset.defaultCharset())
                                }
                            }
                    queue.add(stringReq)
                }

            }

            newCategoryDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            newCategoryDialog.show()

        }
    }

    private fun extractCategories() {
        Log.v("mylog", "url")
        Log.v("mylog", url)
        var requestQueue : RequestQueue = Volley.newRequestQueue(this)
        var jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, // method
            url, // url
            null, // json request
            {response -> // response listener
                Log.v("mylog", "fullresponse")
                Log.v("mylog", response.toString())
                for (i in 0 until response.length()){

                    val categoryObject: JSONObject = response.getJSONObject(i)
                    Log.v("mylog", "respons")
                    Log.v("mylog", categoryObject.toString())
                    var categoryName: String = categoryObject.getString("name")
                    var categoryCode: String = categoryObject.getString("_id")

                    Log.v("mylog", "categoryCode")
                    Log.v("mylog", categoryObject.toString())
                    //val itemQuantity: Int = itemObject.getInt("quantity")

                    //val itemCategoryList: MutableList<String> = mutableListOf()

                    //val itemCategoryArray: JSONArray = itemObject.getJSONArray("categoryStringArray")

                    Log.v("mylog", "for1")
                    var category: Category = Category(categoryName, categoryCode)
                    categoriesList.add(category)
                    adapter.notifyDataSetChanged()
                    Log.v("mylog", "end of for")

                }
                Log.v("mylog", "after for")

            },
            {error -> // error listener
                Log.v("mylog", error.toString())
            }
        )

        requestQueue.add(jsonArrayRequest)
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
                //DONT
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