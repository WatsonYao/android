package com.toggl.timer.log.domain

import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.toSettableValue
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec
import io.mockk.mockk

class TimeEntryTappedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val reducer = TimeEntriesLogReducer(repository)
    val testTe = createTimeEntry(1, "test")

    "The TimeEntryTapped action" - {
        "should thrown when there are no time entries" - {
            "with the matching id" {
                val initialState = createInitialState(listOf(testTe))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }
                shouldThrow<IllegalStateException> {
                    reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryTapped(2))
                }
            }

            "at all" {
                val initialState = createInitialState()
                assertAll(fn = { id: Long ->
                    var state = initialState
                    val settableValue = state.toSettableValue { state = it }
                    shouldThrow<IllegalStateException> {
                        reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryTapped(id))
                    }
                })
            }
        }

        "set the editing time entry property when the time entry exists" {
            val initialState = createInitialState(listOf(testTe))

            var state = initialState
            val settableValue = state.toSettableValue { state = it }
            reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryTapped(1))
            state.editableTimeEntry!!.ids.single() shouldBe testTe.id
        }
    }
})
