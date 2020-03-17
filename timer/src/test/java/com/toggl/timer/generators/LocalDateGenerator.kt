package com.toggl.timer.generators

import io.kotlintest.properties.Gen
import org.threeten.bp.LocalDate

fun Gen.Companion.threeTenLocalDate(): Gen<LocalDate> =
    localDate().map { LocalDate.of(it.year, it.monthValue, it.dayOfMonth) }