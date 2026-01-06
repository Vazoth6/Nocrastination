package pt.ipt.dam2025.nocrastination.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TaskEventBus {
    private val _taskCompletedEvents = MutableSharedFlow<Int>()
    val taskCompletedEvents = _taskCompletedEvents.asSharedFlow()

    suspend fun emitTaskCompleted(taskId: Int) {
        _taskCompletedEvents.emit(taskId)
    }
}