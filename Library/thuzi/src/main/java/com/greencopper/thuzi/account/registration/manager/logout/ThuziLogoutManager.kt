package com.greencopper.thuzi.account.registration.manager.logout

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.greencopper.interfacekit.rootview.RootLayoutManager
import kotlinx.coroutines.launch
import kotlin.math.exp

/**
 * Logs out and resets the root layout if the JWT
 * expires before the user manually logs out. This
 * forces the conditions to run again.
 *
 * This type is temporary. This logic should really
 * be part of `ThuziRegistrationManager`, but that
 * type is overridden in the NFL custom, which
 * complicates the task. After discussion with Carl,
 * it was decided to do this, then create a plugin
 * architecture for `ThuziRegistrationManager` so
 * that we don't need to override the whole type in
 * the NFL custom, then get rid of this type and move
 * the logic into `ThuziRegistrationManager` where it
 * belongs. Tickets are already created.
 */
internal class ThuziLogoutManager(
   private val expirationChecker: JwtExpirationChecker
): DefaultLifecycleObserver {
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        owner.lifecycleScope.launch {
            expirationChecker.checkExpiration()
        }
    }
}
