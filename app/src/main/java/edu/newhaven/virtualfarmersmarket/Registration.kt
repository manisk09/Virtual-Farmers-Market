package edu.newhaven.virtualfarmersmarket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_registration.pb_progress

class Registration : AppCompatActivity() {

    private lateinit var txtUserFirstName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPhoneNumber: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPassword: EditText
    private lateinit var txtSearchLimit: EditText
    private lateinit var bRegister: Button
    private lateinit var bLocation: Button
    private lateinit var tvUserButton: TextView
    private lateinit var pbProgress: ProgressBar
    private lateinit var auth: FirebaseAuth
    private var firebaseUserID: String = ""

    private val TAG = javaClass.name
    private val db = FirebaseFirestore.getInstance()
    private var actualLocationLatitude: Double = 0.0
    private var actualLocationLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        txtUserFirstName = findViewById(R.id.txt_userFirstName)
        txtEmail = findViewById(R.id.txt_email)
        txtPhoneNumber = findViewById(R.id.txt_phoneNumber)

        txtPassword = findViewById(R.id.txt_password)
        txtConfirmPassword = findViewById(R.id.txt_confirmPassword)
        txtSearchLimit = findViewById(R.id.txt_searchLimit)
        bRegister = findViewById(R.id.b_register)
        bLocation = findViewById(R.id.b_location)
        tvUserButton = findViewById(R.id.tv_userButton)

        bRegister.visibility = View.INVISIBLE;

        auth = FirebaseAuth.getInstance()
        pbProgress = findViewById(R.id.pb_progress)

        if (auth.currentUser != null) {
            val intent = Intent(this, BuyersHomePage::class.java)
            startActivity(intent)
        }

        bLocation.setOnClickListener {
            addingLocation()
        }

        b_register.setOnClickListener {
            val email: String = txtEmail.text.toString()
            val password: String = txtPassword.text.toString()

            val confirmPass: String = txtConfirmPassword.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(
                    this@Registration, "Email required",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password.isEmpty()) {   //checking for user name
                Toast.makeText(
                    this@Registration, "Password required",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (confirmPass.isEmpty()) {    //check for a password
                Toast.makeText(
                    this@Registration, "Confirm the password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (password != confirmPass) {    //check for matching passwords
                Toast.makeText(
                    this@Registration, "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (password.length < 6) {    //check password length
                Toast.makeText(
                    this@Registration, "The password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (!hasFineLocationPermission()) {
                Toast.makeText(
                    this@Registration,
                    "Access to location is mandatory!! \n Add location to continue with Registration.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            else {
                pb_progress.visibility = View.VISIBLE
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            firebaseUserID = auth.currentUser!!.uid
                            addToUserCollection()
                            val intent = Intent(this, BuyersHomePage::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@Registration,
                                "Error Message: " + task.exception!!.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            pb_progress.visibility = View.GONE
                        }
                    }
            }
        }

        tvUserButton.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun hasFineLocationPermission() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun addingLocation() {
        pb_progress.visibility = View.VISIBLE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    Log.d(TAG, "Last know location is $loc")
                    if (loc != null) {
                        actualLocationLatitude = loc.latitude
                        actualLocationLongitude = loc.longitude
                        pb_progress.visibility = View.GONE
                        bRegister.visibility = View.VISIBLE;
                    }
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
                    addingLocation()
                } else {
                    pb_progress.visibility = View.GONE
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    Toast.makeText(
                        this@Registration,
                        "Access to location is mandatory!! \n CLICK ON Add location to continue with Registration.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("PermissionsRequest", "Permission Denied")
                }
                return
            }
        }
    }

    private fun addToUserCollection(){

        val myUser = mutableMapOf(
            "userID" to firebaseUserID,
            "emailAddress" to txtEmail.text.toString(),
            "preferredName" to txtUserFirstName.text.toString(),
            "phoneNbr" to txtPhoneNumber.text.toString(),
            "searchLimit" to txtSearchLimit.text.toString(),
            "currentLatitude" to actualLocationLatitude,
            "currentLongitude" to actualLocationLongitude
        )

        db.collection("users")
            .add(myUser)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    TAG,
                    "DocumentSnapshot written with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }
}


