package com.ydly.rankingalarm2.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.ui.alarm.SingleAlarmFragment
import com.ydly.rankingalarm2.ui.ranking.RankingFragment

private const val NUM_PAGES = 2

class MainPagerAdapter(fm: FragmentManager, context: Context) : FragmentPagerAdapter(fm) {
    private val ctx = context

    override fun getCount(): Int = NUM_PAGES

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> SingleAlarmFragment.newInstance()
            else -> RankingFragment.newInstance()
        }

    override fun getPageTitle(position: Int): CharSequence? =
        when (position) {
            0 -> ctx.getString(R.string.alarm)
            else -> ctx.getString(R.string.ranking)
        }
}
