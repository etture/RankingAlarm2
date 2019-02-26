package com.ydly.rankingalarm2.ui.ranking

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseFragment
import com.ydly.rankingalarm2.databinding.FragmentRankingBinding
import kotlinx.android.synthetic.main.pane_util_btns.*
import org.jetbrains.anko.info


class RankingFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = RankingFragment()
    }

    private val viewModel: RankingViewModel by lazy {
        ViewModelProviders.of(activity!!).get(RankingViewModel::class.java)
    }
    private lateinit var binding: FragmentRankingBinding

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

        return binding.root
    }

}
