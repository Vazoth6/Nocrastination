package pt.ipt.dam2025.nocrastination.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Modelo de domínio que representa uma localização de foco (geofence).
 *
 * Esta classe define uma área geográfica onde o utilizador pretende trabalhar sem distrações,
 * ativando notificações quando entra ou sai da zona.
 */
@Parcelize
data class FocusLocation(
    // O ID é gerado pelo servidor Strapi
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