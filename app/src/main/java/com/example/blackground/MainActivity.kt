package com.example.blackground

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    
    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable
    private var originalBrightness: Float = -1f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable fullscreen and hide system UI
        setupFullscreen()
        
        setContentView(R.layout.activity_main)
        
        // Initialize views
        timeTextView = findViewById(R.id.timeTextView)
        dateTextView = findViewById(R.id.dateTextView)
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Reduce brightness
        reduceBrightness()
        
        // Initialize handler for periodic updates
        handler = Handler(Looper.getMainLooper())
        
        // Create runnable for updating time and date
        updateRunnable = object : Runnable {
            override fun run() {
                updateTimeAndDate()
                // Schedule next update at the start of the next minute
                val now = Calendar.getInstance()
                val nextMinute = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, 1)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val delayMs = nextMinute.timeInMillis - now.timeInMillis
                handler.postDelayed(this, delayMs)
            }
        }
        
        // Initial update
        updateTimeAndDate()
        
        // Start the periodic updates
        startPeriodicUpdates()
    }
    
    private fun setupFullscreen() {
        // Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        // Hide system UI (navigation and status bars)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        // Set fullscreen flags
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
    
    private fun reduceBrightness() {
        try {
            // Store original brightness
            originalBrightness = Settings.System.getFloat(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255f
            
            // Set reduced brightness for this window only
            val layoutParams = window.attributes
            layoutParams.screenBrightness = 0.1f // 10% brightness
            window.attributes = layoutParams
        } catch (e: Settings.SettingNotFoundException) {
            // If we can't get system brightness, just set window brightness low
            val layoutParams = window.attributes
            layoutParams.screenBrightness = 0.1f
            window.attributes = layoutParams
        }
    }
    
    private fun restoreBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
    
    private fun updateTimeAndDate() {
        val now = Calendar.getInstance()
        
        // Format time as HH:MM
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(now.time)
        
        // Format date as EEE, MMM d
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val dateString = dateFormat.format(now.time)
        
        // Update the TextViews
        timeTextView.text = timeString
        dateTextView.text = dateString
    }
    
    private fun startPeriodicUpdates() {
        // Calculate delay until next minute
        val now = Calendar.getInstance()
        val nextMinute = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val delayMs = nextMinute.timeInMillis - now.timeInMillis
        
        // Start the periodic updates
        handler.postDelayed(updateRunnable, delayMs)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending callbacks
        handler.removeCallbacks(updateRunnable)
        // Restore brightness
        restoreBrightness()
    }
    
    override fun onBackPressed() {
        // Clean exit - restore brightness and finish
        restoreBrightness()
        super.onBackPressed()
    }
}