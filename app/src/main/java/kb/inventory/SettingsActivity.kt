package kb.inventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.nio.charset.Charset

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

                with (sharedPref.edit()) {
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

                with (sharedPref.edit()) {
                    putInt("server_port", newServerPort)
                    apply()
                }
                tvPort.text = newServerPort.toString()
                Toast.makeText(this, "Sikeres módosítás", Toast.LENGTH_SHORT).show()
            }

            editServerPortDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            editServerPortDialog.show()
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

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}