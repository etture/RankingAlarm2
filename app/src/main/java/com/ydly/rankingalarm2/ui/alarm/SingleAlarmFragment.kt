package com.ydly.rankingalarm2.ui.alarm

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseFragment

class SingleAlarmFragment : BaseFragment() {

    companion object {
        fun newInstance() = SingleAlarmFragment()
    }

    private lateinit var viewModel: SingleAlarmViewModel

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.single_alarm_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SingleAlarmViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
