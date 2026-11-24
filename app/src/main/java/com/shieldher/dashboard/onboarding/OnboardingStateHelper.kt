package com.shieldher.dashboard.onboarding

/**
 * Pure logic helpers for onboarding navigation state.
 *
 * All functions are free of Android dependencies so they can be unit-tested
 * without an instrumented device or mocking framework.
 */
object OnboardingStateHelper {

    /**
     * Returns true when the Skip button should be visible.
     * Skip is available on all pages except the last one.
     */
    fun shouldShowSkip(pageIndex: Int, totalPages: Int): Boolean =
        pageIndex < totalPages - 1

    /**
     * Returns true when the Back button should be visible.
     * Back is hidden on the first page (index 0).
     */
    fun shouldShowBack(pageIndex: Int): Boolean =
        pageIndex > 0

    /**
     * Returns true when [pageIndex] is the last page (index == totalPages - 1).
     * Used to decide whether to show "Get started" vs "Next".
     */
    fun isLastPage(pageIndex: Int, totalPages: Int): Boolean =
        totalPages > 0 && pageIndex == totalPages - 1
}
