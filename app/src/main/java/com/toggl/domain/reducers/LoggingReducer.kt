package com.toggl.domain.reducers

import android.util.Log
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.HigherOrderReducer
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.domain.AppAction
import com.toggl.domain.AppState
import com.toggl.domain.loading.formatForDebug
import com.toggl.onboarding.domain.actions.formatForDebug
import com.toggl.timer.common.domain.formatForDebug

class LoggingReducer(override val innerReducer: Reducer<AppState, AppAction>)
    : HigherOrderReducer<AppState, AppAction> {
    override fun reduce(
        state: SettableValue<AppState>,
        action: AppAction
    ): List<Effect<AppAction>> {
        Log.i(
            "LoggingReducer", when (action) {
                is AppAction.Onboarding -> action.onboarding.formatForDebug()
                is AppAction.Timer -> action.timer.formatForDebug()
                is AppAction.Loading -> action.loading.formatForDebug()
            }
        )

        return innerReducer.reduce(state, action)
    }
}