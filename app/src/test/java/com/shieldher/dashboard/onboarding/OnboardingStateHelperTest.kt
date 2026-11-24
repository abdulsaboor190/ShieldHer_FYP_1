package com.shieldher.dashboard.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStateHelperTest {

    // -----------------------------------------------------------------------
    // shouldShowSkip
    // -----------------------------------------------------------------------

    @Test
    fun `shouldShowSkip returns true on first page of three`() {
        assertTrue(OnboardingStateHelper.shouldShowSkip(pageIndex = 0, totalPages = 3))
    }

    @Test
    fun `shouldShowSkip returns true on middle page of three`() {
        assertTrue(OnboardingStateHelper.shouldShowSkip(pageIndex = 1, totalPages = 3))
    }

    @Test
    fun `shouldShowSkip returns false on last page of three`() {
        assertFalse(OnboardingStateHelper.shouldShowSkip(pageIndex = 2, totalPages = 3))
    }

    @Test
    fun `shouldShowSkip returns false on only page`() {
        assertFalse(OnboardingStateHelper.shouldShowSkip(pageIndex = 0, totalPages = 1))
    }

    // -----------------------------------------------------------------------
    // shouldShowBack
    // -----------------------------------------------------------------------

    @Test
    fun `shouldShowBack returns false on first page`() {
        assertFalse(OnboardingStateHelper.shouldShowBack(pageIndex = 0))
    }

    @Test
    fun `shouldShowBack returns true on second page`() {
        assertTrue(OnboardingStateHelper.shouldShowBack(pageIndex = 1))
    }

    @Test
    fun `shouldShowBack returns true on last page of three`() {
        assertTrue(OnboardingStateHelper.shouldShowBack(pageIndex = 2))
    }

    // -----------------------------------------------------------------------
    // isLastPage
    // -----------------------------------------------------------------------

    @Test
    fun `isLastPage returns false on first page of three`() {
        assertFalse(OnboardingStateHelper.isLastPage(pageIndex = 0, totalPages = 3))
    }

    @Test
    fun `isLastPage returns false on middle page of three`() {
        assertFalse(OnboardingStateHelper.isLastPage(pageIndex = 1, totalPages = 3))
    }

    @Test
    fun `isLastPage returns true on last page of three`() {
        assertTrue(OnboardingStateHelper.isLastPage(pageIndex = 2, totalPages = 3))
    }

    @Test
    fun `isLastPage returns true on only page`() {
        assertTrue(OnboardingStateHelper.isLastPage(pageIndex = 0, totalPages = 1))
    }

    @Test
    fun `isLastPage returns false when totalPages is zero`() {
        assertFalse(OnboardingStateHelper.isLastPage(pageIndex = 0, totalPages = 0))
    }
}
