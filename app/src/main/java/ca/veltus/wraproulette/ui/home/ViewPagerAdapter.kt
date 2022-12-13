package ca.veltus.wraproulette.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ca.veltus.wraproulette.ui.home.bids.BidsFragment
import ca.veltus.wraproulette.ui.home.chat.ChatFragment
import ca.veltus.wraproulette.ui.home.summary.SummaryFragment

class ViewPagerAdapter(fragment: Fragment, val titles: Array<String>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SummaryFragment()
            1 -> BidsFragment()
            else -> ChatFragment()
        }
    }
}