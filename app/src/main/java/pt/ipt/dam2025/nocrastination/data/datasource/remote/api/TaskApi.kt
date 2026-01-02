package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.*
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.TaskListResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.TaskResponse
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {

    @GET("api/task")
    suspend fun getTasks(): Response<TaskListResponse>

    @GET("api/task/{id}")
    suspend fun getTaskById(@Path("id") id: Int): Response<TaskResponse>

    @POST("api/task")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskResponse>

    @PUT("api/task/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body request: UpdateTaskRequest
    ): Response<TaskResponse>

    @PUT("api/task/{id}/complete")
    suspend fun completeTask(
        @Path("id") id: Int,
        @Body request: CompleteTaskRequest
    ): Response<TaskResponse>

    @DELETE("api/task/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}