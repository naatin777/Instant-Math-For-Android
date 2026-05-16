package com.naatin777.instantmath.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = HomeListTab.entries.size

    override fun createFragment(position: Int): Fragment =
        HomeListFragment.newInstance(HomeListTab.entries[position])
}
