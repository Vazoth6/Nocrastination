package pt.ipt.dam2025.nocrastination.domain.models

/**
 * Classe selada que representa eventos de interface do utilizador.
 *
 * Este padrão é usado em arquiteturas como MVI/MVVM para comunicar eventos
 * da camada de domínio/presentação para a camada de visualização.
 **/
sealed class UIEvent {
    data class ShowToast(val message: String) : UIEvent()
    data class ShowSnackbar(val message: String) : UIEvent()
    object NavigateBack : UIEvent()
}