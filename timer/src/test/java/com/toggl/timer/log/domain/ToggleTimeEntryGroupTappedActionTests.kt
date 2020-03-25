package com.toggl.timer.log.domain

import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.specs.FreeSpec
import io.mockk.mockk

class ToggleTimeEntryGroupTappedActionTests : FreeSpec({
    val repository = mockk<TimeEntryRepository>()
    val reducer = TimeEntriesLogReducer(repository)

    "The ToggleTimeEntryGroupTappedAction action" - {
        "should add groupId to expandedGroupId if it wasn't there before" {
            reducer.testReduce(
                initialState = createInitialState(expandedGroupIds = setOf(2, 3)),
                action = TimeEntriesLogAction.ToggleTimeEntryGroupTapped(1)
            ) { state, _ ->
                state.expandedGroupIds shouldContain 1
            }
        }
        "should remove groupId from expandedGroupId if it was already there" {
            reducer.testReduce(
                initialState = createInitialState(expandedGroupIds = setOf(1, 2, 3)),
                action = TimeEntriesLogAction.ToggleTimeEntryGroupTapped(1)
            ) { state, _ ->
                state.expandedGroupIds shouldNotContain 1
            }
        }
        "shouldn't return any effect" {
            reducer.testReduce(
                initialState = createInitialState(expandedGroupIds = setOf(1, 2, 3)),
                action = TimeEntriesLogAction.ToggleTimeEntryGroupTapped(1)
            ) { _, effect ->
                effect.shouldBeEmpty()
            }
        }
    }
})