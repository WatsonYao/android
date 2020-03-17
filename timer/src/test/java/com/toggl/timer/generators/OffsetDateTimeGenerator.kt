package com.toggl.timer.generators

import io.kotlintest.properties.Gen
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

fun Gen.Companion.offsetDateTime()  =
    threeTenLocalDateTime().map { OffsetDateTime.of(it, ZoneOffset.UTC) }