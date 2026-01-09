package pt.ipt.dam2025.nocrastination.ui.focuslocations

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pt.ipt.dam2025.nocrastination.databinding.DialogAddFocusLocationBinding
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation

class AddFocusLocationDialogFragment : DialogFragment() {

    private var _binding: DialogAddFocusLocationBinding? = null
    private val binding get() = _binding!!

    private var existingLocation: FocusLocation? = null
    private var onSaveListener: ((FocusLocation) -> Unit)? = null

    fun setLocation(location: FocusLocation?) {
        this.existingLocation = location
    }

    fun setOnSaveListener(listener: (FocusLocation) -> Unit) {
        this.onSaveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(createDialogView())
            .setTitle(if (existingLocation == null) "Adicionar Zona de Foco" else "Editar Zona de Foco")
            .setPositiveButton("Guardar") { dialog, _ ->
                if (validateAndSave()) {
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun createDialogView(): View {
        _binding = DialogAddFocusLocationBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        existingLocation?.let { location ->
            binding.editLocationName.setText(location.name)
            binding.editLocationAddress.setText(location.address)
            binding.editLatitude.setText(location.latitude.toString())
            binding.editLongitude.setText(location.longitude.toString())
            binding.editRadius.setText(location.radius.toString())
            binding.editNotificationMessage.setText(location.notificationMessage)
        }
    }

    private fun validateAndSave(): Boolean {
        val name = binding.editLocationName.text.toString().trim()
        val address = binding.editLocationAddress.text.toString().trim()
        val latitudeStr = binding.editLatitude.text.toString().trim()
        val longitudeStr = binding.editLongitude.text.toString().trim()
        val radiusStr = binding.editRadius.text.toString().trim()
        val message = binding.editNotificationMessage.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nome é obrigatório", Toast.LENGTH_SHORT).show()
            return false
        }

        if (address.isEmpty()) {
            Toast.makeText(requireContext(), "Endereço é obrigatório", Toast.LENGTH_SHORT).show()
            return false
        }

        val latitude = try {
            latitudeStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Latitude inválida", Toast.LENGTH_SHORT).show()
            return false
        }

        val longitude = try {
            longitudeStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Longitude inválida", Toast.LENGTH_SHORT).show()
            return false
        }

        val radius = try {
            radiusStr.toFloat()
        } catch (e: NumberFormatException) {
            100f // valor padrão
        }

        val location = if (existingLocation != null) {
            existingLocation!!.copy(
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                notificationMessage = message.ifEmpty { "Vamos pôr as mãos ao trabalho!" }
            )
        } else {
            FocusLocation(
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                notificationMessage = message.ifEmpty { "Vamos pôr as mãos ao trabalho!" }
            )
        }

        onSaveListener?.invoke(location)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AddFocusLocationDialogFragment()
    }
}