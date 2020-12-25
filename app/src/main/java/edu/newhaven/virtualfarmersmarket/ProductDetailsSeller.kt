package edu.newhaven.virtualfarmersmarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_product_details_buyer.iv_product_image_PDB
import kotlinx.android.synthetic.main.activity_product_details_buyer.tv_category_PDB
import kotlinx.android.synthetic.main.activity_product_details_buyer.tv_product_description_PDB
import kotlinx.android.synthetic.main.activity_product_details_buyer.tv_product_name_PDB
import kotlinx.android.synthetic.main.activity_product_details_buyer.tv_product_price_PDB
import kotlinx.android.synthetic.main.activity_product_details_buyer.tv_quantity_PDB
import kotlinx.android.synthetic.main.activity_product_details_seller.*
import kotlinx.android.synthetic.main.view_holder.*
import kotlinx.android.synthetic.main.view_holder.view.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
private val db = FirebaseFirestore.getInstance()
//private var auth: FirebaseAuth = FirebaseAuth.getInstance()

class ProductDetailsSeller : AppCompatActivity() {

  private val TAG = javaClass.name

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_product_details_seller)

    val intent = intent
    val prodID = intent.getStringExtra("myProdId")

    val product = intent.getSerializableExtra("SelectedProduct") as? Product ?: return

    val circularProgressDrawable = CircularProgressDrawable(this)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    val storageReference = Firebase.storage.getReferenceFromUrl(product.imageLoc)
    GlideApp
      .with(this)
      .load(storageReference)
      .placeholder(circularProgressDrawable)
      .into(iv_product_image_PDB)

    tv_product_name_PDB.text = product.product
    tv_category_PDB.text = product.category
    tv_product_description_PDB.text = product.description
    tv_product_price_PDB.text = product.price.replace("$","")
    tv_quantity_PDB.text = product.quantity

    if (product.status == "Sold"){
      b_soldOut.visibility = View.INVISIBLE
      b_forSale.visibility = View.VISIBLE
    }

    b_editProd.setOnClickListener {
      b_editProd.visibility = View.INVISIBLE
      et_description.visibility = View.VISIBLE
      et_quantity.visibility = View.VISIBLE
      et_price.visibility = View.VISIBLE
      b_saveChanges.visibility = View.VISIBLE

      b_saveChanges.setOnClickListener {
        if (et_description.text.toString() != ""){
          val newDescription = et_description.text.toString()
          updateProductForSale("description", newDescription, prodID)
          Log.d(TAG, "the new description is $newDescription")
        }
        if(et_price.text.toString() != ""){
          val newPrice = et_price.text.toString()
          updateProductForSale("price", newPrice, prodID)
        }
        if(et_quantity.text.toString() != ""){
          val newNumberAvailable = et_quantity.text.toString()
          updateProductForSale("quantity", newNumberAvailable, prodID)
        }
        val newIntent = Intent(this, SellersHomePage::class.java)
        startActivity(newIntent)
      }
    }

    b_forSale.setOnClickListener{
      Toast.makeText(this, "Product for sale again!", Toast.LENGTH_LONG ).show()
      Log.d(TAG, "the id number is ${product.idNumber}")

      b_soldOut.visibility = View.VISIBLE
      b_forSale.visibility = View.INVISIBLE

      val newStatus = "Added"
      updateProductForSale("status", newStatus, prodID)
      val newIntent = Intent(this, SellersHomePage::class.java)
      startActivity(newIntent)
    }

    b_soldOut.setOnClickListener{
      Toast.makeText(this, "Product sold out", Toast.LENGTH_LONG ).show()
      Log.d(TAG, "the id number is ${product.idNumber}")

      val newStatus = "Sold"
      updateProductForSale("status", newStatus, prodID)
      val newIntent = Intent(this, SellersHomePage::class.java)
      startActivity(newIntent)
    }

    b_deleted.setOnClickListener{
      Toast.makeText(this, "Product deleted", Toast.LENGTH_LONG ).show()
      Log.d(TAG, "the id number is ${product.idNumber}")

      val newStatus = "Deleted"
      updateProductForSale("status", newStatus, prodID)
      val newIntent = Intent(this, SellersHomePage::class.java)
      startActivity(newIntent)
    }
  }

  private fun updateProductForSale(updateField: String, newData : String, prodID : String?) {

    val map: MutableMap<String, Any> = mutableMapOf<String, Any>()
    map[updateField] = newData

    if (prodID != null) {
      db.collection("products")
        .document(prodID)
        .update(map)
    } else{
      Toast.makeText(this, "Product not found", Toast.LENGTH_LONG).show()
    }
  }
  }



