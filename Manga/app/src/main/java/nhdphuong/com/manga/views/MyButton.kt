package nhdphuong.com.manga.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.widget.Button
import nhdphuong.com.manga.R

/*
 * Created by nhdphuong on 4/22/18.
 */
class MyButton(context: Context, attrs: AttributeSet?, defStyle: Int) : Button(context, attrs, defStyle) {
    companion object {
        private val TAG = MyButton::class.java.simpleName
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private fun setCustomFont(context: Context, attrs: AttributeSet?) {
        val attribute = context.obtainStyledAttributes(attrs, R.styleable.MyTextView)
        val customFont = attribute.getString(R.styleable.MyTextView_myFont)
        setCustomFont(context, customFont)
        attribute.recycle()
    }

    private fun setCustomFont(ctx: Context, asset: String?): Boolean {
        val tf: Typeface?
        try {
            tf = Typeface.createFromAsset(ctx.assets, asset)
        } catch (e: Exception) {
            Log.e(TAG, "Could not get typeface: " + e.message)
            return false
        }

        typeface = tf
        return true
    }
}