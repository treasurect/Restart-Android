package com.treasure.restart.func.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfilePagerAdapter(
    fragment: Fragment,
    private val titles: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment {
        return ProfileTabFragment.newInstance(position)
    }
}
