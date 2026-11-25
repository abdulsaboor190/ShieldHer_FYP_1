package com.shieldher.dashboard.check

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.R
import com.shieldher.dashboard.MainActivity
import com.shieldher.dashboard.common.AppSettings
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountCheckFlowTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Ensure production real model is used (clear any fakes installed by previous test classes in same runner)
        com.shieldher.dashboard.data.ServiceLocator.resetToDefaults(context)
        // Mark onboarding as seen so MainActivity launches directly into Dashboard
        AppSettings(context).onboardingSeen = true
    }

    private fun doesNotContainPercent(): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("View does not contain a percent sign '%' in text")
            }

            override fun matchesSafely(view: View): Boolean {
                if (view is TextView) {
                    val text = view.text?.toString() ?: ""
                    return !text.contains("%")
                }
                return true
            }
        }
    }

    @Test
    fun testAccountCheckFlowRealModel() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // 1. Navigate to More tab
            onView(withId(R.id.more)).perform(click())

            // 2. Click Account Check row
            onView(withId(R.id.row_account_check)).perform(click())

            // 3. Verify Account Check screen loaded
            onView(withId(R.id.text_risk_limits_heading)).check(matches(isDisplayed()))

            // 4. Select Instagram platform chip
            onView(withId(R.id.chip_instagram)).perform(click())

            // 5. Enter followers and following counts
            onView(withId(R.id.input_followers)).perform(
                scrollTo(),
                replaceText("500"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.input_following)).perform(
                scrollTo(),
                replaceText("200"),
                closeSoftKeyboard()
            )

            // 6. Run check
            onView(withId(R.id.btn_run_check)).perform(scrollTo(), click())

            // 7. Result container should be displayed with one of the allowed titles
            onView(withId(R.id.container_result)).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.text_risk_title)).check(
                matches(
                    anyOf(
                        withText(R.string.risk_title_higher),
                        withText(R.string.risk_title_lower),
                        withText(R.string.risk_title_uncertain),
                        withText(R.string.risk_title_unsupported)
                    )
                )
            )

            // 8. Limits text must remain visible
            onView(withId(R.id.text_risk_limits_heading)).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.text_risk_limits_body)).perform(scrollTo()).check(matches(isDisplayed()))

            // 9. Assert no view shows a percent sign
            onView(withId(R.id.text_risk_title)).check(matches(doesNotContainPercent()))
            onView(withId(R.id.text_risk_explanation)).check(matches(doesNotContainPercent()))

            // 10. Tap Clear and verify fields are emptied
            onView(withId(R.id.btn_clear)).perform(scrollTo(), click())
            onView(withId(R.id.input_followers)).check(matches(withText("")))
            onView(withId(R.id.input_following)).check(matches(withText("")))
            onView(withId(R.id.container_result)).check(matches(not(isDisplayed())))

            // 11. Choose X (Twitter) chip and Run check
            // Use ChipGroup.check to change selection directly and reliably in Espresso
            onView(withId(R.id.chip_group_platform)).perform(scrollTo(), object : androidx.test.espresso.ViewAction {
                override fun getConstraints(): Matcher<View> = androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(View::class.java)
                override fun getDescription(): String = "check chip_x in chip_group_platform"
                override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
                    (view as? com.google.android.material.chip.ChipGroup)?.check(R.id.chip_x)
                }
            })
            onView(withId(R.id.input_followers)).perform(
                scrollTo(),
                replaceText("100"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.input_following)).perform(
                scrollTo(),
                replaceText("50"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.btn_run_check)).perform(scrollTo(), click())

            // 12. Assert title is Not validated for this platform
            onView(withId(R.id.container_result)).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.text_risk_title)).check(matches(withText(R.string.risk_title_not_validated)))
            onView(withId(R.id.text_risk_explanation)).check(matches(withText(R.string.risk_expl_not_validated)))
        }
    }
}
