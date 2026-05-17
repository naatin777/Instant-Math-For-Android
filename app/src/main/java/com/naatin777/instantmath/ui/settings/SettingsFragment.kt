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
import com.naatin777.instantmath.data.CopyFormat
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

        binding.aaa.updateAppearance(0, 3)
        binding.copyFormatItem.updateAppearance(1, 3)
        binding.bbb.updateAppearance(2, 3)

        setupThemeSelection()
        setupCopyFormatSelection()
        setupDynamicColorToggle()
    }

    private fun setupDynamicColorToggle() {
        binding.dynamicColorSwitch.let { sw ->
            viewModel.isDynamicColorEnabled.observe(viewLifecycleOwner) { enabled ->
                if (sw.isChecked != enabled) {
                    sw.isChecked = enabled
                    sw.jumpDrawablesToCurrentState()
                }
            }
            sw.setOnCheckedChangeListener { _, isChecked ->
                if (viewModel.isDynamicColorEnabled.value != isChecked) {
                    viewModel.setDynamicColorEnabled(isChecked)
                }
            }
        }
    }

    private fun setupCopyFormatSelection() {
        val buttons = listOf(binding.btnCopyText, binding.btnCopyImage)

        viewModel.copyFormat.observe(viewLifecycleOwner) { format ->
            val selectedId = when (format) {
                CopyFormat.TEXT -> R.id.btn_copy_text
                CopyFormat.IMAGE -> R.id.btn_copy_image
            }
            buttons.forEach { btn ->
                if (btn.isChecked != (btn.id == selectedId)) {
                    btn.isChecked = btn.id == selectedId
                }
            }
        }

        buttons.forEach { btn ->
            btn.setOnClickListener {
                val format = when (btn.id) {
                    R.id.btn_copy_image -> CopyFormat.IMAGE
                    else -> CopyFormat.TEXT
                }
                if (viewModel.copyFormat.value != format) {
                    viewModel.setCopyFormat(format)
                } else {
                    btn.isChecked = true
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
