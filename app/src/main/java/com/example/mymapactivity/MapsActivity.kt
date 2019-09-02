package com.example.mymapactivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap

    private val homelat: Double = 28.7501
    private val homelong: Double = 77.1177
    private val destination: LatLng = LatLng(homelat, homelong)
    private val radius: Double = 1.0
    private var curlat: Double = homelat
    private var curlong: Double = homelong
    private var origin: LatLng = LatLng(curlat, curlong)
    private var distance: Float = 0.0F
    private val LOCATION_REQ = 123
    private val CHECK_REQ = 121
    private lateinit var mCurrLocationMarker: Marker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        checkUserSettingAndGetLocation()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        val latLngList = ArrayList<LatLng>()
//        latLngList.add(LatLng(56.952503, 24.083719))
//        latLngList.add(LatLng(55.877526, 26.533898))
//        val encodedPath = PolyUtil.encode(latLngList)

        // Add + a marker in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(destination).title("Marker in Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(destination))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))

//        curlat = intent.getDoubleExtra("myLat", 0.0)
//        curlong = intent.getDoubleExtra("myLong", 0.0)


        //Place current location marker
        val markerOptions = MarkerOptions()
        markerOptions.position(origin)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = mMap.addMarker(markerOptions)


        distance = distanceCal(homelat, homelong, curlat, curlong)
        if (distance > radius) {
            //        //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
            notificationUpdate()
            val url = getMapsApiDirectionsUrl(destination, origin)
            val downloadTask = ReadTask()
            downloadTask.execute(url)
        }



//        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0F))

//        val url = getMapsApiDirectionsUrl(desti, origin)
//        val downloadTask = ReadTask()
//        downloadTask.execute(url)
    }

    private fun notificationUpdate() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    "first",
                    "default",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val i = Intent(this, MapsActivity::class.java)
        i.action = Intent.ACTION_VIEW
//        i.data = Uri.parse("https://www.google.com")

        val pi = PendingIntent.getActivity(this, 123, i, PendingIntent.FLAG_UPDATE_CURRENT)

        val clickableNotification = NotificationCompat.Builder(this, "first")
            .setContentTitle("Warning")
            .setContentText("You are out of the required zone")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        nm.notify(1, clickableNotification)
    }

    private fun distanceCal(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    // to get the direction
    private fun getMapsApiDirectionsUrl(origin: LatLng, dest: LatLng): String {
        // Origin of route
        val strOrigin = "desti=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val strDest = "origin=" + dest.latitude + "," + dest.longitude
        // Sensor enabled
        val sensor = "sensor=false"
        // Building the parameters to the web service
        val parameters = "$strOrigin&$strDest&$sensor"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    @SuppressLint("StaticFieldLeak")
    inner class ReadTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url: String): String {
            // TODO Auto-generated method stub
            var data = ""
            try {
                val http = MapHttpConnection()
                data = http.readUr(url[0])
            } catch (e: Exception) {
                // TODO: handle exception
                Log.d("Background Task", e.toString())
            }
            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            ParserTask().execute(result)
        }
    }

    inner class MapHttpConnection {
        @SuppressLint("LongLogTag")
        @Throws(IOException::class)
        fun readUr(mapsApiDirectionsUrl: String): String {
            var data = ""
            var istream: InputStream? = null
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL(mapsApiDirectionsUrl)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connect()
                istream = urlConnection.inputStream
                val br = BufferedReader(InputStreamReader(istream) as Reader?)
                val sb = StringBuffer()
                val line = br.readLine()
                while ((line) != null)
                    sb.append(line)
                data = sb.toString()
                br.close()
            } catch (e: Exception) {
                Log.d("Exception while reading url", e.toString())
            } finally {
                istream?.close()
                urlConnection?.disconnect()
            }
            return data
        }
    }

    inner class PathJSONParser {
        fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {
            val routes = ArrayList<List<HashMap<String, String>>>()
//            var jRoutes: JSONArray? = null
//            var jLegs: JSONArray? = null
//            var jSteps: JSONArray? = null
            try {
                val jRoutes = jObject.getJSONArray("routes")
                for (i in 0 until jRoutes.length()) {
                    val jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                    val path = ArrayList<HashMap<String, String>>()
                    for (j in 0 until jLegs.length()) {
                        val jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")
                        for (k in 0 until jSteps.length()) {
//                            var polyline = ""
                            val polyline =
                                ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points") as String
                            val list = decodePoly(polyline)
                            for (l in 0 until list.size) {
                                val hm = HashMap<String, String>()
                                hm["lat"] = list[l].latitude.toString()
                                hm["lng"] = list[l].longitude.toString()
                                path.add(hm)
                            }
                        }
                        routes.add(path)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }

        private fun decodePoly(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
                lng += dlng
                val p = LatLng(
                    ((lat.toDouble() / 1E5)),
                    ((lng.toDouble() / 1E5))
                )
                poly.add(p)
            }
            return poly
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
        override fun doInBackground(
            vararg jsonData: String
        ): List<List<HashMap<String, String>>>? {
//             TODO Auto-generated method stub
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = PathJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }

        override fun onPostExecute(routes: List<List<HashMap<String, String>>>) {
            var points: ArrayList<LatLng>?
            var polyLineOptions: PolylineOptions? = null
            // traversing through routes
            for (i in 0 until routes.size) {
                points = ArrayList()
                polyLineOptions = PolylineOptions()
                val path = routes[i]
                for (j in 0 until path.size) {
                    val point = path.get(j)
                    val lat = java.lang.Double.parseDouble(point.get("lat")!!)
                    val lng = java.lang.Double.parseDouble(point.get("lng")!!)
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                polyLineOptions.addAll(points)
                polyLineOptions.width(4F)
                polyLineOptions.color(Color.BLUE)
            }
            mMap.addPolyline(polyLineOptions)
        }
    }

    private fun checkUserSettingAndGetLocation() {

        val locationRequest = LocationRequest().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val client = LocationServices.getSettingsClient(this)

        //returns the modified settings location
        client.checkLocationSettings(request).apply {
            addOnSuccessListener {
                checkAndStartLocationUpdates()
                //call a function to start the location updates
            }
            addOnFailureListener {
                val exception = it as ApiException
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    val resolvable = it as ResolvableApiException
                    resolvable.startResolutionForResult(this@MapsActivity, CHECK_REQ)
                    //this would call to a function onActivityResukt
                    // which would resolve the issue for the failure of response
                }
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_REQ) {
            if (resultCode == Activity.RESULT_OK) {
                checkAndStartLocationUpdates()
                //call a function to start the location updates

            } else {
                Toast.makeText(this, "Enable Location", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndStartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_REQ
                // calls to a function to ask for granting the required permissions
            )
        } else {
            getLocationSmart()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQ) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Please Give ${permissions[i]}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocationSmart() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        client.lastLocation.apply {
            addOnFailureListener {
                Toast.makeText(applicationContext, "$it", Toast.LENGTH_SHORT).show()
            }
            addOnSuccessListener {
                curlat = it.latitude
                curlong = it.longitude
                Toast.makeText(applicationContext, "$curlat,$curlong", Toast.LENGTH_SHORT).show()
//                curloc.text="$curlat,$curlong"
            }
        }
    }

}
