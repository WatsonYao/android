package com.toggl.timer.generators

import com.toggl.models.domain.TimeEntry
import io.kotlintest.properties.Gen

fun Gen.Companion.description() =
    string(2000)

fun Gen.Companion.timeEntries() =
    bind(
        positiveLong(),
        description(),
        offsetDateTime(),
        threeTenDuration(),
        bool(),
        positiveLong(),
        positiveLong()
    ) { id, description, startTime, duration, billable, projectId, taskId ->
        TimeEntry(
            id,
            description,
            startTime,
            duration,
            billable,
            projectId,
            taskId,
            false
        )
    }