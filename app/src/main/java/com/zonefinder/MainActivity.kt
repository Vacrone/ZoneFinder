package com.zonefinder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val captureRequestCode = 1001
    private val overlayRequestCode = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val heading = TextView(this).apply {
            text = "ZoneFinder"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            text = "Supply and demand zones • Fair value gaps"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }

        val overlayButton = Button(this).apply {
            text = "1. Enable floating overlay"
            setOnClickListener { requestOverlayPermission() }
        }

        val startButton = Button(this).apply {
            text = "2. Start floating panel"
            setOnClickListener { startFloatingPanel() }
        }

        val captureButton = Button(this).apply {
            text = "3. Test screen capture"
            setOnClickListener { requestScreenCapture() }
        }

        val status = TextView(this).apply {
            text = "Enable overlay permission, then start the floating panel."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        layout.addView(heading)
        layout.addView(description)

        layout.addView(
            overlayButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            startButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            captureButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(status)
        setContentView(layout)
    }

    private fun requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                "Overlay permission is enabled",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, overlayRequestCode)
    }

    private fun startFloatingPanel() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                "Enable floating overlay permission first",
                Toast.LENGTH_LONG
            ).show()
            requestOverlayPermission()
            return
        }

        startService(Intent(this, OverlayService::class.java))

        Toast.makeText(
            this,
            "Starting ZoneFinder panel",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE)
            as MediaProjectionManager

        startActivityForResult(
            manager.createScreenCaptureIntent(),
            captureRequestCode
        )
    }

    @Deprecated("Deprecated by Android; retained for Android 9 compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            overlayRequestCode -> {
                val enabled = Settings.canDrawOverlays(this)
                Toast.makeText(
                    this,
                    if (enabled) "Overlay permission enabled"
                    else "Overlay permission not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }

            captureRequestCode -> {
                Toast.makeText(
                    this,
                    if (resultCode == RESULT_OK && data != null)
                        "Screen capture permission granted for this session"
                    else
                        "Screen capture cancelled",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
