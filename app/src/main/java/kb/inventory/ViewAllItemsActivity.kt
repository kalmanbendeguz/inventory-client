package kb.inventory

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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kb.inventory.adapter.CategoryAdapter
import kb.inventory.adapter.ItemAdapter
import kb.inventory.data.Category
import kb.inventory.data.Item
import org.json.JSONObject
import java.lang.Exception
import java.nio.charset.Charset

class ViewAllItemsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    lateinit var recyclerView: RecyclerView
    lateinit var itemsList: MutableList<Item>
    lateinit var adapter: ItemAdapter

    lateinit var url: String
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_items)

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

        itemsList = mutableListOf()

        adapter = ItemAdapter(itemsList)

        recyclerView = findViewById(R.id.allItemsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        url = "http://$currentServerIP:$currentPort/item/get_all"

        try {
            val categoryID: String = intent.getStringExtra("category_id")!!
            url = "http://$currentServerIP:$currentPort/item/get_all_of_subcategory?category_id=$categoryID"
            val tvToolbarLabel : TextView = findViewById(R.id.toolbarLabel)
            tvToolbarLabel.text = "Kategórián belüli tételek"
        } catch (e : Exception){
            url = "http://$currentServerIP:$currentPort/item/get_all"
        }

        extractItems()

        val fab : FloatingActionButton = findViewById(R.id.fabSearch)
        fab.setOnClickListener {
            val searchItemDialog = AlertDialog.Builder(this@ViewAllItemsActivity)
            searchItemDialog.setTitle("Keresés:")

            val searchInput = EditText(this@ViewAllItemsActivity)
            searchInput.inputType = InputType.TYPE_CLASS_TEXT

            searchItemDialog.setView(searchInput)

            searchItemDialog.setPositiveButton("OK") { dialogInterface, i ->
                val search = searchInput.text.toString()

                if(search.isEmpty()) {
                    Toast.makeText(this@ViewAllItemsActivity, "Nem lehet üres!", Toast.LENGTH_SHORT).show()
                } else {

                    adapter.items = itemsList.filter {
                        Regex.fromLiteral(it.name.toUpperCase()).containsMatchIn(search.toUpperCase()) || Regex.fromLiteral(search.toUpperCase()).containsMatchIn(it.name.toUpperCase())
                        //it.name.toUpperCase() == search
                    } as MutableList<Item>
                    adapter.notifyDataSetChanged()
                }
            }

            searchItemDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            searchItemDialog.show()

        }
    }

    private fun extractItems() {
        val requestQueue : RequestQueue = Volley.newRequestQueue(this)
        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, // method
                url, // url
                null, // json request
                {response -> // response listener

                    for (i in 0 until response.length()){

                        val itemObject: JSONObject = response.getJSONObject(i)

                        val itemName: String = itemObject.getString("name")
                        val itemCode: String = itemObject.getString("code")
                        val itemQuantity: Int = itemObject.getInt("quantity")

                        val item: Item = Item(itemCode, itemName, itemQuantity)
                        itemsList.add(item)
                        adapter.notifyDataSetChanged()

                    }
                },
                {error -> }
        )

        requestQueue.add(jsonArrayRequest)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_landing -> {
                val intent = Intent(this, MainActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.nav_all_items -> {}
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