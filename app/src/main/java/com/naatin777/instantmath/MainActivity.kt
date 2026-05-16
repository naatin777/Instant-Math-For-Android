package com.naatin777.instantmath

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
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
        enableEdgeToEdge()
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            settingsRepository.isDynamicColorEnabledFlow.drop(1).collect {
                recreate()
            }
        }
    }
}
