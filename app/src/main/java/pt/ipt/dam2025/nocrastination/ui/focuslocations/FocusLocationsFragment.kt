package pt.ipt.dam2025.nocrastination.ui.focuslocations

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.FragmentFocusLocationsBinding
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.UIEvent
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.FocusLocationViewModel
import pt.ipt.dam2025.nocrastination.ui.focuslocations.adapter.FocusLocationAdapter
import java.util.Locale

class FocusLocationsFragment : Fragment() {

    private var _binding: FragmentFocusLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FocusLocationViewModel by viewModel()
    private lateinit var adapter: FocusLocationAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Verifica permissões antes de obter localização
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Log.d("FocusLocations", "Permissões concedidas")
            getCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissão de localização negada. Ative nas definições do dispositivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        Log.d("FocusLocations", "Fragment iniciado")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadFocusLocations()
    }

    private fun setupRecyclerView() {
        adapter = FocusLocationAdapter(
            onEditClick = { location ->
                Log.d("FocusLocations", "Editar localização: ${location.name}")
                showAddLocationDialog(location)
            },
            onDeleteClick = { location ->
                Log.d("FocusLocations", "Eliminar localização: ${location.name}")
                showDeleteConfirmation(location)
            },
            onToggleClick = { location, enabled ->
                Log.d("FocusLocations", "Toggle localização ${location.id}: $enabled")
                viewModel.toggleFocusLocation(location.id!!, enabled)
            }
        )

        binding.recyclerViewLocations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FocusLocationsFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observar tarefas
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.focusLocations.collectLatest { locations ->
                Log.d("FocusLocations", "Localizações atualizadas: ${locations.size} items")
                adapter.submitList(locations)
                binding.emptyState.root.visibility = if (locations.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewLocations.visibility = if (locations.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Observar loading
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observar erros
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("FocusLocations", "Erro: $errorMessage")
                    viewModel.clearError()
                }
            }
        }

        // Observar eventos UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is UIEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> { /* outros eventos */ }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddLocation.setOnClickListener {
            Log.d("FocusLocations", "Clicado em adicionar localização")
            checkLocationPermissions()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showAddLocationDialog(existingLocation: FocusLocation? = null) {
        val dialog = AddFocusLocationDialogFragment().apply {
            setLocation(existingLocation)
            setOnSaveListener { location ->
                if (existingLocation == null) {
                    viewModel.createFocusLocation(location)
                } else {
                    viewModel.updateFocusLocation(location)
                }
            }
        }
        dialog.show(parentFragmentManager, "AddFocusLocationDialog")
    }

    private fun showDeleteConfirmation(location: FocusLocation) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Localização")
            .setMessage("Tem a certeza que deseja eliminar '${location.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteFocusLocation(location.id!!)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun checkLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Para Android 10+ (API 29+), precisamos de background location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            getCurrentLocation()
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
            Log.w("FocusLocations", "Sem permissões para obter localização")
            Toast.makeText(requireContext(), "Permissões de localização necessárias", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("FocusLocations", "Obtendo localização atual...")

        // Verifica permissão antes de chamar lastLocation
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
                    Log.d("FocusLocations", "Localização obtida: ${location.latitude}, ${location.longitude}")

                    // Obter endereço a partir das coordenadas
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses: List<Address>? = try {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                    } catch (e: Exception) {
                        Log.e("FocusLocations", "Erro no Geocoder: ${e.message}")
                        null
                    }

                    val addressName = if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val sb = StringBuilder()
                        if (address.thoroughfare != null) sb.append(address.thoroughfare)
                        if (address.locality != null) {
                            if (sb.isNotEmpty()) sb.append(", ")
                            sb.append(address.locality)
                        }
                        sb.toString()
                    } else {
                        "Localização atual"
                    }

                    Log.d("FocusLocations", "Endereço: $addressName")

                    // Criar localização com nome padrão
                    val newLocation = FocusLocation(
                        name = addressName,
                        address = addressName,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        radius = 100f
                    )

                    showAddLocationDialog(newLocation)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Não foi possível obter a localização. Ative o GPS.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.w("FocusLocations", "Localização é nula - GPS pode estar desligado")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao obter localização: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FocusLocations", "Erro ao obter localização", e)
            }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("FocusLocations", "View destruída")
    }
}