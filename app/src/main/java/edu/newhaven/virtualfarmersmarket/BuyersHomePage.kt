package edu.newhaven.virtualfarmersmarket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class BuyersHomePage : AppCompatActivity() {

    private val TAG = javaClass.name

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var categoryOne: TextView
    private lateinit var categoryOneImage: CircleImageView
    private lateinit var categoryTwo: TextView
    private lateinit var categoryTwoImage: CircleImageView
    private lateinit var categoryThree: TextView
    private lateinit var categoryThreeImage: CircleImageView
    private lateinit var categoryFour: TextView
    private lateinit var categoryFourImage: CircleImageView
    private lateinit var categoryFive: TextView
    private lateinit var categoryFiveImage: CircleImageView
    private lateinit var searchButton: ImageView

    private lateinit var categoryList: MutableList<String>

    private lateinit var bottom_navigation_menu: BottomNavigationView
    private lateinit var bottom_navigation_menu_login: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyers_home_page)
        //registerPushToken()

        auth = Firebase.auth
        val thisUser = auth.currentUser


        Log.d(TAG, "The user currently is ${auth.currentUser?.uid}")

        categoryOne = findViewById(R.id.tv_categoryOne)
        categoryOneImage = findViewById(R.id.iv_categoryOneImage)
        categoryTwo = findViewById(R.id.tv_categoryTwo)
        categoryTwoImage = findViewById(R.id.iv_categoryTwoImage)
        categoryThree = findViewById(R.id.tv_categoryThree)
        categoryThreeImage = findViewById(R.id.iv_categoryThreeImage)
        categoryFour = findViewById(R.id.tv_categoryFour)
        categoryFourImage = findViewById(R.id.iv_categoryFourImage)
        categoryFive = findViewById(R.id.tv_categoryFive)
        categoryFiveImage = findViewById(R.id.iv_categoryFiveImage)

        db.collection("categories").get()
            .addOnSuccessListener { documents ->
                this.categoryList = mutableListOf()
                for (document in documents) {
                    document.getString("category")?.let { categoryList.add(it) }
                    Log.d(TAG, "${document.id} => ${document.getString("category")}")
                }
                categoryOne.setText(categoryList[0])
                categoryTwo.setText(categoryList[1])
                categoryThree.setText(categoryList[2])
                categoryFour.setText(categoryList[3])
                categoryFive.setText(categoryList[4])
            }

        categoryOne.setOnClickListener{view: View -> productListing(categoryOne.text)}
        categoryOneImage.setOnClickListener {view: View -> productListing(categoryOne.text)}
        categoryTwo.setOnClickListener{view: View -> productListing(categoryTwo.text)}
        categoryTwoImage.setOnClickListener {view: View -> productListing(categoryTwo.text)}
        categoryThree.setOnClickListener{view: View -> productListing(categoryThree.text)}
        categoryThreeImage.setOnClickListener {view: View -> productListing(categoryThree.text)}
        categoryFour.setOnClickListener{view: View -> productListing(categoryFour.text)}
        categoryFourImage.setOnClickListener {view: View -> productListing(categoryFour.text)}
        categoryFive.setOnClickListener{view: View -> productListing(categoryFive.text)}
        categoryFiveImage.setOnClickListener {view: View -> productListing(categoryFive.text)}

        bottom_navigation_menu = findViewById(R.id.bottom_navigation_view)
        bottom_navigation_menu.visibility = View.INVISIBLE
        bottom_navigation_menu_login = findViewById(R.id.bottom_navigation_view_login)
        bottom_navigation_menu_login.visibility = View.INVISIBLE

        if (thisUser != null){
            Log.d(TAG, "the firebase id is ${thisUser.uid}")
            Log.d(TAG, "User is in")
            bottom_navigation_menu.visibility = View.VISIBLE
            bottom_navigation_menu.selectedItemId = R.id.nav_home
            bottom_navigation_menu.setOnNavigationItemSelectedListener { item ->
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
                    R.id.nav_settings ->  {
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
            Toast.makeText(this, "You must log in to alert a seller.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Not logged in")
            bottom_navigation_menu_login.visibility = View.VISIBLE
            bottom_navigation_menu_login.menu.getItem(0).isCheckable = false
            bottom_navigation_menu_login.setOnNavigationItemSelectedListener { item ->
                var message = ""
                Log.d(TAG, "The user currently is ${thisUser?.uid}")
                when(item.itemId) {
                    R.id.nav_user_login ->  {
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

    override fun onResume() {
        super.onResume()
        bottom_navigation_menu.getMenu().getItem(1).setChecked(true);
    }

    private fun productListing(categoryClicked: CharSequence){
        val intent = Intent(this, ProductListingForBuyer::class.java)
        intent.putExtra("CategoryClicked", categoryClicked)
        startActivity(intent)
    }

}

