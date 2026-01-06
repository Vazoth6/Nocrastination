package pt.ipt.dam2025.nocrastination.domain.models

sealed class UIEvent {
    data class ShowToast(val message: String) : UIEvent()
    data class ShowSnackbar(val message: String) : UIEvent()
    object NavigateBack : UIEvent()
}