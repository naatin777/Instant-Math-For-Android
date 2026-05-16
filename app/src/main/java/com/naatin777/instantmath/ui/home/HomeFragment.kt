package com.naatin777.instantmath.ui.home

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.naatin777.instantmath.databinding.FragmentHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSettings.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().navigate(action)
        }

        binding.mathView.apply {
            latex = """f(x) = \int_{-\infty}^{\infty} \hat{f}(\xi) e^{2 \pi i \xi x} d\xi"""
            fontSize = 24f
            displayMode = true
        }

        setupMathSymbols()
        setupKeyboardListener()
    }


    private fun setupKeyboardListener() {
        val bottomAppBar = binding.bottomAppBar
        val editText = binding.editMessage

        // アニメーション状態と、OSが意図している最終的なキーボードの表示状態
        var isAnimating = false
        var isImeTargetVisible = false

        editText.setOnFocusChangeListener { _, _ ->
            ViewCompat.requestApplyInsets(binding.root)
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            binding.root,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    isAnimating = true
                }

                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    bottomAppBar.visibility = View.VISIBLE
                    return super.onStart(animation, bounds)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

                    val diff = maxOf(imeBottom, sysBottom)
                    bottomAppBar.translationY = -diff.toFloat()

                    val imeAnimation = runningAnimations
                        .find { it.typeMask and WindowInsetsCompat.Type.ime() != 0 }
                        ?: return insets

                    val fraction = imeAnimation.interpolatedFraction
                    // 【修正】OSが最終的に開くつもりなら濃く、閉じるつもりなら薄くする！
                    bottomAppBar.alpha = if (isImeTargetVisible) fraction else (1f - fraction)

                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    isAnimating = false

                    // アニメーションが完了した後、強制的に正しい最終状態に合わせる（ズレ防止）
                    val hasHardwareKeyboard = resources.configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS
                    val isHardwareKeyboardActive = hasHardwareKeyboard && editText.hasFocus()
                    val shouldShow = isImeTargetVisible || isHardwareKeyboardActive

                    if (shouldShow) {
                        bottomAppBar.visibility = View.VISIBLE
                        bottomAppBar.alpha = 1f
                    } else {
                        bottomAppBar.visibility = View.GONE
                        bottomAppBar.alpha = 0f
                    }
                }
            }
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            // 【重要】OSから「これからキーボードどうなるか」の真実を受け取る
            isImeTargetVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            val hasHardwareKeyboard = resources.configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS
            val isHardwareKeyboardActive = hasHardwareKeyboard && editText.hasFocus()

            val shouldShow = isImeTargetVisible || isHardwareKeyboardActive

            val diff = maxOf(imeBottom, sysBottom)
            bottomAppBar.translationY = -diff.toFloat()

            // アニメーションしていない時だけ、即座に見た目を切り替える
            if (!isAnimating) {
                if (shouldShow) {
                    bottomAppBar.alpha = 1f
                    bottomAppBar.visibility = View.VISIBLE
                } else {
                    bottomAppBar.alpha = 0f
                    bottomAppBar.visibility = View.GONE
                }
            }
            insets
        }

        binding.root.post {
            ViewCompat.requestApplyInsets(binding.root)
        }
    }

    private fun setupMathSymbols() {
        val symbols = listOf("\\", "{", "}", "^", "_", "[", "]", "`", "~", "&", "%")
        val adapter = MathSymbolAdapter(symbols) { symbol ->
            val editText = binding.editMessage
            val start = editText.selectionStart
            val end = editText.selectionEnd
            editText.text.replace(start, end, symbol)
            editText.setSelection(start + symbol.length)
        }
        binding.rvMathSymbols.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
