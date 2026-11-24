package com.shieldher.dashboard.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.shieldher.R
import com.shieldher.dashboard.common.AppSettings

class OnboardingFragment : Fragment() {

    private data class OnboardingPage(
        val headingRes: Int,
        val bodyRes: Int
    )

    private val pages = listOf(
        OnboardingPage(R.string.onboarding_page1_heading, R.string.onboarding_page1_body),
        OnboardingPage(R.string.onboarding_page2_heading, R.string.onboarding_page2_body),
        OnboardingPage(R.string.onboarding_page3_heading, R.string.onboarding_page3_body)
    )

    private lateinit var appSettings: AppSettings
    private lateinit var viewPager: ViewPager2
    private lateinit var textIndicator: TextView
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appSettings = AppSettings(requireContext())

        viewPager = view.findViewById(R.id.view_pager_onboarding)
        textIndicator = view.findViewById(R.id.text_page_indicator)
        btnBack = view.findViewById(R.id.btn_back)
        btnNext = view.findViewById(R.id.btn_next)
        btnSkip = view.findViewById(R.id.btn_skip)

        viewPager.adapter = OnboardingPagerAdapter(pages)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUiForPage(position)
            }
        })

        // Initial state for page 0
        updateUiForPage(0)

        btnSkip.setOnClickListener { finishOnboarding() }

        btnBack.setOnClickListener {
            val current = viewPager.currentItem
            if (current > 0) viewPager.currentItem = current - 1
        }

        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (OnboardingStateHelper.isLastPage(current, pages.size)) {
                finishOnboarding()
            } else {
                viewPager.currentItem = current + 1
            }
        }
    }

    private fun updateUiForPage(pageIndex: Int) {
        val total = pages.size

        // Page indicator text
        textIndicator.text = getString(R.string.onboarding_page_indicator, pageIndex + 1, total)

        // Skip visibility: visible on all but last page
        btnSkip.visibility = if (OnboardingStateHelper.shouldShowSkip(pageIndex, total)) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Back visibility: gone on first page
        btnBack.visibility = if (OnboardingStateHelper.shouldShowBack(pageIndex)) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Next button label changes on last page
        btnNext.setText(
            if (OnboardingStateHelper.isLastPage(pageIndex, total)) {
                R.string.onboarding_action_get_started
            } else {
                R.string.onboarding_action_next
            }
        )
    }

    private fun finishOnboarding() {
        appSettings.onboardingSeen = true
        findNavController().popBackStack()
    }

    // ---------------------------------------------------------------------------
    // Inner adapter
    // ---------------------------------------------------------------------------

    private inner class OnboardingPagerAdapter(
        private val items: List<OnboardingPage>
    ) : RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder>() {

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_page, parent, false)
            return PageViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            holder.bind(items[position])
        }

        inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val heading: TextView = itemView.findViewById(R.id.text_onboarding_heading)
            private val body: TextView = itemView.findViewById(R.id.text_onboarding_body)

            fun bind(page: OnboardingPage) {
                heading.setText(page.headingRes)
                body.setText(page.bodyRes)
            }
        }
    }
}
