package pt.ipt.dam2025.nocrastination.data.remote.api

import pt.ipt.dam2025.nocrastination.data.remote.models.requests.CreateTaskRequest
import pt.ipt.dam2025.nocrastination.data.remote.models.responses.ApiResponse
import pt.ipt.dam2025.nocrastination.data.remote.models.responses.TaskResponse
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {

    @GET("tasks")
    suspend fun getTasks(
        @Query("populate") populate: String = "user",
        @Query("sort") sort: String = "dueDate:asc",
        @Query("filters[status][\$ne]") excludeStatus: String? = null,
        @Query("pagination[page]") page: Int = 1,
        @Query("pagination[pageSize]") pageSize: Int = 50
    ): Response<ApiResponse<List<TaskResponse>>>

    @GET("tasks/{id}")
    suspend fun getTask(
        @Path("id") taskId: Int,
        @Query("populate") populate: String = "*"
    ): Response<ApiResponse<TaskResponse>>

    @POST("tasks")
    suspend fun createTask(
        @Body request: CreateTaskRequest
    ): Response<ApiResponse<TaskResponse>>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: Int,
        @Body request: CreateTaskRequest
    ): Response<ApiResponse<TaskResponse>>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") taskId: Int
    ): Response<Unit>

    // Custom endpoint if you added it to Strapi
    @POST("tasks/{id}/complete")
    suspend fun completeTask(
        @Path("id") taskId: Int
    ): Response<ApiResponse<TaskResponse>>
}