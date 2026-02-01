package com.consisteso.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.consisteso.app.worker.WorkerScheduler

/**
 * Boot receiver to restart workers after device reboot
 * 
 * Philosophy: Inescapable. Even after reboot.
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                // Reschedule all workers
                WorkerScheduler.scheduleAllWorkers(it)
            }
        }
    }
}
