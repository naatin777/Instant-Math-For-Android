package com.naatin777.instantmath.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.naatin777.instantmath.databinding.FragmentHomeListBinding

class HomeListFragment : Fragment() {

    private var _binding: FragmentHomeListBinding? = null
    private val binding get() = _binding!!

    private val tab: HomeListTab
        get() = HomeListTab.entries[requireArguments().getInt(ARG_TAB)]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = FormulaEntryAdapter(tab.dummyEntries())
    }

    fun setImePadding(imeBottom: Int) {
        val basePadding = (16 * resources.displayMetrics.density).toInt()
        _binding?.recyclerView?.updatePadding(bottom = basePadding + imeBottom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAB = "tab"

        fun newInstance(tab: HomeListTab): HomeListFragment =
            HomeListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB, tab.ordinal)
                }
            }
    }
}
