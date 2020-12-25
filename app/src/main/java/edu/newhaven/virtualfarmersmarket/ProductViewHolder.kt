package edu.newhaven.virtualfarmersmarket

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    //var productImagePL: ImageView = view.findViewById(R.id.iv_productListingVhPhoto)
    var name: TextView = view.findViewById(R.id.tv_prodName)
    var price: TextView = view.findViewById(R.id.tv_prodPrice)
    var status: TextView = view.findViewById(R.id.tv_statusSold)
    var quantity: TextView = view.findViewById((R.id.tv_prodQuantity))
    var productPicture : ImageView = view.findViewById((R.id.iv_prodPicture))

}
