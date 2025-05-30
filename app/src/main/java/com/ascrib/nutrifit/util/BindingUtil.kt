package com.ascrib.nutrifit.util

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import androidx.databinding.BindingConversion
import com.ascrib.nutrifit.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
        // Reemplaza 127.0.0.1 con tu IP local (ej: 192.168.1.X)
        val correctedUrl = url.replace("http://127.0.0.1", "http://192.168.50.221")

        Glide.with(view.context)
            .load(correctedUrl)
            .placeholder(R.drawable.userdummy)
            .error(R.drawable.usererrror)
            .into(view)
    } else {
        view.setImageResource(R.drawable.userdummy)
    }
}

@BindingAdapter("horarioFormateado")
fun formatHorario(textView: TextView, horario: String?) {
    horario?.let {
        val formatted = it.replaceFirst(":", ":\n").replace(";", "\n")
        textView.text = formatted
    }
}

@BindingAdapter("fechaNacimiento")
fun setFechaNacimiento(textView: TextView, date: String?) {
    date?.let {
        textView.text = it // o formateas si viene en formato ISO y necesitas mostrarlo distinto
    }
}

@BindingAdapter("htmlText")
fun setHtmlText(textView: TextView, html: String?) {
    html?.let {
        textView.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}

@BindingAdapter("imageResource")
fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}

@BindingAdapter("formattedDate")
fun TextView.setFormattedDate(dateString: String?) {
    text = if (dateString.isNullOrEmpty()) {
        "Fecha no disponible"
    } else {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }
}