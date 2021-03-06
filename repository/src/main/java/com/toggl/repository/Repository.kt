package com.toggl.repository

import com.toggl.database.dao.TimeEntryDao
import com.toggl.database.dao.WorkspaceDao
import com.toggl.environment.services.time.TimeService
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.repository.interfaces.StartTimeEntryResult
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.repository.interfaces.WorkspaceRepository
import org.threeten.bp.Duration

class Repository(
    private val timeEntryDao: TimeEntryDao,
    private val workspaceDao: WorkspaceDao,
    private val timeService: TimeService
) : TimeEntryRepository, WorkspaceRepository {

    override suspend fun loadTimeEntries() =
        timeEntryDao.getAll()

    override suspend fun loadWorkspaces() =
        workspaceDao.getAll().let {
            if (it.any())
                return@let it

            // Automatically create a default workspace
            val workspace = Workspace(
                name = "Auto created workspace",
                features = listOf(WorkspaceFeature.Pro)
            )
            val workspaceId = workspaceDao.insert(workspace)

            return@let listOf(workspace.copy(id = workspaceId))
        }

    override suspend fun startTimeEntry(workspaceId: Long, description: String): StartTimeEntryResult {
        val stoppedTimeEntry = stopRunningTimeEntry()
        val id = timeEntryDao.insert(
            TimeEntry(
                description = description,
                startTime = timeService.now(),
                duration = null,
                billable = false,
                workspaceId = workspaceId,
                projectId = null,
                taskId = null,
                isDeleted = false
            )
        )
        return StartTimeEntryResult(
            timeEntryDao.getOne(id),
            stoppedTimeEntry
        )
    }

    override suspend fun stopRunningTimeEntry(): TimeEntry? {
        val now = timeService.now()
        return timeEntryDao
            .getAllRunning()
            .map { it.copy(duration = Duration.between(it.startTime, now)) }
            .also(timeEntryDao::updateAll)
            .firstOrNull()
    }

    override suspend fun editTimeEntry(timeEntry: TimeEntry): TimeEntry =
        timeEntryDao.update(timeEntry).run { timeEntry }

    override suspend fun deleteTimeEntries(timeEntries: List<TimeEntry>): HashSet<TimeEntry> =
        timeEntries
            .map { it.copy(isDeleted = true) }
            .apply(timeEntryDao::updateAll)
            .toHashSet()
}
