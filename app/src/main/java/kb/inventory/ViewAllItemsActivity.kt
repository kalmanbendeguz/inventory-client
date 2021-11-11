package kb.inventory

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
import kb.inventory.adapter.ItemAdapter
import kb.inventory.data.Category
import kb.inventory.data.Item
import org.json.JSONObject

class ViewAllItemsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    lateinit var recyclerView: RecyclerView
    lateinit var itemsList: MutableList<Item>
    lateinit var adapter: ItemAdapter

    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_items)
        Log.v("mylog", "via1")
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

        itemsList = mutableListOf()

        adapter = ItemAdapter(itemsList)

        recyclerView = findViewById(R.id.allItemsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        url = "http://192.168.137.1:3000/item/get_all"

        extractItems()
    }

    private fun extractItems() {
        var requestQueue : RequestQueue = Volley.newRequestQueue(this)
        var jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, // method
                url, // url
                null, // json request
                {response -> // response listener
                    Log.v("mylog", "fullresponse")
                    Log.v("mylog", response.toString())
                    for (i in 0 until response.length()){

                        val itemObject: JSONObject = response.getJSONObject(i)
                        Log.v("mylog", "respons")
                        Log.v("mylog", itemObject.toString())
                        var itemName: String = itemObject.getString("name")
                        var itemCode: String = itemObject.getString("code")
                        var itemQuantity: Int = itemObject.getInt("quantity")

                        Log.v("mylog", "categoryCode")
                        Log.v("mylog", itemObject.toString())
                        //val itemQuantity: Int = itemObject.getInt("quantity")

                        //val itemCategoryList: MutableList<String> = mutableListOf()

                        //val itemCategoryArray: JSONArray = itemObject.getJSONArray("categoryStringArray")

                        Log.v("mylog", "for1")
                        var item: Item = Item(itemCode, itemName, itemQuantity)
                        itemsList.add(item)
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

            }
            R.id.nav_categories -> {
                val intent = Intent(this, ViewCategoriesActivity::class.java).apply {
                    putExtra("category_code", "" )
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