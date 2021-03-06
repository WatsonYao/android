package com.toggl.timer.log.domain

import com.toggl.environment.services.time.TimeService
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

private const val timeEntriesLogHeaderTimeFormat = "eee, dd MMM"

fun timeEntriesLogSelector(
    timeEntries: Map<Long, TimeEntry>,
    projects: Map<Long, Project>,
    timeService: TimeService,
    todayString: String,
    yesterdayString: String,
    shouldGroup: Boolean
): List<TimeEntryViewModel> {

    val today = timeService.now().toLocalDate()
    val yesterday = today.minusDays(1)

    fun TimeEntry.similarityHashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + billable.hashCode()
        result = 31 * result + (projectId?.hashCode() ?: 0)
        result = 31 * result + (taskId?.hashCode() ?: 0)
        return result
    }

    fun List<TimeEntry>.mapToGroups(): List<List<TimeEntry>> =
        this.groupBy(TimeEntry::similarityHashCode)
            .map { (_, timeEntries) -> timeEntries }

    suspend fun SequenceScope<TimeEntryViewModel>.yieldDayHeader(
        groupDate: LocalDate,
        timeEntries: List<TimeEntry>
    ) {
        yield(
            DayHeaderViewModel(
                dayTitle = when (groupDate) {
                    today -> todayString
                    yesterday -> yesterdayString
                    else -> groupDate.format(
                        DateTimeFormatter.ofPattern(
                            timeEntriesLogHeaderTimeFormat
                        )
                    )
                },
                totalDuration = timeEntries.totalDuration()
            )
        )
    }

    suspend fun SequenceScope<TimeEntryViewModel>.yieldFlatTimeEntry(timeEntry: TimeEntry) =
        yield(timeEntry.toFlatTimeEntryViewModel(projects))

    suspend fun SequenceScope<TimeEntryViewModel>.yieldTimeEntryGroup(timeEntries: List<TimeEntry>) =
        yield(timeEntries.toTimeEntryGroupViewModel(projects))

    return timeEntries.values
        .filter { it.duration != null && !it.isDeleted }
        .sortedByDescending { it.startTime }
        .groupBy { timeEntry -> timeEntry.startTime.toLocalDate() }
        .flatMap { (groupDate, timeEntries) ->
            sequence<TimeEntryViewModel> {
                yieldDayHeader(groupDate, timeEntries)

                if (shouldGroup) {
                    val timeEntryGroups = timeEntries.mapToGroups()
                    for (timeEntryGroup in timeEntryGroups) {
                        if (timeEntryGroup.size == 1) {
                            yieldFlatTimeEntry(timeEntryGroup.first())
                        } else {
                            yieldTimeEntryGroup(timeEntryGroup)
                        }
                    }
                } else {
                    for (timeEntry in timeEntries) {
                        yieldFlatTimeEntry(timeEntry)
                    }
                }
            }.toList()
        }
}