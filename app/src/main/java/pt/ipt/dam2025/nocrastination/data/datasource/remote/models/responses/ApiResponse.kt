package pt.ipt.dam2025.nocrastination.data.remote.models.responses

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ApiError? = null,

    @SerializedName("meta")
    val meta: MetaData? = null
) {
    val isSuccess: Boolean get() = error == null
}

data class ApiError(
    @SerializedName("status")
    val status: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("details")
    val details: Map<String, Any>? = null
)

data class MetaData(
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("page")
    val page: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("pageCount")
    val pageCount: Int,

    @SerializedName("total")
    val total: Int
)
