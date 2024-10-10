package com.example.techweartry4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.techweartry4.databinding.ActivityMainBinding
import android.widget.EditText
import android.content.SharedPreferences
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.getSystemService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityMainBinding
    private lateinit var phoneNumberEditText: EditText
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sendButton: Button
    private val predefinedNumber = ""
    private var currentLocation: LatLng? = null
    private var countdownTimer: CountDownTimer? = null
    private val PREF_NAME = "MyPrefs"
    private val PHONE_NUMBER_KEY = "phoneNumber"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        mapView = findViewById(R.id.mapView)
        sendButton = findViewById(R.id.sendButton)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        databaseListner()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedPhoneNumber = sharedPreferences.getString(PHONE_NUMBER_KEY, "")
        val phonenumber = "+91" + phoneNumberEditText.text.toString()
        val editor = sharedPreferences.edit()
        editor.putString(PHONE_NUMBER_KEY,phonenumber)
        editor.apply()

        phoneNumberEditText.setText(savedPhoneNumber)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    val loc = currentLocation
                    loc?.let {
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(it).title("Your Location"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                    }
                }
            }
        }

        sendButton.setOnClickListener {
            showconfirmationdialogue()
            vibe(it)
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationUpdates()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }
    private fun databaseListner(){
        database = FirebaseDatabase.getInstance().getReference()
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var pulse = snapshot.child("User/pulse").value
                val heartrate: Int = pulse.toString().toInt()
                binding.textViesHeartRate.setText(pulse.toString())
                if (heartrate <= 250 || heartrate >= 350){
                    showconfirmationdialogue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to read pulse data", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(postListener)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    private fun showconfirmationdialogue(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you Need Help")
        builder.setMessage("Are you Sure!")
        val phonenumber = phoneNumberEditText.text.toString()
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(PHONE_NUMBER_KEY,phonenumber)
        editor.apply()
        builder.setPositiveButton("Yes"){_,_->
            Toast.makeText(this@MainActivity, "Help has been sent", Toast.LENGTH_SHORT).show()
            currentLocation?.let { loc ->
                val locationUrl = "http://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                if (phonenumber != null){
                sendSMS(phonenumber, "I need help! My location: $locationUrl")
                }else{
                    sendSMS(predefinedNumber,"I need help! My location: $locationUrl")
                }
            }
        }
        builder.setNegativeButton("No"){_,_->
            countdownTimer?.cancel()
            countdownTimer = null
            Toast.makeText(this@MainActivity, "No message is sent", Toast.LENGTH_SHORT).show()

        }

        val dialog = builder.create()
        dialog.show()
        countdownTimer = object : CountDownTimer(10000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                dialog.setMessage("Are you Sure!"
                        +"Sending in...${millisUntilFinished/1000}seconds...")
            }

            override fun onFinish() {
                if (dialog.isShowing){
                    currentLocation?.let { loc ->
                        val locationUrl = "http://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                        if (phonenumber != null){
                            sendSMS(phonenumber, "I need help! My location: $locationUrl")
                            Toast.makeText(this@MainActivity, "Help has been called to relative!", Toast.LENGTH_SHORT).show()
                        }else{
                            sendSMS(predefinedNumber,"I need help! My location: $locationUrl")
                            Toast.makeText(this@MainActivity, "Help has been called to police!", Toast.LENGTH_SHORT).show()

                        }
                    }
                }else{
                    Toast.makeText(this@MainActivity, "Some Error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()


    }
    private fun readData(){
        database = FirebaseDatabase.getInstance().getReference("User").child("pulse")
        database.get().addOnSuccessListener{
            if (it.exists()){
                val heartRate: Int = it.value.toString().toInt()
                Toast.makeText(this,"Success full pulse Reading", Toast.LENGTH_SHORT).show()
                binding.textViesHeartRate.setText(heartRate.toString())
            }else{
                Toast.makeText(this,"The Pulse path do not exists", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(this,"Failed to Read pulse Rate", Toast.LENGTH_SHORT).show()

        }
    }
    fun vibe(v: View){
        val vibrate = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if(Build.VERSION.SDK_INT>= 26){
            vibrate.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }else{
            vibrate.vibrate(400)
        }
    }
}
