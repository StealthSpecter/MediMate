package com.example.healthcareaccess.method

import android.content.Context
import android.widget.Toast

object AndroidUtil {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

