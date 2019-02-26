package com.ydly.rankingalarm2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ydly.rankingalarm2.R
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MainActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        info("onCreate() called")

        // Setting up Toolbar
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)

        // Setting up TabLayout with ViewPager and MainPagerAdapter
        val pagerAdapter = MainPagerAdapter(supportFragmentManager, this)
        viewpager_main.adapter = pagerAdapter
        tabs_main.setupWithViewPager(viewpager_main)
    }

    override fun onResume() {
        super.onResume()
        info("onResume() called")
    }

    override fun onPause() {
        super.onPause()
        info("onPause() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        info("onDestroy() called")
    }

    override fun onBackPressed() {
        if (viewpager_main.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewpager_main.currentItem = viewpager_main.currentItem - 1
        }
    }
}
