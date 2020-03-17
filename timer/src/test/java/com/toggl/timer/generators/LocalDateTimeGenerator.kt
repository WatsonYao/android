package com.toggl.timer.generators

import io.kotlintest.properties.Gen
import org.threeten.bp.LocalDateTime

fun Gen.Companion.threeTenLocalDateTime() =
    bind(threeTenLocalDate(), threeTenLocalTime(), LocalDateTime::of)