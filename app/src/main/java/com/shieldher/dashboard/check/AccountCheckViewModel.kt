package com.shieldher.dashboard.check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.dashboard.check.risk.RiskInputParser
import com.shieldher.dashboard.data.RiskOutcome
import com.shieldher.dashboard.data.RiskPlatform
import com.shieldher.dashboard.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State representing the Account Check screen lifecycle.
 */
sealed interface AccountCheckState {
    object Idle : AccountCheckState
    object Computing : AccountCheckState
    data class Done(val outcome: RiskOutcome) : AccountCheckState
}

/**
 * ViewModel for Account Check.
 * Privacy rule: The ViewModel holds ONLY the state and outcome.
 * Raw user input strings exist only inside [run] and are never stored in fields.
 */
class AccountCheckViewModel : ViewModel() {

    private val _state = MutableStateFlow<AccountCheckState>(AccountCheckState.Idle)
    val state: StateFlow<AccountCheckState> = _state.asStateFlow()

    fun run(
        platform: RiskPlatform?,
        followersText: String?,
        followingText: String?,
        bioText: String?,
        hasLink: Boolean
    ) {
        val parsedInput = RiskInputParser.parse(
            platform = platform,
            followersText = followersText,
            followingText = followingText,
            bioText = bioText,
            hasLink = hasLink
        )

        _state.value = AccountCheckState.Computing

        viewModelScope.launch {
            val outcome = ServiceLocator.riskChecker.check(parsedInput)
            _state.value = AccountCheckState.Done(outcome)
        }
    }

    fun clear() {
        _state.value = AccountCheckState.Idle
    }
}
