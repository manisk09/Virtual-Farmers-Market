package edu.newhaven.virtualfarmersmarket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_product_listing_for_buyer.*
import java.util.*

const val PERMISSION_REQUEST_CODE = 0

class ProductListingForBuyer : AppCompatActivity(), ProductListingAdapter.OnDataChanged {

    private val TAG = javaClass.name

    private val dbProductListingBuyer = FirebaseFirestore.getInstance()

    private lateinit var productListingAdapter: ProductListingAdapter

    private lateinit var categoryFilterView: TextView

    private lateinit var productBank: MutableList<String>
    var productList = arrayListOf<String>()

    private lateinit var bottomNavigationMenuPL: BottomNavigationView
    private lateinit var bottom_navigation_menuPL_login: BottomNavigationView

    private lateinit var auth: FirebaseAuth  //Needed to check login

    private lateinit var categoryFilterForSearch: String

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_listing_for_buyer)

        auth = Firebase.auth
        val thisUser = auth.currentUser

        getAllProductNames()
        //Log.d(TAG, productList.toString())

        val intent = intent
        val categoryFilter = intent.getStringExtra("CategoryClicked")
        if (categoryFilter != null) {
            categoryFilterForSearch = categoryFilter
        }

        categoryFilterView = findViewById(R.id.tv_categoryNamePL)
        val ref: CollectionReference = dbProductListingBuyer.collection("products")
        val query: Query = ref
            .whereEqualTo("category", categoryFilter)
            .whereEqualTo("status", "Added")
            .orderBy("product")

        val options: FirestoreRecyclerOptions<Product> = FirestoreRecyclerOptions.Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()

        categoryFilterView.setText(categoryFilter)

        productListingAdapter = ProductListingAdapter(options, this)

        rv_product_listing_buyer.adapter = productListingAdapter
        rv_product_listing_buyer.layoutManager = LinearLayoutManager(this)

        bottomNavigationMenuPL = findViewById(R.id.bottom_navigation_viewPL)
        bottomNavigationMenuPL.visibility = View.INVISIBLE
        bottom_navigation_menuPL_login = findViewById(R.id.bottom_navigation_viewPL_login)
        bottom_navigation_menuPL_login.visibility = View.INVISIBLE

        if (thisUser != null){
            Log.d(TAG, "the firebase id is ${thisUser.uid}")
            Log.d(TAG, "User is in")
            bottomNavigationMenuPL.visibility = View.VISIBLE
            bottomNavigationMenuPL.menu.getItem(0).isCheckable = false
            bottomNavigationMenuPL.setOnNavigationItemSelectedListener { item ->
                var message = ""
                Log.d(TAG, "The user currently is ${thisUser.uid}")
                when(item.itemId) {
                    R.id.nav_sell_home -> {  //also add this above onCreate: private var auth = FirebaseAuth.getInstance() & thisUser
                        val intent = Intent(this, SellersHomePage::class.java)
                        startActivity(intent)
                        Log.d(TAG, "User is in")
                    }
                    R.id.nav_home -> {
                        val intent = Intent(this, BuyersHomePage::class.java)
                        startActivity(intent)
                    }
                    R.id.nav_settings -> {
                        val intent = Intent(this, UserSettings::class.java)
                        startActivity(intent)
                    }
                    R.id.nav_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        finishAffinity()
                        val intent = Intent(this, BuyersHomePage::class.java)
                        startActivity(intent)
                    }
                }
                Toast.makeText(this, "$message clicked!!", Toast.LENGTH_SHORT).show()
                return@setOnNavigationItemSelectedListener true
            }

        } else {
            Toast.makeText(this, "YOU MUST LOGIN TO BUY A PRODUCT", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Not logged in")
            bottom_navigation_menuPL_login.visibility = View.VISIBLE
            bottom_navigation_menuPL_login.menu.getItem(0).isCheckable = false
            bottom_navigation_menuPL_login.setOnNavigationItemSelectedListener { item ->
                var message = ""
                Log.d(TAG, "The user currently is ${thisUser?.uid}")
                when(item.itemId) {
                    R.id.nav_user_login -> {
                        message = "login"
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                    }
                    R.id.nav_user_registration -> {
                        val intent = Intent(this, Registration::class.java)
                        startActivity(intent)
                    }
                }
                Toast.makeText(this, "$message clicked!!", Toast.LENGTH_SHORT).show()
                return@setOnNavigationItemSelectedListener true
            }
        }

    }

    private fun updateDistances() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    //Log.d(TAG, loc.latitude.toString())
                    //Log.d(TAG, loc.longitude.toString())
                    Log.d(TAG, "Last know location is $loc")
                    productListingAdapter.updateAllDistances(loc)
                }
        } else {
            requestPermissions()
        }

    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
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
                    updateDistances()

                } else {
                    Log.d("PermissionsRequest", "Permission Denied")
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        productListingAdapter.startListening()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationMenuPL.getMenu().getItem(0).setChecked(false);
    }

    override fun onStop() {
        super.onStop()
        productListingAdapter.stopListening()
    }

    override fun dataChanged() {
        updateDistances()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu!!.findItem(R.id.search_menu)

        searchView = menuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(product: String?): Boolean {
                searchData(product)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //searchData(newText)
                if (newText == "") {
                    default()
                } else {
                    searchData(newText)
                }
                return false
            }

        })

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchView.onActionViewCollapsed();
                default()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun default() {
        val ref: CollectionReference = dbProductListingBuyer.collection("products")
        val query: Query = ref
            .whereEqualTo("category", categoryFilterForSearch)
            .whereEqualTo("status", "Added")
            .orderBy("product")

        val options: FirestoreRecyclerOptions<Product> = FirestoreRecyclerOptions.Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()
        productListingAdapter.updateOptions(options)
    }

    private fun searchData(product: String?) {

        val ref: CollectionReference = dbProductListingBuyer.collection("products")

        var results = mutableListOf<String>()

        results.clear()

        productList.forEach {
            if (product != null && results.size < 10) {
                if(it.toUpperCase(Locale.ROOT).contains(product.toUpperCase(Locale.ROOT)) ) {
                    results.add(it)
                }
            }
        }

        if(results.isEmpty()) {
            val query: Query = ref
                .whereEqualTo("category", "")

            val options: FirestoreRecyclerOptions<Product> = FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product::class.java)
                .build()
            productListingAdapter.updateOptions(options)
        } else {
            val query: Query = ref.whereIn("product", results).whereEqualTo(
                "category",
                categoryFilterForSearch
            )

            val options: FirestoreRecyclerOptions<Product> = FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product::class.java)
                .build()
            productListingAdapter.updateOptions(options)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun getAllProductNames() {

        dbProductListingBuyer.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    this.productBank = mutableListOf()
                    document.getString("product")?.let {
                        productBank.add(it)
                        productList.add(it)
                    }
                    Log.d(TAG, "${document.id} => ${document.getString("product")}")
                }
                for(item in productList){
                    Log.d("in the list", item)
                }

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}
