package com.greencopper.thuzi.survey

import android.webkit.CookieManager
import android.webkit.ValueCallback
import androidx.lifecycle.ViewModel
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.services.attendee.AttendeeService

internal class SurveyViewModel(
    localStorage: LocalStorage,
    private val attendeeService: AttendeeService
) : ViewModel() {

    private val jwtCookie = "mobile=${localStorage.project.thuzi.jwt.value}; " +
            "path=/; " +
            "Secure;"

    fun injectCookie(url: String, callback: ValueCallback<Boolean>) {
        CookieManager.getInstance().setCookie(url, jwtCookie, callback)
    }

    fun updateUserProfile() {
        attendeeService.fetchAndDispatch()
    }
}