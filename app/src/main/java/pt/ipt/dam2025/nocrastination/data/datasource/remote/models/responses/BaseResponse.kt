package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import pt.ipt.dam2025.nocrastination.data.remote.models.responses.Pagination

data class StrapiListResponse<T>(
    val data: List<StrapiData<T>>,
    val meta: StrapiMeta
)

data class StrapiResponse<T>(
    val data: StrapiData<T>
)

data class StrapiData<T>(
    val id: Int,
    val attributes: T
)

data class StrapiMeta(
    val pagination: Pagination? = null
)