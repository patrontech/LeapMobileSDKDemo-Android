package com.greencopper.thuzi.fanscan

import com.budiyev.android.codescanner.DecodeCallback
import com.google.zxing.Result

public interface KibaDecodeCallback: DecodeCallback {
    public fun setAction(newAction: (Result) -> Unit)
}
