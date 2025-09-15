package com.example.blackground

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    
    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeContainer: LinearLayout
    private lateinit var rootLayout: RelativeLayout
    private lateinit var settingsButton: ImageButton
    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable
    private lateinit var preferences: SharedPreferences
    private var originalBrightness: Float = -1f
    
    // Settings
    private var timePosition = "left" // "left" or "right"
    private var backgroundColor = Color.BLACK
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable fullscreen and hide system UI
        setupFullscreen()
        
        setContentView(R.layout.activity_main)
        
        // Initialize SharedPreferences
        preferences = getSharedPreferences("blackground_settings", MODE_PRIVATE)
        
        // Initialize views
        timeTextView = findViewById(R.id.timeTextView)
        dateTextView = findViewById(R.id.dateTextView)
        timeContainer = findViewById(R.id.timeContainer)
        rootLayout = findViewById(R.id.rootLayout)
        settingsButton = findViewById(R.id.settingsButton)
        
        // Load saved settings
        loadSettings()
        
        // Apply settings
        applySettings()
        
        // Setup settings button
        settingsButton.setOnClickListener {
            showSettingsDialog()
        }
        
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
    
    private fun loadSettings() {
        timePosition = preferences.getString("time_position", "left") ?: "left"
        backgroundColor = preferences.getInt("background_color", Color.BLACK)
    }
    
    private fun saveSettings() {
        preferences.edit().apply {
            putString("time_position", timePosition)
            putInt("background_color", backgroundColor)
            apply()
        }
    }
    
    private fun applySettings() {
        // Apply background color
        rootLayout.setBackgroundColor(backgroundColor)
        
        // Apply time position
        val layoutParams = timeContainer.layoutParams as RelativeLayout.LayoutParams
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START)
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
        layoutParams.setMargins(0, 0, 0, 0)
        
        if (timePosition == "right") {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParams.marginEnd = 32.dpToPx()
            timeContainer.gravity = android.view.Gravity.END
        } else {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            layoutParams.marginStart = 32.dpToPx()
            timeContainer.gravity = android.view.Gravity.START
        }
        
        timeContainer.layoutParams = layoutParams
        
        // Apply smart text color based on background
        val textColor = getContrastColor(backgroundColor)
        timeTextView.setTextColor(textColor)
        dateTextView.setTextColor(textColor)
        settingsButton.setColorFilter(textColor)
    }
    
    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.settings_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // Setup position radio buttons
        val positionGroup = dialogView.findViewById<RadioGroup>(R.id.positionRadioGroup)
        val leftRadio = dialogView.findViewById<RadioButton>(R.id.leftRadio)
        val rightRadio = dialogView.findViewById<RadioButton>(R.id.rightRadio)
        
        if (timePosition == "left") leftRadio.isChecked = true else rightRadio.isChecked = true
        
        // Setup color selection
        val colorViews = listOf(
            dialogView.findViewById<View>(R.id.colorBlack) to Color.BLACK,
            dialogView.findViewById<View>(R.id.colorDarkGray) to Color.parseColor("#333333"),
            dialogView.findViewById<View>(R.id.colorDarkBlue) to Color.parseColor("#001122"),
            dialogView.findViewById<View>(R.id.colorDarkRed) to Color.parseColor("#220011")
        )
        
        var selectedColor = backgroundColor
        
        colorViews.forEach { (view, color) ->
            view.setOnClickListener {
                selectedColor = color
                // Add selection indicator
                colorViews.forEach { (v, _) -> 
                    v.alpha = if (v == view) 1.0f else 0.7f
                }
            }
            // Set initial selection
            view.alpha = if (color == backgroundColor) 1.0f else 0.7f
        }
        
        // Setup buttons
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.applyButton).setOnClickListener {
            timePosition = if (leftRadio.isChecked) "left" else "right"
            backgroundColor = selectedColor
            saveSettings()
            applySettings()
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun getContrastColor(backgroundColor: Int): Int {
        // Calculate luminance of background color
        val red = Color.red(backgroundColor)
        val green = Color.green(backgroundColor)
        val blue = Color.blue(backgroundColor)
        
        // Use relative luminance formula
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        
        // Return white for dark backgrounds, black for light backgrounds
        return if (luminance < 0.5) Color.WHITE else Color.BLACK
    }
    
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}