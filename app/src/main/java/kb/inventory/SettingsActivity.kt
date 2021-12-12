package kb.inventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import kb.inventory.data.Item
import org.json.JSONObject


class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
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

        val tvServerIP : TextView = findViewById(R.id.tvServerIP)
        val tvPort: TextView = findViewById(R.id.tvServerPort)

        tvServerIP.text = currentServerIP
        tvPort.text = currentPort

        val cardServerIP : CardView = findViewById(R.id.cardServerIP)
        cardServerIP.setOnClickListener {
            val editServerIPDialog = AlertDialog.Builder(this@SettingsActivity)
            editServerIPDialog.setTitle("Szerver IP:")

            val editServerIPInput = EditText(this@SettingsActivity)
            editServerIPInput.inputType = InputType.TYPE_CLASS_TEXT
            editServerIPInput.setText(currentServerIP)

            editServerIPDialog.setView(editServerIPInput)

            editServerIPDialog.setPositiveButton("OK") { dialogInterface, i ->
                val newServerIP = editServerIPInput.text.toString()

                with(sharedPref.edit()) {
                    putString("server_ip", newServerIP)
                    apply()
                }
                tvServerIP.text = newServerIP
                Toast.makeText(this, "Sikeres módosítás", Toast.LENGTH_SHORT).show()
            }

            editServerIPDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            editServerIPDialog.show()
        }

        val cardServerPort : CardView = findViewById(R.id.cardServerPort)
        cardServerPort.setOnClickListener {
            val editServerPortDialog = AlertDialog.Builder(this@SettingsActivity)
            editServerPortDialog.setTitle("Port:")

            val editServerPortInput = EditText(this@SettingsActivity)
            editServerPortInput.inputType = InputType.TYPE_CLASS_NUMBER
            editServerPortInput.setText(currentPort.toString())

            editServerPortDialog.setView(editServerPortInput)

            editServerPortDialog.setPositiveButton("OK") { dialogInterface, i ->
                val newServerPort : Int = editServerPortInput.text.toString().toInt()

                with(sharedPref.edit()) {
                    putInt("server_port", newServerPort)
                    apply()
                }
                tvPort.text = newServerPort.toString()
                Toast.makeText(this, "Sikeres módosítás", Toast.LENGTH_SHORT).show()
            }

            editServerPortDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            editServerPortDialog.show()
        }

        val cardEmptyStock : CardView = findViewById(R.id.cardEmptyStock)
        cardEmptyStock.setOnClickListener {
            val emptyStockDialog = AlertDialog.Builder(this@SettingsActivity)
            emptyStockDialog.setTitle("Teljes készlet kiürítése")

            val inflater = this.layoutInflater
            val securityCode: Int = (1000..9999).random()
            val dialogView : View = inflater.inflate(R.layout.empty_stock_dialog, null)
            val tvSecurityCode = dialogView.findViewById<TextView>(R.id.empty_stock_security_code)
            tvSecurityCode.text = securityCode.toString()
            val etSecurityCode: EditText = dialogView.findViewById(R.id.etSecurityCode)
            val url = "http://$currentServerIP:$currentPort/item/empty_all"

            emptyStockDialog.setView(dialogView)
            emptyStockDialog.setPositiveButton("OK") { dialogInterface, i ->
                if(etSecurityCode.text.isNotEmpty() && securityCode == etSecurityCode.text.toString().toInt()){
                    val requestQueue : RequestQueue = Volley.newRequestQueue(this)
                    val jsonRequest: JsonObjectRequest = JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        {response ->
                            Toast.makeText(this, "Sikeres készletkiürítés!", Toast.LENGTH_SHORT).show()
                        },
                        {error -> }
                    )
                    requestQueue.add(jsonRequest)

                } else {
                    Toast.makeText(this, "Hibás ellenőrzőkód!", Toast.LENGTH_SHORT).show()
                }

            }
            emptyStockDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }

            emptyStockDialog.show()

        }

        val cardDeleteInventory : CardView = findViewById(R.id.cardDeleteInventory)
        cardDeleteInventory.setOnClickListener {
            val deleteInventoryDialog = AlertDialog.Builder(this@SettingsActivity)
            deleteInventoryDialog.setTitle("Raktár alaphelyzetbe állítása")

            val inflater = this.layoutInflater
            val securityCode: Int = (1000..9999).random()
            val dialogView : View = inflater.inflate(R.layout.delete_inventory_dialog, null)
            val tvSecurityCode = dialogView.findViewById<TextView>(R.id.delete_inventory_security_code)
            tvSecurityCode.text = securityCode.toString()
            val etSecurityCode: EditText = dialogView.findViewById(R.id.etSecurityCode)
            val url = "http://$currentServerIP:$currentPort/inventory/reset_inventory"

            deleteInventoryDialog.setView(dialogView)
            deleteInventoryDialog.setPositiveButton("OK") { dialogInterface, i ->
                if(etSecurityCode.text.isNotEmpty() && securityCode == etSecurityCode.text.toString().toInt()){
                    val requestQueue : RequestQueue = Volley.newRequestQueue(this)
                    val jsonRequest: JsonObjectRequest = JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        {response ->
                            Toast.makeText(this, "Sikeres alaphelyzetbe állítás!", Toast.LENGTH_SHORT).show()
                        },
                        {error -> }
                    )
                    requestQueue.add(jsonRequest)

                } else {
                    Toast.makeText(this, "Hibás ellenőrzőkód!", Toast.LENGTH_SHORT).show()
                }

            }
            deleteInventoryDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }

            deleteInventoryDialog.show()

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

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}