package pt.ipt.dam2025.nocrastination.ui.focuslocations

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.DialogMapPickerBinding
import java.util.Locale

class MapPickerDialog : DialogFragment(), OnMapReadyCallback {

    private var _binding: DialogMapPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null

    interface OnLocationSelectedListener {
        fun onLocationSelected(latitude: Double, longitude: Double, address: String)
    }

    private var listener: OnLocationSelectedListener? = null

    fun setOnLocationSelectedListener(listener: OnLocationSelectedListener) {
        this.listener = listener
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Log.d("MapPickerDialog", "Permissões concedidas")
            enableMyLocation()
        } else {
            Log.w("MapPickerDialog", "Permissões negadas")
            binding.btnSelectLocation.isEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("MapPickerDialog", "Dialog criado")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Configurar botões
        binding.btnSelectLocation.setOnClickListener {
            selectedLatLng?.let { latLng ->
                getAddressFromLatLng(latLng) { address ->
                    listener?.onLocationSelected(
                        latLng.latitude,
                        latLng.longitude,
                        address
                    )
                    dismiss()
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // Inicializar mapa
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("MapPickerDialog", "Mapa pronto")
        setupMap()
        checkLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        try {
            // Configurar controles do mapa
            googleMap.uiSettings.apply {
                isZoomControlsEnabled = true
                isMyLocationButtonEnabled = true
                isCompassEnabled = true
                isRotateGesturesEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isZoomGesturesEnabled = true
            }

            // Listener para clique no mapa
            googleMap.setOnMapClickListener { latLng ->
                Log.d("MapPickerDialog", "Mapa clicado: $latLng")
                selectedLatLng = latLng

                // Limpar marcadores anteriores e adicionar novo
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Local selecionado")
                        .draggable(true)
                )

                // Habilitar botão de seleção
                binding.btnSelectLocation.isEnabled = true

                // Obter endereço para mostrar no botão
                getAddressFromLatLng(latLng) { address ->
                    binding.btnSelectLocation.text = "Selecionar: $address"
                }
            }

            // Listener para arrastar marcador
            googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: com.google.android.gms.maps.model.Marker) {}

                override fun onMarkerDrag(marker: com.google.android.gms.maps.model.Marker) {}

                override fun onMarkerDragEnd(marker: com.google.android.gms.maps.model.Marker) {
                    selectedLatLng = marker.position
                    getAddressFromLatLng(marker.position) { address ->
                        binding.btnSelectLocation.text = "Selecionar: $address"
                    }
                }
            })

        } catch (e: Exception) {
            Log.e("MapPickerDialog", "Erro ao configurar mapa: ${e.message}", e)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            enableMyLocation()
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
                Log.d("MapPickerDialog", "Localização habilitada no mapa")
            }
        } catch (e: Exception) {
            Log.e("MapPickerDialog", "Erro ao habilitar localização: ${e.message}")
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    Log.d("MapPickerDialog", "Localização atual: $latLng")

                    // Mover câmera para localização atual
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )

                    // Adicionar marcador na localização atual
                    selectedLatLng = latLng
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Sua localização atual")
                    )

                    // Habilitar botão de seleção
                    binding.btnSelectLocation.isEnabled = true

                    // Obter endereço
                    getAddressFromLatLng(latLng) { address ->
                        binding.btnSelectLocation.text = "Selecionar: $address"
                    }
                } ?: run {
                    Log.w("MapPickerDialog", "Localização nula - GPS pode estar desligado")
                    // Centralizar em Portugal por padrão
                    val portugal = LatLng(38.736946, -9.142685) // Lisboa
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(portugal, 10f)
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapPickerDialog", "Erro ao obter localização: ${e.message}", e)
                // Centralizar em Portugal por padrão em caso de erro
                val portugal = LatLng(38.736946, -9.142685) // Lisboa
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(portugal, 10f)
                )
            }
    }

    private fun getAddressFromLatLng(latLng: LatLng, callback: (String) -> Unit) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val sb = StringBuilder()

                // Construir endereço legível
                address.thoroughfare?.let { sb.append(it) }
                address.featureName?.let {
                    if (sb.isNotEmpty()) sb.append(", ")
                    sb.append(it)
                }
                address.locality?.let {
                    if (sb.isNotEmpty()) sb.append(", ")
                    sb.append(it)
                }

                val addressText = if (sb.isNotEmpty()) {
                    sb.toString()
                } else {
                    "Lat: ${latLng.latitude.format(6)}, Lng: ${latLng.longitude.format(6)}"
                }

                callback(addressText)
            } else {
                callback("Lat: ${latLng.latitude.format(6)}, Lng: ${latLng.longitude.format(6)}")
            }
        } catch (e: Exception) {
            Log.e("MapPickerDialog", "Erro no Geocoder: ${e.message}")
            callback("Lat: ${latLng.latitude.format(6)}, Lng: ${latLng.longitude.format(6)}")
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("MapPickerDialog", "View destruída")
    }
}