package edu.newhaven.virtualfarmersmarket

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_new_product.*
import java.io.File
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

private var catSelection = ""
private val FILE_NAME = "photo.jpg"
private val CAMERA_REQUEST_CODE = 42
private lateinit var photoFile: File
private val IMAGE_GALLERY_REQUEST_CODE = 123
private val FINE_LOCATION_REQUEST_CODE = 200


private lateinit var storageReference:StorageReference
private var myImgLocation = ""

class AddNewProduct : AppCompatActivity() {
    private val TAG = javaClass.name
    private val db = FirebaseFirestore.getInstance()
    private lateinit var categoryBank: MutableList<String>
    private lateinit var categoryList: Array<String?>
    private var arrayAdapter: ArrayAdapter<String?>? = null

    private var sellerLocationLatitude: Double = 0.0
    private var sellerLocationLongitude: Double = 0.0
    private lateinit var sellerActualLocation: Location
    private lateinit var imageUri : Uri
    private var currentUserDocumentId: String = ""

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        auth = Firebase.auth
        val thisUser = auth.currentUser

        storageReference = FirebaseStorage.getInstance().getReference("image")

        db.collection("categories").get()
            .addOnSuccessListener { documents ->
                this.categoryBank = mutableListOf()
                for (document in documents) {
                    document.getString("category")?.let { categoryBank.add(it) }
                    Log.d(TAG, "${document.id} => ${document.getString("category")}")
                }

                val howMany = categoryBank.size
                categoryList = Array(howMany){null}
                var i = 0

                for (item in categoryBank) {
                    categoryList[i] = item
                    i += 1
                }
                arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item, categoryList)
                spCategories.adapter = arrayAdapter
            }


        spCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            //spinner selection Toast
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(adapterView:  AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(this@AddNewProduct, "You selected ${adapterView?.getItemAtPosition(position).toString()}",
                    Toast.LENGTH_LONG).show()
                catSelection = adapterView?.getItemAtPosition(position).toString()
            }
        }

        //gallery module
        b_gImage.setOnClickListener {
            prepOpenImageGallery()
        }

        //photo module
        b_takePicturePerm.setOnClickListener {
            checkForPermissions(android.Manifest.permission.CAMERA, "camera", CAMERA_REQUEST_CODE)
        }

        b_takePicture.setOnClickListener{
           capturePhoto()
        }

        b_sellerPage.setOnClickListener {
            val intent = Intent(this, SellersHomePage::class.java)
            startActivity(intent)
        }

        //communication with firebase adding the product to the list
        db.collection("products")

        db.collection("users")
            .whereEqualTo("userID", thisUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    //currentUserDocumentId = document.id
                    document.getDouble("currentLatitude")?.let { sellerLocationLatitude = it }
                    document.getDouble("currentLongitude")?.let { sellerLocationLongitude = it }
                    Log.i("sellerActualLocation: ", sellerLocationLatitude.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }


        b_addItNow.setOnClickListener {

            var edtField: EditText? = null
            val loggedInUser = thisUser?.uid
            val productStatus = "Added"
            val imageName = uploadFile()
            val stamp = getDateTime()


            val products = mutableMapOf(
                //takenImage to firebase
                "product" to txtProdName.text.toString(),
                //"productSearch" to txtProdName.text.toString().toUpperCase(),
                "description" to txtDescription.text.toString(),
                "price" to txtPrice.text.toString(),
                "quantity" to txtQuantity.text.toString(),
                "category" to catSelection,
                "user" to loggedInUser,
                "imageLoc" to imageName,
                "postingDate" to stamp,
                "status" to productStatus,
                "latitude" to sellerLocationLatitude,
                "longitude" to sellerLocationLongitude
            )

            val intent = Intent(this, AddNewProduct::class.java)
            startActivity(intent)

            db.collection("products")
                .add(products)
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

    //Checks to see if there is permission for Camera or location
    private fun checkForPermissions(permission: String, name: String, requestCode: Int ) {
        when{
            ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED ->{
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
                b_takePicturePerm.visibility = View.INVISIBLE
                b_takePicture.visibility = View.VISIBLE
            }
            shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

            else -> ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)
        }
    }

    //first level permission for Camera or location
    private fun onRequestForPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        fun innerCheck(name: String){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_LONG).show()
            }  else{
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
                b_takePicturePerm.visibility = View.INVISIBLE
                b_takePicturePerm.visibility = View.VISIBLE
            }
        }
        when(requestCode) {
            FINE_LOCATION_REQUEST_CODE -> innerCheck("location")
            CAMERA_REQUEST_CODE -> innerCheck("camera")
        }
    }

    //permission for Camera or Location
    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(this@AddNewProduct, arrayOf(permission), requestCode)
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Open the gallery
    private fun prepOpenImageGallery() {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(i, "Choose Picture"), 123)
    }

    //Get the image from the camera
    private fun capturePhoto(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    //turn image into type of file needed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null){
            //filepath = data.data!!
            if (requestCode == CAMERA_REQUEST_CODE) {
                val imageBitmap = data.extras?.get("data") as Bitmap
                Log.d(TAG, "the imagebitmap is $imageBitmap")
                val bytes = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                imageUri = getImageUri(imageBitmap)
                iv_prodPicture.setImageBitmap(imageBitmap)

            } else if (requestCode == IMAGE_GALLERY_REQUEST_CODE)  {
                imageUri = data.data!!
                val bitmap:Bitmap  = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                iv_prodPicture.setImageBitmap(bitmap)
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    //assist with camera image
    private fun getImageUri(bitmap: Bitmap): Uri{
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    //prepare the image for upload to the database
    private fun uploadFile() : String{
        val stamp = getDateTime()
        val nameOfImage = ("${stamp}.jpg")
        val imageRef = FirebaseStorage.getInstance().reference
        val pathRef = imageRef.child ("images/$stamp.jpg")
        Log.i("image loc", imageRef.toString() + "images/" + nameOfImage)
        pathRef.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(this,"File Uploaded", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, "File not uploaded", Toast.LENGTH_LONG ).show()
            }
        return imageRef.toString() + "images/" + nameOfImage
    }

    //get the date and time
    private fun getDateTime(): String{
        val stamp = Timestamp(System.currentTimeMillis())
        return Date(stamp.getTime()).toString()
    }
}
