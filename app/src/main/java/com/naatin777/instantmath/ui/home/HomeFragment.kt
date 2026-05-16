package com.naatin777.instantmath.ui.home

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.naatin777.instantmath.R
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

        setupTabs()
        setupMathSymbols()
        setupKeyboardListener()
    }

    private fun setupTabs() {
        binding.viewPager.adapter = HomePagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_favorite)
                1 -> getString(R.string.tab_history)
                else -> ""
            }
        }.attach()
    }


    private fun setupKeyboardListener() {
        val symbolBar = binding.bottomAppBar
        val bottomContainer = binding.bottomContainer
        val editText = binding.editMessage
        var isAnimating = false
        var isImeTargetVisible = false
        var barHeight = 0f

        // 【修正1】XMLの alpha="0" を上書きして、常に不透明（見える状態）にしておく
        symbolBar.alpha = 1f

        editText.setOnFocusChangeListener { _, _ ->
            ViewCompat.requestApplyInsets(binding.root)
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            binding.root,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    isAnimating = true

                    // 高さの事前計算をより正確にするための処理
                    if (symbolBar.height > 0) {
                        barHeight = symbolBar.height.toFloat()
                    } else {
                        // GONE状態で高さが0の場合、一時的に見えない状態(INVISIBLE)にして高さを測る
                        symbolBar.visibility = View.INVISIBLE
                        symbolBar.measure(
                            View.MeasureSpec.makeMeasureSpec(bottomContainer.width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        barHeight = symbolBar.measuredHeight.toFloat()
                        symbolBar.visibility = View.GONE
                    }
                }

                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    symbolBar.visibility = View.VISIBLE
                    return super.onStart(animation, bounds)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    val diff = maxOf(imeBottom, sysBottom)

                    val imeAnimation = runningAnimations
                        .find { it.typeMask and WindowInsetsCompat.Type.ime() != 0 }
                        ?: return insets
                    val fraction = imeAnimation.interpolatedFraction

                    val slideProgress = if (isImeTargetVisible) fraction else (1f - fraction)

                    // スライド用のアニメーション位置計算
                    bottomContainer.translationY = -diff.toFloat() + barHeight * (1f - slideProgress)

                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    isAnimating = false
                    val hasHardwareKeyboard = resources.configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS
                    val isHardwareKeyboardActive = hasHardwareKeyboard && editText.hasFocus()
                    val shouldShow = isImeTargetVisible || isHardwareKeyboardActive

                    if (shouldShow) {
                        symbolBar.visibility = View.VISIBLE
                    } else {
                        symbolBar.visibility = View.GONE
                    }

                    // 【修正2】めり込み防止：アニメーション終了後にY軸のズレを完全にリセットする
                    val rootInsets = ViewCompat.getRootWindowInsets(binding.root)
                    if (rootInsets != null) {
                        val imeBottom = rootInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        val sysBottom = rootInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                        bottomContainer.translationY = -maxOf(imeBottom, sysBottom).toFloat()
                    }
                }
            }
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            isImeTargetVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val hasHardwareKeyboard = resources.configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS
            val isHardwareKeyboardActive = hasHardwareKeyboard && editText.hasFocus()
            val shouldShow = isImeTargetVisible || isHardwareKeyboardActive
            val diff = maxOf(imeBottom, sysBottom)

            if (!isAnimating) {
                if (shouldShow) {
                    symbolBar.visibility = View.VISIBLE
                } else {
                    symbolBar.visibility = View.GONE
                }
                // 通常時の位置をしっかり固定（めり込み防止）
                bottomContainer.translationY = -diff.toFloat()
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
