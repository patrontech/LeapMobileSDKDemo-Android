package com.greencopper.thuzi.fanscan

import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.route.RouteController

public class ConcreteDecodeCallback(
    private val linkResolver: LinkResolver,
    private val routeController: RouteController
) : KibaDecodeCallback {
    private lateinit var action: ((Result) -> Unit)

    override fun setAction(newAction: (Result) -> Unit) {
        action = newAction
    }

    override fun onDecoded(result: Result) {
        val qrCodeUri = Uri.parse(result.text)
        qrCodeUri.getQueryParameter("deeplink")?.let {
            val route = linkResolver.route(it)
            if (route == null) {
                action(result)
            } else {
                routeController.redirect(route, null)
            }
        } ?: qrCodeUri.getQueryParameter("moduleID")?.let {
            if (it.isEmpty()) {
                action(result)
            } else {
                action(it.toResult())
            }
        } ?: run {
            action(result)
        }
    }

    private fun String.toResult() =
        Result(this, this.toByteArray(), emptyArray(), BarcodeFormat.QR_CODE)
}
