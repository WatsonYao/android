package com.toggl.timer.log.domain

import com.toggl.environment.services.time.TimeService
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.createTimeEntry
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldNotBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

class TimeEntriesLogSelectorTests : FreeSpec({

    val todayString = "Today"
    val yesterdayString = "Yesterday"
    val timeService = mockk<TimeService>()
    val today = OffsetDateTime.now()
    val localToday = today.toLocalDate()
    val yesterday = localToday.minusDays(1)

    val selectorToTest: (Map<Long, TimeEntry>, Map<Long, Project>) -> List<TimeEntryViewModel> =
        { timeEntries, projects ->
            timeEntriesLogSelector(
                timeEntries,
                projects,
                timeService,
                todayString,
                yesterdayString,
                false,
                setOf()
            )
        }

    val groupSelectorToTest: (Map<Long, TimeEntry>, Map<Long, Project>) -> List<TimeEntryViewModel> =
        { timeEntries, projects ->
            timeEntriesLogSelector(
                timeEntries,
                projects,
                timeService,
                todayString,
                yesterdayString,
                true,
                setOf()
            )
        }

    every { timeService.now() } returns today

    /*
     *  Test Data
     */
    val projectsMap = mapOf(
        1L to Project(
            id = 1,
            name = "First Project",
            color = "",
            active = true,
            isPrivate = true,
            billable = true,
            workspaceId = 1,
            clientId = 1
        ),
        2L to Project(
            id = 2,
            name = "Second Project",
            color = "",
            active = true,
            isPrivate = true,
            billable = true,
            workspaceId = 1,
            clientId = 2
        )
    )

    val timeEntries = (0L..10L).flatMap { daysInThePast ->
        val date = today.minusDays(daysInThePast)
        (0L..10L).map { id ->
            createTimeEntry(
                id = daysInThePast + id,
                description = "Time entry $id",
                startTime = date,
                duration = Duration.ofMinutes(30)
            )
        }
    }

    val shortDuration = Duration.ofMinutes(30)

    fun List<TimeEntry>.mapToYesterday() = this.map { it.copy(id = it.id * 100, startTime = it.startTime.minusDays(1)) }

    val singleItem = listOf(
        createTimeEntry(
            id = 1,
            description = "S",
            startTime = today,
            duration = shortDuration
        )
    )

    val yesterdaySingleItem = singleItem.mapToYesterday()

    val groupA = (2L..5L).map { id ->
        createTimeEntry(
            id = id,
            description = "A",
            startTime = today,
            duration = shortDuration.plusMinutes(id)
        )
    }

    val yesterdayGroupA = groupA.mapToYesterday()

    val groupB = (6L..9L).map { id ->
        createTimeEntry(
            id = id,
            description = "B",
            startTime = today,
            duration = shortDuration.plusMinutes(id)
        )
    }

    val yesterdayGroupB = groupB.mapToYesterday()

    val twoProjects = listOf(
        createTimeEntry(
            id = 10,
            description = "B",
            startTime = today,
            duration = shortDuration.plusMinutes(10),
            projectId = 1
        ),
        createTimeEntry(
            id = 11,
            description = "B",
            startTime = today,
            duration = shortDuration.plusMinutes(11),
            projectId = 2
        )
    )

    val yesterdayTwoProjects = twoProjects.mapToYesterday()

    val differentDescriptions = listOf(
        createTimeEntry(
            id = 12,
            description = "C1",
            startTime = today,
            duration = shortDuration.plusMinutes(12)
        ),
        createTimeEntry(
            id = 13,
            description = "C1",
            startTime = today,
            duration = shortDuration.plusMinutes(13)
        ),
        createTimeEntry(
            id = 14,
            description = "C2",
            startTime = today,
            duration = shortDuration.plusMinutes(14)
        )
    )

    val yesterdayDifferentDescriptions = differentDescriptions.mapToYesterday()

    val longDuration = listOf(
        createTimeEntry(
            id = 15,
            description = "D1",
            startTime = today,
            duration = Duration.ofHours(1)
        ),
        createTimeEntry(
            id = 16,
            description = "D1",
            startTime = today,
            duration = Duration.ofHours(2)
        ),
        createTimeEntry(
            id = 17,
            description = "D3",
            startTime = today,
            duration = Duration.ofHours(3)
        )
    )

    val yesterdayLongDuration = longDuration.mapToYesterday()

    val todayEntries = (
        singleItem +
            groupA +
            groupB +
            twoProjects +
            differentDescriptions +
            longDuration
        )

    val yesterdayEntries = (
        yesterdaySingleItem +
            yesterdayGroupA +
            yesterdayGroupB +
            yesterdayTwoProjects +
            yesterdayDifferentDescriptions +
            yesterdayLongDuration
        )

    val similarTimeEntries: List<TimeEntry> = yesterdayEntries + todayEntries

    val timeEntriesMap = timeEntries.associateBy { it.id }
    val similarTimeEntriesMap = similarTimeEntries.associateBy { it.id }

    /*
     *  Expected Data
     */
    val expectedTodayGroupedTimeEntries: List<TimeEntryViewModel> = listOf(
        singleItem.first().toFlatTimeEntryViewModel(projectsMap),
        groupA.toTimeEntryGroupViewModel(1, false, projectsMap),
        groupB.toTimeEntryGroupViewModel(1, false, projectsMap),
        twoProjects.first().toFlatTimeEntryViewModel(projectsMap),
        twoProjects[1].toFlatTimeEntryViewModel(projectsMap),
        differentDescriptions.dropLast(1).toTimeEntryGroupViewModel(1, false, projectsMap),
        differentDescriptions.last().toFlatTimeEntryViewModel(projectsMap),
        longDuration.dropLast(1).toTimeEntryGroupViewModel(1, false, projectsMap),
        longDuration.last().toFlatTimeEntryViewModel(projectsMap)
    )

    val expectedYesterdayGroupedTimeEntries = expectedTodayGroupedTimeEntries.map {
        when (it) {
            is FlatTimeEntryViewModel -> it.copy(id = it.id * 100, startTime = it.startTime.minusDays(1))
            is TimeEntryGroupViewModel -> it.copy(timeEntryIds = it.timeEntryIds.map { id -> id * 100 })
            else -> it
        }
    }

    val expectedGroupedTimeEntries: List<TimeEntryViewModel> = listOf(
        DayHeaderViewModel(
            dayTitle = todayString,
            totalDuration = todayEntries.totalDuration()
        )
    ) + expectedTodayGroupedTimeEntries + listOf(
        DayHeaderViewModel(
            dayTitle = yesterdayString,
            totalDuration = yesterdayEntries.totalDuration()
        )
    ) + expectedYesterdayGroupedTimeEntries

    "The TimeEntriesLogSelector" - {
        "does not" - {
            "add running time entries to the groups" - {
                val timeEntriesButAllRunning = timeEntries.map { it.copy(duration = null) }
                val groupedTimeEntries = selectorToTest(
                    timeEntriesButAllRunning.associateBy { it.id },
                    projectsMap
                )

                groupedTimeEntries.size shouldBe 0
            }

            "create headers for days that have no time entries" - {

                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap
                )

                val headers =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }

                val flatTimeEntryHeaders =
                    groupedTimeEntries
                        .asSequence()
                        .filterIsInstance<FlatTimeEntryViewModel>()
                        .map { it.startTime.toLocalDate() }
                        .distinct()
                        .map {
                            when (it) {
                                localToday -> todayString
                                yesterday -> yesterdayString
                                else -> it.format(
                                    DateTimeFormatter.ofPattern("eee, dd MMM")
                                )
                            }
                        }.distinct().toList()

                headers shouldContainExactlyInAnyOrder flatTimeEntryHeaders
            }
            "group similar time entries when grouping is disabled" - {

                val notGroupedTimeEntries = selectorToTest(
                    similarTimeEntriesMap,
                    projectsMap
                )

                notGroupedTimeEntries.forEach {
                    it.shouldNotBeTypeOf<TimeEntryGroupViewModel>()
                }
            }
        }

        "the header titles are formatted" - {

            "as the localized string for Today when the header is for the current day" - {
                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap
                )

                val headerTitles =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }

                headerTitles shouldContain todayString
            }

            "as the localized string for Today when the header is for the previous day" - {
                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap
                )

                val headerTitles =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }

                headerTitles shouldContain yesterdayString
            }

            "using eee, dd MMM for all other days" - {
                val formatter = DateTimeFormatter.ofPattern("eee, dd MMM")

                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap
                )

                val headerTitles =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }
                        .filter { it != todayString && it != yesterdayString }
                        .distinct()

                val formattedDates =
                    groupedTimeEntries
                        .asSequence()
                        .filterIsInstance<FlatTimeEntryViewModel>()
                        .map { it.startTime.toLocalDate() }
                        .distinct()
                        .sortedDescending()
                        .drop(2)
                        .map { it.format(formatter) }
                        .toList()

                headerTitles shouldContainExactlyInAnyOrder formattedDates
            }
        }

        "groups similar time entries" - {
            val groupedTimeEntries = groupSelectorToTest(
                similarTimeEntriesMap,
                projectsMap
            )
            groupedTimeEntries shouldBe expectedGroupedTimeEntries
        }
    }
})