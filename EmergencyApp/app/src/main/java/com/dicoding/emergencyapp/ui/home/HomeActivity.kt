package com.dicoding.emergencyapp.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dicoding.emergencyapp.R
import com.dicoding.emergencyapp.databinding.ActivityHomeBinding
import com.dicoding.emergencyapp.ui.googlemapsactivity.MapsActivity
import com.dicoding.emergencyapp.ui.help.HelpFragment
import com.dicoding.emergencyapp.ui.settings.SettingsFragment
import com.dicoding.emergencyapp.ui.sos.SosFragment
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val sosFragment = SosFragment()
    private val helpFragment = HelpFragment()
    private val settingsFragment = SettingsFragment()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var cityName: String? = null
    private var longitude: Double? = null
    private var latitude: Double? = null
    private val bundle = Bundle()

    companion object{
        //Unique int for permission ID
        private const val PERMISSION_ID = 1000
        const val LONGITUDE_KEY = "longitude"
        const val LATITUDE_KEY = "latitude"
        const val CITY_KEY = "city"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sosFragment.arguments = bundle
        replaceFragment(sosFragment)
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.ic_sos -> replaceFragment(sosFragment)
                R.id.ic_help -> replaceFragment(helpFragment)
                R.id.ic_settings -> replaceFragment(settingsFragment)
            }
            true
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        binding.toolbarHome.seeLocationBtn.setOnClickListener {
            if (cityName!= null){
                val locationIntent = Intent(this@HomeActivity,MapsActivity::class.java)
                locationIntent.putExtra(LONGITUDE_KEY,longitude)
                locationIntent.putExtra(LATITUDE_KEY,latitude)
                locationIntent.putExtra(CITY_KEY,cityName)
                startActivity(locationIntent)
            } else {
                Toast.makeText(this,"Please allow app to access location service.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation(){
        if(checkPermission()){
            if (isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                    var location: Location? = task.result
                    if (location == null){
                        //If the location is null we will get the new user location
                        //So we need to create a new function
                        getNewLocation()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                        bundle.putDouble("long",location.longitude)
                        bundle.putDouble("lat",location.latitude)
                        cityName = getCityName(location.latitude,location.longitude)
                        binding.toolbarHome.tvLocation.text = cityName
                    }
                }
            } else {
                Toast.makeText(this,"Please enable your location service", Toast.LENGTH_SHORT).show()
            }
        } else {
            RequestPermission()
        }

    }

    private fun getNewLocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            latitude = lastLocation.latitude
            longitude = lastLocation.longitude
            bundle.putDouble("long",lastLocation.longitude)
            bundle.putDouble("lat",lastLocation.latitude)
            cityName = getCityName(lastLocation.latitude,lastLocation.longitude)
            binding.toolbarHome.tvLocation.text = cityName
        }
    }

    private fun checkPermission():Boolean{
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_ID
        )
    }

    private fun isLocationEnabled():Boolean{

        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getCityName(lat:Double,long: Double):String{
        var cityName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var address = geoCoder.getFromLocation(lat,long,1)
        cityName = address.get(0).locality

        return cityName
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug: ","You Have the Permission")
            }
        }
    }


    private fun replaceFragment(fragment: Fragment){
        if (fragment!=null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container,fragment)
            transaction.commit()
        }
    }
}