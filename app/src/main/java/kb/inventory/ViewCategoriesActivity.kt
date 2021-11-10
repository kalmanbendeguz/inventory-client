package kb.inventory

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import kb.inventory.adapter.CategoryAdapter
import kb.inventory.data.Category
import org.json.JSONArray
import org.json.JSONObject
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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("mylog", "viewcategories oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_categories)

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

        adapter = CategoryAdapter(categoriesList)
        Log.v("mylog", "viewcategories")
        recyclerView = findViewById(R.id.categoriesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        currentCategoryCode = intent.getStringExtra("category_code")!!
        url = "http://192.168.137.1:3000/category/get_subcategories?parent_category=$currentCategoryCode"
        if(currentCategoryCode.isEmpty()){
            url = "http://192.168.137.1:3000/category/get_subcategories"
        }
        //url = "http://192.168.137.1:3000/category/get_subcategories?parent_category=6179e29491f511fe16bb19e5"
        Log.v("mylog", "oncreate before extract")
        extractCategories()


    }

    private fun extractCategories() {
        var requestQueue : RequestQueue = Volley.newRequestQueue(this)
        var jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, // method
            url, // url
            null, // json request
            {response -> // response listener
                for (i in 0 until response.length()){

                    val categoryObject: JSONObject = response.getJSONObject(i)

                    var categoryName: String = categoryObject.getString("name")
                    Log.v("mylog", "respons")
                    var categoryCode: String = categoryObject.getString("_id")

                    Log.v("mylog", "categoryCode")
                    Log.v("mylog", categoryObject.toString())
                    //val itemQuantity: Int = itemObject.getInt("quantity")

                    //val itemCategoryList: MutableList<String> = mutableListOf()

                    //val itemCategoryArray: JSONArray = itemObject.getJSONArray("categoryStringArray")


                    var category: Category = Category(categoryName, categoryCode)
                    categoriesList.add(category)
                    adapter.notifyDataSetChanged()

                }

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