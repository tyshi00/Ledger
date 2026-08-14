package com.tyshi00.ledger

import com.thelightphone.sdk.EntryPoint
import com.thelightphone.sdk.LightEntryPoint
import com.thelightphone.sdk.shared.LightServerData
import kotlinx.coroutines.flow.StateFlow

@EntryPoint
object LedgerEntryPoint : LightEntryPoint {
    override suspend fun onToolCreate(serverData: StateFlow<LightServerData?>) {
        // Future: register periodic backup job here
    }

    override suspend fun onPushNotification(data: ByteArray) {
        // Not used
    }
}
