package com.tyshi00.ledger.util

import android.util.Log
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.tyshi00.ledger.data.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

object LedgerLockManager {
    private const val TAG = "LedgerPIN"

    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    @Volatile private var isInitialized = false
    @Volatile private var pinEnabledInSession = false
    @Volatile private var repo: LedgerRepository? = null

    private val overlayViews = mutableListOf<View>()

    fun registerOverlay(view: View) {
        overlayViews.add(view)
    }

    // Direct View manipulation — no post(), no Compose, no delay.
    // ProcessLifecycleOwner callbacks run on the main thread.
    private fun showOverlays() {
        overlayViews.forEach { it.visibility = View.VISIBLE }
    }

    private fun hideOverlays() {
        overlayViews.forEach { it.visibility = View.GONE }
    }

    fun init(repository: LedgerRepository) {
        repo = repository
        if (isInitialized) return
        isInitialized = true

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                if (pinEnabledInSession) {
                    _locked.value = true
                    showOverlays()
                    Log.d(TAG, "onPause -> LOCKED + overlays VISIBLE")
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (pinEnabledInSession) {
                    _locked.value = true
                    showOverlays()
                    Log.d(TAG, "onStop -> LOCKED + overlays VISIBLE")
                }
            }
        })

        runBlocking {
            if (repository.isPinEnabled()) {
                pinEnabledInSession = true
                Log.d(TAG, "Init: PIN enabled, staying locked")
            } else {
                _locked.value = false
                Log.d(TAG, "Init: No PIN, unlocking")
            }
        }
    }

    fun verifyPin(pin: String): Boolean {
        val r = repo ?: return false
        return runBlocking {
            val valid = r.verifyPin(pin)
            if (valid) {
                _locked.value = false
                hideOverlays()
                Log.d(TAG, "PIN correct -> UNLOCKED + overlays GONE")
            }
            valid
        }
    }

    fun onPinSet() {
        pinEnabledInSession = true
    }

    fun onPinRemoved() {
        pinEnabledInSession = false
        _locked.value = false
        hideOverlays()
    }

    fun refreshPinState() {
        val r = repo ?: return
        runBlocking {
            val enabled = r.isPinEnabled()
            pinEnabledInSession = enabled
            if (!enabled) {
                _locked.value = false
                hideOverlays()
            }
        }
    }
}
