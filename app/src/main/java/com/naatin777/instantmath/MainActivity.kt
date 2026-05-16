package com.naatin777.instantmath

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.naatin777.instantmath.data.SettingsRepository
import com.naatin777.instantmath.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDynamicColorEnabled = runBlocking {
            settingsRepository.isDynamicColorEnabledFlow.first()
        }
        if (isDynamicColorEnabled) {
            DynamicColors.applyToActivityIfAvailable(this@MainActivity)
        } else {
            val options = DynamicColorsOptions.Builder()
                .setContentBasedSource(ContextCompat.getColor(this, R.color.main_color))
                .build()
            DynamicColors.applyToActivityIfAvailable(this@MainActivity, options)
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyNavigationBarColor()

        lifecycleScope.launch {
            settingsRepository.isDynamicColorEnabledFlow.drop(1).collect {
                recreate()
            }
        }
    }

    private fun applyNavigationBarColor() {
        val navigationBarColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorSurfaceContainer,
            Color.BLACK,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.navigationBarColor = navigationBarColor
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = MaterialColors.isColorLight(navigationBarColor)
        }
    }
}
