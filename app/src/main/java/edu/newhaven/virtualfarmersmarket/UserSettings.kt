package edu.newhaven.virtualfarmersmarket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user_settings.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_user_settings.pb_progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//private var auth: FirebaseAuth = FirebaseAuth.getInstance()
private lateinit var docID: String

class UserSettings : AppCompatActivity() {

    private val db = Firebase.firestore
    private var myData = FirebaseFirestore.getInstance()

    private val TAG = javaClass.name
    private lateinit var auth: FirebaseAuth
    //private val thisUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        auth = Firebase.auth
        val thisUser = auth.currentUser

        Log.d(TAG, "We got to here")

        val preferredName = findViewById<TextView>(R.id.tv_userFirstName)
        val phoneNbr = findViewById<TextView>(R.id.tv_phoneNumber)
        val mySearchLimit = findViewById<TextView>(R.id.tv_searchLimit)

        //get data
        if (thisUser != null) {
            db.collection("users")
                .whereEqualTo("userID", thisUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        docID = document.id
                        preferredName.text = document.getString("preferredName")
                        phoneNbr.text = document.getString("phoneNbr")
                        mySearchLimit.text = document.getString("searchLimit")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
        //update preferred name
        b_updateMe.setOnClickListener {

          if (et_newUserPreferredName.text.toString() != ""){
            val newPreferredName : String = et_newUserPreferredName.text.toString()
            val  map : MutableMap<String, Any> = mutableMapOf<String, Any>()
            map["preferredName"] = newPreferredName
            updateUserAll(map, docID)
            Log.d(TAG, "${thisUser?.uid} => $map")
          }

          //update the phone number
          if(et_newPhoneNumber.text.toString() != ""){
            val newPhone : String = et_newPhoneNumber.text.toString()
            val  map : MutableMap<String, Any> = mutableMapOf<String, Any>()
            map["phoneNbr"] = newPhone
            updateUserAll(map, docID)
            Log.d(TAG, "${thisUser?.uid} => $map")
          }

          //update search limit
          if(et_newSearchLimit.text.toString() != "") {
            val newSearch : String = et_newSearchLimit.text.toString()
            val  map : MutableMap<String, Any> = mutableMapOf<String, Any>()
            map["searchLimit"] = newSearch
            updateUserAll(map, docID)
            Log.d(TAG, "${thisUser?.uid} => $map")
          }
          val intent = Intent(this, UserSettings::class.java)
          startActivity(intent)
        }

        b_homepage.setOnClickListener{
            val intent = Intent(this, BuyersHomePage::class.java)
            startActivity(intent)
        }

        b_updateLocation.setOnClickListener {
            pb_progress.visibility = View.VISIBLE
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                CoroutineScope(Dispatchers.IO).launch{
                    updateLocation()
                }
            } else {
                requestPermissions()
            }
        }
    }

    private fun updateUserAll( map : MutableMap<String, Any>, docID: String){
        myData
          .collection("users")
          .document(docID)
          .update(map)
    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    Log.d(TAG, "Last know location is $loc")
                    if (loc != null) {
                        myData
                            .collection("users")
                            .document(docID)
                            .update(
                                "currentLatitude", loc.latitude,
                                "currentLongitude", loc.longitude)
                    }
                    pb_progress.visibility = View.GONE
                }
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Log.d("PermissionsRequest", "Permission Granted for ${permissions[0]}")
                    updateLocation()
                } else {
                    pb_progress.visibility = View.GONE
                    Log.d("PermissionsRequest", "Permission Denied")
                }
                return
            }
        }
    }
}
