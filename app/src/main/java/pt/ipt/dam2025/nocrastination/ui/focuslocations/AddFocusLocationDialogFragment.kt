package pt.ipt.dam2025.nocrastination.ui.focuslocations

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.DialogAddFocusLocationBinding
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import java.util.Locale

class AddFocusLocationDialogFragment : DialogFragment() {

    private var _binding: DialogAddFocusLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var existingLocation: FocusLocation? = null // Null se for criação, não null se for edição

    private var onSaveListener: ((FocusLocation) -> Unit)? = null // Callback para salvar

    // API moderna para pedir permissões (Activity Results API)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() // Pede múltiplas permissões
    ) { permissions ->
        val granted = permissions.entries.all { it.value } // Verifica se todas foram concedidas
        if (granted) {
            Log.d("AddFocusLocation", "Permissões concedidas")
            getCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissão de localização negada. Ative nas definições do dispositivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Setter para modo edição
    fun setLocation(location: FocusLocation?) {
        existingLocation = location
    }

    // Setter para callback de salvamento
    fun setOnSaveListener(listener: (FocusLocation) -> Unit) {
        onSaveListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddFocusLocationBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("AddFocusLocation", "Dialog criado")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Preenche dados se for edição
        existingLocation?.let { location ->
            binding.editLocationName.setText(location.name)
            binding.editLocationAddress.setText(location.address)
            binding.editLatitude.setText(location.latitude.toString())
            binding.editLongitude.setText(location.longitude.toString())
            binding.editRadius.setText(location.radius.toString())
            binding.editNotificationMessage.setText(location.notificationMessage)

            binding.toolbar.title = "Editar Zona de Foco"
        } ?: run {
            binding.toolbar.title = "Nova Zona de Foco"
        }

        setupToolbar()
        setupButtons()
        setupMapButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss() // Fecha ao clicar na navegação
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    saveLocation()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        binding.buttonUseCurrentLocation.setOnClickListener {
            Log.d("AddFocusLocation", "Clicado em usar localização atual")
            checkLocationPermissions() // Verifica permissões antes de obter localização
        }
    }

    private fun setupMapButton() {
        binding.buttonSelectOnMap.setOnClickListener {
            Log.d("AddFocusLocation", "Clicado em selecionar no mapa")

            val mapDialog = MapPickerDialog() // Diálogo para selecionar no mapa

            // Implementa interface de callback
            mapDialog.setOnLocationSelectedListener(object : MapPickerDialog.OnLocationSelectedListener {
                override fun onLocationSelected(latitude: Double, longitude: Double, address: String) {
                    // Preenche os campos com os dados do mapa
                    binding.editLatitude.setText(latitude.toString())
                    binding.editLongitude.setText(longitude.toString())
                    binding.editLocationAddress.setText(address)

                    // Preenche o nome se estiver vazio
                    if (binding.editLocationName.text.isNullOrEmpty()) {
                        // Tenta extrair nome útil do endereço
                        val name = extractNameFromAddress(address)
                        binding.editLocationName.setText(name)
                    }
                }
            })

            mapDialog.show(parentFragmentManager, "MapPickerDialog")
        }
    }

    // Extrai nome curto do endereço completo
    private fun extractNameFromAddress(address: String): String {
        return when {
            address.contains(",") -> address.substringBefore(",").trim()
            address.length > 30 -> address.substring(0, 30).trim() + "..."
            else -> address
        }
    }

    private fun checkLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Otimização para Android 10+ (API 29+), precisa de background location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        // Filtra apenas permissões não concedidas
        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray()) // Request apenas as necessárias
        } else {
            getCurrentLocation() // Já tem permissões
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            Log.w("AddFocusLocation", "Sem permissões para obter localização")
            Toast.makeText(requireContext(), "Permissões de localização necessárias", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("AddFocusLocation", "A obter localização atual...")

        // Verificação duplicada por segurança (necessária pela API)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("AddFocusLocation", "Localização obtida: ${location.latitude}, ${location.longitude}")

                    // Obter endereço a partir das coordenadas (reverse geocoding)
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses: List<android.location.Address>? = try {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1 // Apenas o melhor resultado
                        )
                    } catch (e: Exception) {
                        Log.e("AddFocusLocation", "Erro no Geocoder: ${e.message}")
                        null
                    }

                    val addressName = if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val sb = StringBuilder()
                        if (address.thoroughfare != null) sb.append(address.thoroughfare) // Nome da rua
                        if (address.locality != null) {
                            if (sb.isNotEmpty()) sb.append(", ")
                            sb.append(address.locality)
                        }
                        sb.toString()
                    } else {
                        "Localização atual" // Fallback se Geocoder falhar
                    }

                    Log.d("AddFocusLocation", "Endereço: $addressName")

                    // Preencher campos
                    binding.editLatitude.setText(location.latitude.toString())
                    binding.editLongitude.setText(location.longitude.toString())
                    binding.editLocationAddress.setText(addressName)

                    // Preencher nome se estiver vazio
                    if (binding.editLocationName.text.isNullOrEmpty()) {
                        binding.editLocationName.setText(addressName)
                    }

                    Toast.makeText(
                        requireContext(),
                        "Localização atual obtida!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Não foi possível obter a localização. Ative o GPS.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.w("AddFocusLocation", "Localização é nula - GPS pode estar desligado")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao obter localização: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("AddFocusLocation", "Erro ao obter localização", e)
            }
    }

    private fun saveLocation() {
        val name = binding.editLocationName.text.toString().trim()
        val address = binding.editLocationAddress.text.toString().trim()
        val latitudeStr = binding.editLatitude.text.toString().trim()
        val longitudeStr = binding.editLongitude.text.toString().trim()
        val radiusStr = binding.editRadius.text.toString().trim()
        val notificationMessage = binding.editNotificationMessage.text.toString().trim()

        // Validação passo-a-passo
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nome é obrigatório", Toast.LENGTH_SHORT).show()
            return
        }

        if (address.isEmpty()) {
            Toast.makeText(requireContext(), "Endereço é obrigatório", Toast.LENGTH_SHORT).show()
            return
        }

        val latitude = try {
            latitudeStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Latitude inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val longitude = try {
            longitudeStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Longitude inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val radius = try {
            radiusStr.toFloat()
        } catch (e: NumberFormatException) {
            100f // Valor padrão se conversão falhar
        }

        // Cria objeto FocusLocation
        val location = FocusLocation(
            id = existingLocation?.id, // Mantém ID se for edição
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            enabled = existingLocation?.enabled ?: true, // Mantém estado ou ativa por padrão
            notificationMessage = if (notificationMessage.isNotEmpty()) {
                notificationMessage
            } else {
                "Vamos pôr as mãos ao trabalho!" // Mensagem padrão
            },
            createdAt = existingLocation?.createdAt, // Mantém timestamps originais
            updatedAt = existingLocation?.updatedAt
        )

        onSaveListener?.invoke(location) // Chama callback com a localização
        dismiss()
    }

    // Metodo para pré-preenchimento programático
    fun prefillFields(name: String? = null, address: String? = null, latitude: Double? = null, longitude: Double? = null, radius: Float? = null, notificationMessage: String? = null) {
        Log.d("AddFocusLocation", " Pré-preenchendo campos:")
        Log.d("AddFocusLocation", "  Nome: $name")
        Log.d("AddFocusLocation", "  Latitude: $latitude, Longitude: $longitude")

        // Executa após a view ser criada (evita NullPointer)
        view?.post {
            try {
                name?.let { binding.editLocationName.setText(it) }
                address?.let { binding.editLocationAddress.setText(it) }
                latitude?.let { binding.editLatitude.setText(it.toString()) }
                longitude?.let { binding.editLongitude.setText(it.toString()) }
                radius?.let { binding.editRadius.setText(it.toString()) }
                notificationMessage?.let { binding.editNotificationMessage.setText(it) }

                Log.d("AddFocusLocation", " Campos pré-preenchidos com sucesso")
            } catch (e: Exception) {
                Log.e("AddFocusLocation", " Erro ao pré-preecher campos: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpa binding
        Log.d("AddFocusLocation", "View destruída")
    }
}