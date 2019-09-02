package com.example.mymapactivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener , LocationListener {
    override fun onLocationChanged(p0: Location?) {
        p0?.let {
            curlat = it.latitude
            curlong = it.longitude

        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    //    private val radius: Double = 1.0
    private var curlat: Double = 0.0
    private var curlong: Double = 0.0
    private var origin: LatLng = LatLng(curlat, curlong)

    private val LOCATION_REQ = 123
    private val CHECK_REQ = 121


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.maps->{
                val intent = Intent(this,MapsActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

//    private fun checkUserSettingAndGetLocation() {
//
//        val locationRequest = LocationRequest().apply {
//            interval = 1000
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//        val request = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)
//            .build()
//
//        val client = LocationServices.getSettingsClient(this)
//
//        //returns the modified settings location
//        client.checkLocationSettings(request).apply {
//            addOnSuccessListener {
//                checkAndStartLocationUpdates()
//                //call a function to start the location updates
//            }
//            addOnFailureListener {
//                val exception = it as ApiException
//                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
//                    val resolvable = it as ResolvableApiException
//                    resolvable.startResolutionForResult(this@MainActivity, CHECK_REQ)
//                    //this would call to a function onActivityResukt
//                    // which would resolve the issue for the failure of response
//                }
//            }
//        }
//
//    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == CHECK_REQ) {
//            if (resultCode == Activity.RESULT_OK) {
//                checkAndStartLocationUpdates()
//                //call a function to start the location updates
//
//            } else {
//                Toast.makeText(this, "Enable Location", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    private fun checkAndStartLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ), LOCATION_REQ
//                // calls to a function to ask for granting the required permissions
//            )
//        } else {
//            getLocationSmart()
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == LOCATION_REQ) {
//            for (i in grantResults.indices) {
//                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(
//                        this,
//                        "Please Give ${permissions[i]}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    return
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun getLocationSmart() {
//        val client = LocationServices.getFusedLocationProviderClient(this)
//        client.lastLocation.apply {
//            addOnFailureListener {
//                Toast.makeText(applicationContext, "$it", Toast.LENGTH_SHORT).show()
//
//            }
//            addOnSuccessListener {
//                curlat = it.latitude
//                curlong = it.longitude
//                Toast.makeText(applicationContext, "$curlat,$curlong", Toast.LENGTH_SHORT).show()
//
//            }
//        }
//    }
//

}
