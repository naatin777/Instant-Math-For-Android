package com.naatin777.instantmath.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.naatin777.instantmath.R
import com.naatin777.instantmath.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.aaa.updateAppearance(0, 2)
        binding.bbb.updateAppearance(1, 2)

        setupThemeSelection()
        setupDynamicColorToggle()

    }

    private fun setupDynamicColorToggle() {
        binding.dynamicColorSwitch.let { sw ->
            viewModel.isDynamicColorEnabled.observe(viewLifecycleOwner) { enabled ->
                if (sw.isChecked != enabled) {
                    sw.isChecked = enabled
                }
            }
            sw.setOnCheckedChangeListener { _, isChecked ->
                if (viewModel.isDynamicColorEnabled.value != isChecked) {
                    viewModel.setDynamicColorEnabled(isChecked)
                }
            }
        }
    }

    private fun setupThemeSelection() {
        val buttons = listOf(binding.btnLight, binding.btnSystem, binding.btnDark)

        viewModel.themeMode.observe(viewLifecycleOwner) { mode ->
            val selectedId = when (mode) {
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.btn_light
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.btn_dark
                else -> R.id.btn_system
            }

            buttons.forEach { btn ->
                if (btn.isChecked != (btn.id == selectedId)) {
                    btn.isChecked = (btn.id == selectedId)
                }
            }
        }

        buttons.forEach { btn ->
            btn.setOnClickListener {
                val mode = when (btn.id) {
                    R.id.btn_light -> AppCompatDelegate.MODE_NIGHT_NO
                    R.id.btn_dark -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                if (viewModel.themeMode.value != mode) {
                    viewModel.setThemeMode(mode)
                } else {
                    btn.isChecked = true
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
