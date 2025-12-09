package com.greencopper.testmocks.interfacekit

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.greencopper.testmocks.R

public class DummyActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)
    }
}
