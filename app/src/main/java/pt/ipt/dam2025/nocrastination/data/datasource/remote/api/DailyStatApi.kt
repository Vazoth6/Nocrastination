package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.DailyStatRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.DailyStatListResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.DailyStatResponse
import retrofit2.Response
import retrofit2.http.*

interface DailyStatApi {
    @GET("api/daily-stats")
    suspend fun getDailyStats(
        @QueryMap filters: Map<String, String>
    ): Response<DailyStatListResponse>

    @POST("api/daily-stats")
    suspend fun createOrUpdateDailyStat(@Body request: DailyStatRequest): Response<DailyStatResponse>
}