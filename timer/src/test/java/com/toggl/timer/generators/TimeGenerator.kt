package com.toggl.timer.generators

import io.kotlintest.properties.Gen
import org.threeten.bp.LocalTime

fun Gen.Companion.threeTenLocalTime() =
    localTime().map { LocalTime.of(it.hour, it.minute, it.second) }