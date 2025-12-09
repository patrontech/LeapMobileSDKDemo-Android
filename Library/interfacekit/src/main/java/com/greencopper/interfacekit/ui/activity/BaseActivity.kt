package com.greencopper.interfacekit.ui.activity

import androidx.appcompat.app.AppCompatActivity

public abstract class BaseActivity(layout: Int = 0) : AppCompatActivity(layout) {

    override fun onSupportNavigateUp(): Boolean {
        // Back arrow in toolbar acts as back button.
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
