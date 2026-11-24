package com.shieldher.dashboard

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.R
import com.shieldher.dashboard.common.AppSettings
import com.shieldher.dashboard.data.ServiceLocator
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FE-4.11: Espresso core-flow test running against fake data (no real vault or picker dependencies).
 *
 * Automates:
 * 1) Core flow: Home -> Evidence tab -> Open first item -> Detail shows hash & "Not checked yet"
 *    -> Tap Verify -> Result badge changes to "Unchanged since saved"
 *    -> Tap Add to case -> Choose case -> Confirm -> Open Cases tab -> Open case -> Timeline shows item -> Back.
 * 2) More tab verification: More tab shows Settings, Account Check, Welcome guide rows.
 */
@RunWith(AndroidJUnit4::class)
class CoreFlowTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Mark onboarding as seen so MainActivity launches directly into Home
        AppSettings(context).onboardingSeen = true

        // Install fakes before launching MainActivity
        ServiceLocator.installFakes()
    }

    @Test
    fun testCoreFlow() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Step 1: MainActivity launched, verify Home screen content is displayed
            onView(withId(R.id.content_scroll_view)).check(matches(isDisplayed()))

            // Step 2: Open Evidence tab via bottom navigation
            onView(withId(R.id.evidence)).perform(click())
            onView(withId(R.id.recycler_evidence)).check(matches(isDisplayed()))

            // Step 3: Tap evidence item (located by unique source label)
            onView(allOf(withId(R.id.card_title), withText(containsString("Example email")))).perform(click())

            // Step 4: Detail shows hash label and "Not checked yet" status
            onView(withId(R.id.text_hash_value)).check(matches(isDisplayed()))
            onView(withId(R.id.verification_badge)).check(matches(hasDescendant(withText(R.string.status_not_checked))))

            // Step 5: Tap Verify button
            onView(withId(R.id.button_verify)).perform(scrollTo(), click())

            // Step 6: Verification result badge updates to "Unchanged since saved"
            onView(withId(R.id.verification_badge)).check(matches(hasDescendant(withText(R.string.status_unchanged))))

            // Step 7: Tap "Add to case" button
            onView(withId(R.id.button_add_to_case)).perform(scrollTo(), click())

            // Step 8: Dialog appears - confirm selection (default case is "Example Harassment Incident 1")
            onView(withText(R.string.detail_action_confirm_add)).perform(click())

            // Step 9: Open Cases tab via bottom navigation
            onView(withId(R.id.cases)).perform(click())
            onView(withId(R.id.recycler_cases)).check(matches(isDisplayed()))

            // Step 10: Open that case
            onView(withText("Example Harassment Incident 1")).perform(click())

            // Step 11: Case timeline shows items
            onView(withId(R.id.recycler_timeline)).check(matches(isDisplayed()))

            // Step 12: Tap back to return to cases list
            pressBack()
            onView(withId(R.id.recycler_cases)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testMoreTabRows() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Open More tab via bottom navigation
            onView(withId(R.id.more)).perform(click())

            // Check that the three rows are shown
            onView(withId(R.id.row_settings)).check(matches(isDisplayed()))
            onView(withText(R.string.title_settings)).check(matches(isDisplayed()))

            onView(withId(R.id.row_account_check)).check(matches(isDisplayed()))
            onView(withText(R.string.title_account_check)).check(matches(isDisplayed()))

            onView(withId(R.id.row_welcome_guide)).check(matches(isDisplayed()))
            onView(withText(R.string.title_welcome_guide)).check(matches(isDisplayed()))
        }
    }

    private fun <T> first(matcher: Matcher<T>): Matcher<T> {
        return object : TypeSafeMatcher<T>() {
            private var isFirst = true

            override fun describeTo(description: Description) {
                description.appendText("first matching: ")
                matcher.describeTo(description)
            }

            override fun matchesSafely(item: T): Boolean {
                if (matcher.matches(item)) {
                    if (isFirst) {
                        isFirst = false
                        return true
                    }
                }
                return false
            }
        }
    }
}
