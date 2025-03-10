package com.ascrib.nutrifit.util

import android.content.Context
import android.graphics.Typeface
import android.util.Log

class TypefaceUtil {
    companion object{

        fun overrideFont(context: Context, defaultFontToOverride: String, customFontFileNameInAssets:String){
            try {
                val customTypeface = Typeface.createFromAsset(context.assets,customFontFileNameInAssets)
                val defaultTypefaceField = Typeface::class.java.getDeclaredField(defaultFontToOverride)
                defaultTypefaceField.isAccessible = true
                defaultTypefaceField.set(null,customTypeface)
            }catch (e:Exception){
                Log.d(
                    "font matters : ",
                    "Can not set custom font $customFontFileNameInAssets instead of $defaultFontToOverride"
                )
            }
        }
    }

}