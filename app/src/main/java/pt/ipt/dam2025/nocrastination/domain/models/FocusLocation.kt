package pt.ipt.dam2025.nocrastination.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FocusLocation(
    val id: Int? = null,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f,
    val enabled: Boolean = true,
    val notificationMessage: String = "Vamos pôr as mãos ao trabalho!",
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable