package com.naatin777.instantmath.ui.home

import android.graphics.Rect
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import com.naatin777.instantmath.R
import com.naatin777.instantmath.data.CopyFormat
import com.naatin777.instantmath.databinding.FragmentHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()
    private var mathPreviewJob: Job? = null

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

        setupMathPreviewSync()
        setupTabs()
        setupMathSymbols()
        setupSplitButton()
        setupKeyboardListener()
    }

    private fun setupSplitButton() {
        val leadingButton = binding.btnCopyLeading
        val trailingButton = binding.expandMoreOrLessFilled

        listOf(leadingButton, trailingButton).forEach { button ->
            button.isFocusable = false
            button.isFocusableInTouchMode = false
        }

        viewModel.copyFormat.observe(viewLifecycleOwner, ::applyCopyFormatUi)

        leadingButton.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val format = viewModel.copyFormat.value ?: CopyFormat.TEXT
            performCopy(format)
            restoreEditTextIme()
        }

        trailingButton.setOnClickListener { button ->
            showCopyOptionsMenu(button)
        }
    }

    private fun applyCopyFormatUi(format: CopyFormat) {
        val button = binding.btnCopyLeading
        val iconRes = when (format) {
            CopyFormat.TEXT -> R.drawable.outline_text_fields_24
            CopyFormat.IMAGE -> R.drawable.outline_image_24
        }
        val description = when (format) {
            CopyFormat.TEXT -> getString(R.string.copy_formula_as_text)
            CopyFormat.IMAGE -> getString(R.string.copy_formula_as_image)
        }
        if (button.contentDescription == description) return
        button.setIconResource(iconRes)
        button.contentDescription = description
    }

    private fun restoreEditTextIme() {
        val editText = binding.editMessage
        editText.post {
            editText.requestFocus()
            WindowCompat.getInsetsController(requireActivity().window, editText)
                .show(WindowInsetsCompat.Type.ime())
        }
    }

    private fun performCopy(format: CopyFormat) {
        val latex = binding.editMessage.text?.toString().orEmpty()
        if (latex.isBlank()) return

        when (format) {
            CopyFormat.TEXT -> {
                FormulaClipboard.copyText(requireContext(), latex)
            }
            CopyFormat.IMAGE -> {
                binding.mathView.latex = latex
                binding.mathView.doOnLayout {
                    FormulaClipboard.copyImage(requireContext(), binding.mathView)
                }
            }
        }
    }

    private fun showCopyOptionsMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.split_button_copy_menu, popup.menu)
        popup.setForceShowIcon(true)
        popup.setOnMenuItemClickListener { item ->
            anchor.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            when (item.itemId) {
                R.id.copy_as_text -> {
                    viewModel.setCopyFormat(CopyFormat.TEXT)
                    true
                }
                R.id.copy_as_image -> {
                    viewModel.setCopyFormat(CopyFormat.IMAGE)
                    true
                }
                else -> false
            }
        }
        popup.setOnDismissListener {
            binding.expandMoreOrLessFilled.isChecked = false
            restoreEditTextIme()
        }
        popup.show()
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
        var isClosing = false
        var activeKeyboardIsResize = false
        var barHeight = 0f

        symbolBar.alpha = 1f

        fun layoutConsumedByIme(): Int {
            val visibleFrame = Rect()
            binding.root.getWindowVisibleDisplayFrame(visibleFrame)
            val rootLocation = IntArray(2)
            binding.root.getLocationOnScreen(rootLocation)
            val rootBottomOnScreen = rootLocation[1] + binding.root.height
            return (rootBottomOnScreen - visibleFrame.bottom).coerceAtLeast(0)
        }

        fun isResizeImeKeyboard(insets: WindowInsetsCompat): Boolean {
            if (!insets.isVisible(WindowInsetsCompat.Type.ime())) return false

            val consumed = layoutConsumedByIme()
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val threshold = (48 * resources.displayMetrics.density).toInt()

            return when {
                imeBottom == 0 -> consumed >= threshold
                else -> consumed > imeBottom / 3
            }
        }

        fun updateListImePadding(imeBottom: Int) {
            childFragmentManager.fragments
                .filterIsInstance<HomeListFragment>()
                .forEach { it.setImePadding(imeBottom) }
        }

        fun applyKeyboardState(insets: WindowInsetsCompat, slideProgress: Float = 1f) {
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val slideOffset = if (imeBottom > 0 || isAnimating) {
                barHeight * (1f - slideProgress)
            } else {
                0f
            }
            val keyboardShownFraction = when {
                isAnimating -> slideProgress
                imeBottom > 0 -> 1f
                else -> 0f
            }
            val resizeKeyboard = if (isAnimating || insets.isVisible(WindowInsetsCompat.Type.ime())) {
                activeKeyboardIsResize
            } else {
                false
            }
            val navBarMarginScale = if (resizeKeyboard) 1f - keyboardShownFraction else 1f

            bottomContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = (sysBottom * navBarMarginScale).toInt()
            }
            bottomContainer.translationY = -imeBottom.toFloat() + slideOffset
            updateListImePadding(imeBottom)
        }

        fun updateSymbolBarVisibility(insets: WindowInsetsCompat) {
            val hasHardwareKeyboard =
                resources.configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS
            val isHardwareKeyboardActive = hasHardwareKeyboard && editText.hasFocus()
            val shouldShow = insets.isVisible(WindowInsetsCompat.Type.ime()) || isHardwareKeyboardActive
            symbolBar.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }

        editText.setOnFocusChangeListener { _, _ ->
            ViewCompat.requestApplyInsets(binding.root)
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            binding.root,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    isAnimating = true
                    val currentInsets = ViewCompat.getRootWindowInsets(binding.root)
                    isClosing = currentInsets?.isVisible(WindowInsetsCompat.Type.ime()) == true
                    if (!isClosing) {
                        activeKeyboardIsResize = false
                    }
                    if (symbolBar.height > 0) {
                        barHeight = symbolBar.height.toFloat()
                    } else {
                        symbolBar.visibility = View.INVISIBLE
                        symbolBar.measure(
                            View.MeasureSpec.makeMeasureSpec(bottomContainer.width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        )
                        barHeight = symbolBar.measuredHeight.toFloat()
                        symbolBar.visibility = View.GONE
                    }
                }

                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat,
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    symbolBar.visibility = View.VISIBLE
                    return super.onStart(animation, bounds)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>,
                ): WindowInsetsCompat {
                    val imeAnimation = runningAnimations
                        .find { it.typeMask and WindowInsetsCompat.Type.ime() != 0 }
                        ?: return insets
                    val fraction = imeAnimation.interpolatedFraction
                    val slideProgress = if (!isClosing) fraction else (1f - fraction)
                    if (!isClosing) {
                        activeKeyboardIsResize = isResizeImeKeyboard(insets)
                    }
                    applyKeyboardState(insets, slideProgress)
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    isAnimating = false
                    val rootInsets = ViewCompat.getRootWindowInsets(binding.root) ?: return
                    activeKeyboardIsResize = if (isClosing) {
                        false
                    } else {
                        isResizeImeKeyboard(rootInsets)
                    }
                    updateSymbolBarVisibility(rootInsets)
                    applyKeyboardState(rootInsets)
                }
            },
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            if (!isAnimating) {
                activeKeyboardIsResize = if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                    isResizeImeKeyboard(insets)
                } else {
                    false
                }
                updateSymbolBarVisibility(insets)
                applyKeyboardState(insets)
            }
            insets
        }

        binding.root.post {
            ViewCompat.requestApplyInsets(binding.root)
        }
    }

    private fun setupMathPreviewSync() {
        binding.mathView.apply {
            fontSize = MATH_PREVIEW_FONT_SIZE_SP
            displayMode = true
        }
        binding.editMessage.doAfterTextChanged { text ->
            val latex = text?.toString().orEmpty()
            mathPreviewJob?.cancel()
            mathPreviewJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(MATH_PREVIEW_DEBOUNCE_MS)
                binding.mathView.latex = latex
            }
        }
    }

    private fun commitImeComposingText(editText: EditText) {
        val editorInfo = EditorInfo().apply {
            inputType = editText.inputType
            imeOptions = editText.imeOptions
        }
        editText.onCreateInputConnection(editorInfo)?.finishComposingText()
        editText.text?.let { BaseInputConnection.removeComposingSpans(it) }
    }

    private fun setupMathSymbols() {
        val symbols = listOf("\\", "{", "}", "=","^", "_", "|", "[", "]", "`", "~", "&", "%")
        val adapter = MathSymbolAdapter(symbols) { symbol ->
            val editText = binding.editMessage
            commitImeComposingText(editText)
            val start = editText.selectionStart
            val end = editText.selectionEnd
            editText.text.replace(start, end, symbol)
            editText.setSelection(start + symbol.length)
        }
        binding.rvMathSymbols.adapter = adapter
    }

    override fun onDestroyView() {
        mathPreviewJob?.cancel()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MATH_PREVIEW_FONT_SIZE_SP = 24f
        private const val MATH_PREVIEW_DEBOUNCE_MS = 100L
    }
}
