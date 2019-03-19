package com.ydly.rankingalarm2.ui.ranking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseFragment
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.databinding.FragmentRankingBinding
import com.ydly.rankingalarm2.util.SingleEvent
import org.jetbrains.anko.info
import java.util.*


class RankingFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = RankingFragment()
    }

    private val viewModel: RankingViewModel by lazy {
        ViewModelProviders.of(activity!!).get(RankingViewModel::class.java)
    }
    private lateinit var binding: FragmentRankingBinding

    private lateinit var minuteTickReceiver: BroadcastReceiver

    private lateinit var refreshObserver: Observer<SingleEvent<Boolean>>

    override fun initialize(inflater: LayoutInflater, container: ViewGroup?) {
        //ViewModel and DataBinding setup
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking, container, false)
        binding.viewModel = viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        initialize(inflater, container)
        info("onCreateView()")

        binding.rankFragSwipeRefresh.setColorSchemeResources(
            R.color.colorAccent, R.color.colorCancel, R.color.colorConfirm, R.color.black
        )


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        info("onResume()")

        // Every time in onResume(), attempt to upload pending alarmHistory items
        viewModel.attemptUploadPendingHistory()

        // Receiver to receive intent every minute
        minuteTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                info("minuteTickReceiver -> onReceive()")
                if (action == Intent.ACTION_TIME_TICK) {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    info("minuteTickReceiver -> minute ticked -> hour: $hour, minute: $minute")

                    // When day has passed
                    if (hour == 0 && minute == 0) {
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                        viewModel.updateLatestNumPeople(year, month, dayOfMonth)
                    }
                }
            }
        }
        activity?.registerReceiver(minuteTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

        // Observer to turn SwipeRefreshLayout back off
        refreshObserver = Observer {
            it?.getContentIfNotHandled()?.let { refreshStatus ->
                binding.rankFragSwipeRefresh.isRefreshing = refreshStatus
            }
        }
        viewModel.apply {
            observeRefreshEvent().observe(activity!!, refreshObserver)
        }
    }

    override fun onPause() {
        super.onPause()
        info("onPause()")

        activity?.unregisterReceiver(minuteTickReceiver)

        viewModel.apply {
            observeRefreshEvent().removeObserver(refreshObserver)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSubscription()
    }
}
