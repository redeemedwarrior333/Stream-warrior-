package com.streamwarrior.app

import android.app.Application
import android.os.Looper
import android.widget.Toast
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Catches any crash, saves the stack trace to crash.log and shows a toast.
 * MainActivity displays the saved report so it can be shared.
 */
class WarriorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                File(filesDir, "crash.log").writeText(
                    "Thread: " + thread.name + "\n" + sw.toString()
                )
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Looper.prepare()
                }
                Toast.makeText(this, "Stream Warrior error: " + throwable.javaClass.simpleName, Toast.LENGTH_LONG).show()
                Looper.loop()
            } catch (t: Throwable) {
                // last resort — let the system handle it
                Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
            }
        }
    }
}
