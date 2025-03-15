package com.ascrib.nutrifit.ui.profile

import android.content.Context
import android.widget.TextView
import com.ascrib.nutrifit.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class MakerViewLayout (context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    // This method is called when a value is selected/highlighted
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            // Format the value to display with one decimal place
            tvContent.text = String.format("%.1f%%", e.y)
        }
        super.refreshContent(e, highlight)
    }

    // Offset to ensure the tooltip is drawn at the correct position
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 10)
    }
}