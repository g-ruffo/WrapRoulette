package ca.veltus.wraproulette.ui.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ca.veltus.wraproulette.ui.home.bids.BidsFragment
import ca.veltus.wraproulette.ui.home.chat.ChatFragment
import ca.veltus.wraproulette.ui.home.summary.SummaryFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    companion object {
    val fragmentTitle = arrayListOf<String>("Summary", "Bids", "Chat")
}

    override fun getItemCount(): Int {
        return fragmentTitle.size
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> SummaryFragment()
            1 -> BidsFragment()
            else -> ChatFragment()
        }
    }
}