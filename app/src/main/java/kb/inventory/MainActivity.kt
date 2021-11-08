package kb.inventory

import android.content.DialogInterface
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            var intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val takeOutButton: ImageButton = findViewById(R.id.btnTakeOut)
        takeOutButton.setOnClickListener {
            lastPressedButton = "takeOutButton"
            var intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }

        val checkButton: ImageButton = findViewById(R.id.btnCheck)
        checkButton.setOnClickListener {
            lastPressedButton = "checkButton"
            var intentIntegrator: IntentIntegrator = IntentIntegrator(this)
            intentIntegrator.setPrompt("Világítás: hangerő fel gomb")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.captureActivity = Capture::class.java
            intentIntegrator.initiateScan()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.v("mylog", requestCode.toString())
        Log.v("mylog", resultCode.toString())
        val intentResult: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        //Log.v("mylog", intentResult.contents)
        if(intentResult.contents != null){
            /*var alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Sikerült :)")*/
            Log.v("mylog", intentResult.contents)
            /*alertDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener())
            alertDialogBuilder.show()*/
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
            Log.v("mylog", "megszakadt")
            Toast.makeText(applicationContext, "Beolvasás megszakítva", Toast.LENGTH_SHORT).show()
        }
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