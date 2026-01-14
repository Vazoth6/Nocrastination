package pt.ipt.dam2025.nocrastination.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Objeto singleton que funciona como um barramento de eventos para tarefas
object TaskEventBus {
    // Fluxo mutável privado para emissão de eventos
    private val _taskCompletedEvents = MutableSharedFlow<Int>()
    // Fluxo público apenas para leitura
    val taskCompletedEvents = _taskCompletedEvents.asSharedFlow()

    // Função suspensa para emitir eventos de tarefa concluída
    suspend fun emitTaskCompleted(taskId: Int) {
        _taskCompletedEvents.emit(taskId)
    }
}