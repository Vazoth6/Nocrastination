package pt.ipt.dam2025.nocrastination.data.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipt.dam2025.nocrastination.data.remote.ApiClient
import pt.ipt.dam2025.nocrastination.data.remote.models.requests.CreateTaskRequest
import pt.ipt.dam2025.nocrastination.data.remote.models.requests.TaskData
import pt.ipt.dam2025.nocrastination.data.remote.models.responses.TaskResponse
import java.lang.Exception

class TaskRepository(private val context: Context) {

    private val taskApi = ApiClient.getTaskApi(context)

    suspend fun getTasks(): Result<List<TaskResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = taskApi.getTasks()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        Result.success(apiResponse.data ?: emptyList())
                    } else {
                        Result.failure(Exception(apiResponse?.error?.message ?: "Failed to fetch tasks"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("API error: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createTask(
        title: String,
        description: String? = null,
        dueDate: String? = null,
        priority: String = "MEDIUM",
        category: String? = null
    ): Result<TaskResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateTaskRequest(
                    TaskData(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        category = category
                    )
                )

                val response = taskApi.createTask(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        Result.success(apiResponse.data!!)
                    } else {
                        Result.failure(Exception(apiResponse?.error?.message ?: "Failed to create task"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("API error: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeTask(taskId: Int): Result<TaskResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = taskApi.completeTask(taskId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        Result.success(apiResponse.data!!)
                    } else {
                        Result.failure(Exception(apiResponse?.error?.message ?: "Failed to complete task"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("API error: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}