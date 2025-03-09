package com.ascrib.nutrifit.util

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import androidx.databinding.BindingConversion


@BindingAdapter("imageLoad")
fun loadImage(imageView: ImageView, image: Int) {
    image.let {
        Glide.with(imageView.context)
            .load(image)
            .into(imageView)
    }
}

@BindingAdapter("textPrice")
fun loadPrice(textView: TextView, price: Float) {
    textView.text = "$".plus(price.toInt().toString())
}

@BindingAdapter("backColor")
fun setCardColor(view: CardView, color: String) {
    view.apply {
        //setCardBackgroundColor(ContextCompat.getColor(view.context, color))
        setCardBackgroundColor(Color.parseColor(color))
    }
}

@BindingAdapter("viewVisible")
fun visibility(view: CardView, isVisible: Boolean) {
    view.apply {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
