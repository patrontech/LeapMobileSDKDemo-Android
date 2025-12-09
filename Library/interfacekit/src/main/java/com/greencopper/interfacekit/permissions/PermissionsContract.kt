package com.greencopper.interfacekit.permissions

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

internal class PermissionsContract :
    ActivityResultContract<PermissionsContract.PermissionsContractData, Set<String>>() {

    private var data: PermissionsContractData? = null

    override fun createIntent(context: Context, input: PermissionsContractData): Intent {
        data = input
        return input.intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Set<String> {
        return data?.permissions ?: emptySet()
    }

    data class PermissionsContractData(val intent: Intent, val permissions: Set<String>)
}