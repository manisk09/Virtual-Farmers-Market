package edu.newhaven.virtualfarmersmarket

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.product_listing_buyer_view_holder.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat

class ProductListingAdapter(options: FirestoreRecyclerOptions<Product>,
                            private val onDataChanged: OnDataChanged) :
    FirestoreRecyclerAdapter<Product, ProductListingBuyerViewHolder>(options){

    interface OnDataChanged {
        fun dataChanged()
    }

    private val dbProductAdapterBuyer = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth  //Needed to check login
    private val TAG = javaClass.name

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductListingBuyerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_listing_buyer_view_holder, parent, false)
        return ProductListingBuyerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductListingBuyerViewHolder,
        position: Int,
        model: Product
    ) {
        auth = Firebase.auth
        val thisUser = auth.currentUser
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, ProductDetailsBuyer::class.java).apply {
                putExtra("SelectedProduct", model)
            }
            holder.itemView.context.startActivity(intent)
        }

        var sellerEmail: String = ""
        var sellerName: String = ""
        var buyerEmail: String = ""
        var buyerName: String = ""
        var buyerPhone: String = ""
        dbProductAdapterBuyer.collection("users")
            .whereEqualTo("userID", model.user)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    sellerEmail = document.getString("emailAddress").toString()
                    sellerName = document.getString("preferredName").toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        holder.itemView.bt_productListingVhBuy.setOnClickListener {
            if (thisUser != null){
                Log.d(TAG, "the firebase id is ${thisUser.uid}")
                Log.d(TAG, "User is in")

                dbProductAdapterBuyer.collection("users")
                    .whereEqualTo("userID", thisUser.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            buyerEmail = document.getString("emailAddress").toString()
                            buyerPhone = document.getString("phoneNbr").toString()
                            buyerName = document.getString("preferredName").toString()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }

                val emailDialogBuilder = AlertDialog.Builder(holder.itemView.context)

                emailDialogBuilder.setMessage("Please Confirm if you are really interested " +
                        "in this product, if not, click on Cancel.")
                    .setCancelable(false)
                    .setPositiveButton("Confirm", DialogInterface.OnClickListener {
                            dialog, id ->
                        GlobalScope.launch { // or however you do background threads
                            Mailer.sendMailToSeller(
                                sellerName,
                                sellerEmail,
                                buyerEmail,
                                buyerPhone,
                                model.product
                            )
                            Mailer.sendMailToBuyer(buyerName, buyerEmail, model.product)
                        }
                        dialog.cancel()
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })

                val alert = emailDialogBuilder.create()
                alert.setTitle("Are you sure?")
                alert.show()

            } else {
                Toast.makeText(holder.itemView.context, "YOU MUST LOGIN TO BUY A PRODUCT", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Not logged in")
            }
        }

        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        val storageReference = Firebase.storage.getReferenceFromUrl(model.imageLoc)
        GlideApp
            .with(holder.productImagePL)
            .load(storageReference)
            .placeholder(circularProgressDrawable)
            .into(holder.productImagePL)

        //holder.distanceLogo.visibility = View.INVISIBLE;
        holder.productName.text = model.product
        holder.productPrice.text = model.price.replace("$","")
        holder.sellerDistance.text = model.distance
    }

    override fun onDataChanged() {
        super.onDataChanged()
        onDataChanged.dataChanged()
    }

    fun updateAllDistances(loc: Location?) {
        snapshots.forEach {
            val destination = Location("")
            destination.latitude = it.latitude
            destination.longitude = it.longitude
            //destination.latitude = it.actualLocation.latitude
            //destination.longitude = it.actualLocation.longitude
            val distanceMeters = loc?.distanceTo(destination)
            val distanceMiles = distanceMeters?.times(0.000621371)
            if (distanceMiles != null) {
                val df = DecimalFormat("#.#")
                df.roundingMode = RoundingMode.CEILING
                it.distance = "${df.format(distanceMiles)} miles" // rounded to 1 decimal place
            }
        }
        notifyDataSetChanged()
    }
}