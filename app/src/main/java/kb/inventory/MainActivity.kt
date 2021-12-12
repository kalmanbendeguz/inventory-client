package kb.inventory

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kb.inventory.qbarcode.Capture

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    var lastPressedButton: String = "none"
    lateinit var sharedPref: SharedPreferences
    lateinit var currentServerIP: String
    lateinit var currentPort: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        val insertButton: ImageButton = findViewById(R.id.btnInsert)
        insertButton.setOnClickListener {
            lastPressedButton = "insertButton"
            val intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val takeOutButton: ImageButton = findViewById(R.id.btnTakeOut)
        takeOutButton.setOnClickListener {
            lastPressedButton = "takeOutButton"
            val intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val checkButton: ImageButton = findViewById(R.id.btnCheck)
        checkButton.setOnClickListener {
            lastPressedButton = "checkButton"
            val intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val intentResult: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(intentResult.contents != null){

            when(lastPressedButton){
                "insertButton" -> {
                    val intent = Intent(this, InsertItemActivity::class.java).apply {
                        putExtra("itemCode", intentResult.contents )
                    }
                    startActivity(intent)
                }
                "takeOutButton" -> {
                    val intent = Intent(this, TakeOutItemActivity::class.java).apply {
                        putExtra("itemCode", intentResult.contents )
                    }
                    startActivity(intent)
                }
                "checkButton" -> {
                    val intent = Intent(this, CheckItemActivity::class.java).apply {
                        putExtra("itemCode", intentResult.contents )
                    }
                    startActivity(intent)
                }
            }

        } else {

            Toast.makeText(applicationContext, "Beolvasás megszakítva", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_landing -> {

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
                val intent = Intent(this, SettingsActivity::class.java).apply {}
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}