package pt.ipt.dam2025.nocrastination.ui.focuslocations

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.BuildConfig
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.FragmentFocusLocationsBinding
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.UIEvent
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.FocusLocationViewModel
import pt.ipt.dam2025.nocrastination.ui.focuslocations.adapter.FocusLocationAdapter
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt

class FocusLocationsFragment : Fragment() {

    private var _binding: FragmentFocusLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FocusLocationViewModel by viewModel() // Injeção do ViewModel com Koin
    private lateinit var adapter: FocusLocationAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient // Cliente de localização do Google

    // Registo para pedir permissões usando a API moderna de Activity Results
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() // Pede múltiplas permissões
    ) { permissions ->
        val granted = permissions.entries.all { it.value } // Verifica se todas foram concedidas
        if (granted) {
            Log.d("FocusLocations", "Permissões concedidas")
            getCurrentLocation() // Obtém localização após permissão concedida
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

        // Configurar botão "voltar" na ActionBar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        Log.d("FocusLocations", "Fragment iniciado")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        if (BuildConfig.DEBUG) {
            addTestButtons() // Adiciona botões de teste apenas em modo debug
        }

        viewModel.loadFocusLocations() // Carrega localizações ao iniciar
    }

    private fun addTestButtons() {
        // Botão de teste GPS (apenas visível em desenvolvimento)
        val testGPSButton = MaterialButton(requireContext()).apply {
            text = "🔧 Testar GPS"
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            cornerRadius = 20.dpToPx() // Conversão de dp para pixels

            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            elevation = 8f // Sombra para efeito de elevação

            setOnClickListener {
                Log.d("FocusLocations", "Botão de teste GPS clicado")
                testGPSFunctionality() // Executa testes de GPS
            }
        }

        // Adiciona botão ao layout principal
        (binding.root as? ViewGroup)?.addView(testGPSButton)

        // Posiciona manualmente no canto superior direito após medida
        testGPSButton.post {
            val parentWidth = (testGPSButton.parent as? ViewGroup)?.width ?: 0
            val buttonWidth = testGPSButton.measuredWidth

            // Posicionar no canto superior direito com margem de 16dp
            testGPSButton.translationX = (parentWidth - buttonWidth - 16.dpToPx()).toFloat()
            testGPSButton.translationY = 16.dpToPx().toFloat()
        }
    }

    // Função de extensão para converter dp para pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun testGPSFunctionality() {
        Log.d("FocusLocationsTest", "=== INICIANDO TESTE COMPLETO DE GPS ===")

        // Resultado acumulado dos testes
        val testResults = mutableListOf<String>()

        // Testa permissões
        testPermissions(testResults)

        // Testa provedores de localização
        testLocationProviders(testResults)

        // Testa serviços da Google Play
        testGooglePlayServices(testResults)

        // Testa Geotools (Geocoder)
        testGeocoder(testResults)

        // Esta a obtenção da localização
        testLocationAcquisition(testResults)

        // Testa Geofencing
        testGeofencing(testResults)

        // Testa conexão com a API
        testAPIConnection(testResults)

        // Exibir resultados completos
        displayTestResults(testResults)

        Log.d("FocusLocationsTest", "=== FIM DO TESTE COMPLETO DE GPS ===")
    }

    private fun testPermissions(results: MutableList<String>) {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Não necessário em versões anteriores ao Android 10
        }

        results.add(" PERMISSÕES:")
        results.add(" FINE_LOCATION: ${if (hasFineLocation) "CONCEDIDA" else "NEGADA"}")
        results.add(" COARSE_LOCATION: ${if (hasCoarseLocation) "CONCEDIDA" else "NEGADA"}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            results.add(" BACKGROUND_LOCATION: ${if (hasBackgroundLocation) "CONCEDIDA" else "NEGADA"}")
        }
    }

    private fun testLocationProviders(results: MutableList<String>) {
        val locationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val passiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)

        results.add("\n PROVEDORES DE LOCALIZAÇÃO:")
        results.add(" GPS: ${if (gpsEnabled) "ATIVO" else "INATIVO"}")
        results.add(" NETWORK: ${if (networkEnabled) "ATIVO" else "INATIVO"}")
        results.add(" PASSIVE: ${if (passiveEnabled) "ATIVO" else "INATIVO"}")

        // Lista todos os provedores disponíveis no sistema
        val allProviders = locationManager.allProviders
        results.add(" Todos os provedores: ${allProviders.joinToString(", ")}")
    }

    private fun testGooglePlayServices(results: MutableList<String>) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        val isAvailable = resultCode == ConnectionResult.SUCCESS
        val version = try {
            requireContext().packageManager.getPackageInfo("com.google.android.gms", 0).versionName
        } catch (e: Exception) {
            "Desconhecida"
        }

        results.add("\n GOOGLE PLAY SERVICES:")
        results.add(" Versão: $version")
        results.add(" Status: ${if (isAvailable) "DISPONÍVEL" else "INDISPONÍVEL (Código: $resultCode)"}")

        if (!isAvailable) {
            val errorString = googleApiAvailability.getErrorString(resultCode)
            results.add("❌ Erro: $errorString")
        }
    }

    private fun testGeocoder(results: MutableList<String>) {
        val isGeocoderPresent = android.location.Geocoder.isPresent()
        results.add("\n GEOCODER:")
        results.add(" Disponível: $isGeocoderPresent")

        if (isGeocoderPresent) {
            // Testa geocoding reverso com localização conhecida (Lisboa)
            val geocoder = android.location.Geocoder(requireContext(), Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(38.736946, -9.142685, 1)
                results.add(" Geocoding reverso: ${if (!addresses.isNullOrEmpty()) "FUNCIONA" else "FALHA"}")

                if (!addresses.isNullOrEmpty()) {
                    results.add(" Endereço teste: ${addresses[0].getAddressLine(0)?.take(50)}...")
                }
            } catch (e: IOException) {
                results.add(" Erro no Geocoder: ${e.message}")
            }
        }
    }

    private fun testLocationAcquisition(results: MutableList<String>) {
        if (!hasLocationPermission()) {
            results.add("\n OBTENÇÃO DE LOCALIZAÇÃO:")
            results.add(" Não testado - Sem permissões")
            return
        }

        results.add("\n OBTENÇÃO DE LOCALIZAÇÃO:")

        // Verificação dupla de permissão por segurança
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            results.add(" Permissões insuficientes")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    results.add(" Última localização obtida:")
                    results.add("   Latitude: ${location.latitude}")
                    results.add("   Longitude: ${location.longitude}")
                    results.add("   Precisão: ${location.accuracy?.roundToInt()} metros")
                    results.add("   Provedor: ${location.provider}")
                    results.add("   Velocidade: ${location.speed ?: 0.0} m/s")

                    // Testar atualização em tempo real
                    testRealTimeLocation(results)
                } else {
                    results.add(" Última localização: NULA")
                    results.add("   Possível causa: GPS desativado ou primeiro uso")

                    // Solicitar atualização única se a última localização for nula
                    val locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(5000)
                        .setNumUpdates(1) // Apenas uma atualização

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                locationResult.lastLocation?.let { updatedLocation ->
                                    results.add(" Localização atualizada obtida!")
                                    results.add(" Lat: ${updatedLocation.latitude}, Lon: ${updatedLocation.longitude}")
                                }
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        },
                        Looper.getMainLooper()
                    )
                }
            }
            .addOnFailureListener { e ->
                results.add(" Falha ao obter localização: ${e.message}")
            }
    }

    private fun testRealTimeLocation(results: MutableList<String>) {
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

        // Solicitar algumas atualizações para teste de localização em tempo real
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(3000) // Intervalo de 3 segundos
            .setFastestInterval(1000) // Intervalo mais rápido de 1 segundo
            .setNumUpdates(3) // Apenas 3 atualizações para teste

        val updates = mutableListOf<String>()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val loc = locationResult.lastLocation
                    updates.add(" Update ${updates.size + 1}: Lat=${loc?.latitude}, Lon=${loc?.longitude}")

                    if (updates.size >= 3) {
                        results.add(" Localização em tempo real:")
                        updates.forEach { update -> results.add("   $update") }
                        fusedLocationClient.removeLocationUpdates(this) // Para updates após teste
                    }
                }
            },
            Looper.getMainLooper() // Executa no thread principal
        )
    }

    private fun testGeofencing(results: MutableList<String>) {
        results.add("\n GEOFENCING:")

        // Verificar se há localizações configuradas no ViewModel
        val currentLocations = viewModel.focusLocations.value
        results.add(" Zonas de foco configuradas: ${currentLocations.size}")

        if (currentLocations.isNotEmpty()) {
            val enabledCount = currentLocations.count { it.enabled }
            results.add(" Zonas ativas: $enabledCount")
            results.add(" Zonas inativas: ${currentLocations.size - enabledCount}")

            // Mostrar algumas zonas como exemplo
            currentLocations.take(2).forEach { location ->
                results.add(" ${location.name}: Lat=${location.latitude}, Lon=${location.longitude}, Raio=${location.radius}m")
            }

            if (currentLocations.size > 2) {
                results.add("   ... e mais ${currentLocations.size - 2} zonas")
            }
        } else {
            results.add("ℹ Nenhuma zona de foco configurada")
        }
    }

    private fun testAPIConnection(results: MutableList<String>) {
        results.add("\n CONEXÃO COM API:")
        results.add(" Testando conexão com API...")

        // Verificar conectividade de rede
        val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected
        val connectionType = when (networkInfo?.type) {
            android.net.ConnectivityManager.TYPE_WIFI -> "WiFi"
            android.net.ConnectivityManager.TYPE_MOBILE -> "Dados móveis"
            else -> "Desconhecido"
        }

        results.add(" Conexão: ${if (isConnected) "ATIVA ($connectionType)" else "INATIVA"}")

        if (isConnected) {
            results.add(" Conectado à internet")
        } else {
            results.add(" Sem conexão à internet")
        }
    }

    private fun displayTestResults(results: List<String>) {
        val fullReport = results.joinToString("\n")

        Log.d("FocusLocationsTest", "Relatório completo:\n$fullReport")

        // Mostrar em um diálogo mais organizado
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(" Relatório de Teste GPS")
            .setMessage(fullReport)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Copiar") { dialog, _ ->
                // Copiar relatório para clipboard
                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Teste GPS", fullReport)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Relatório copiado!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Testar Novamente") { dialog, _ ->
                dialog.dismiss()
                testGPSFunctionality() // Reiniciar teste
            }
            .show()

        // Mostrar Toast com resumo rápido
        val summary = """
             Resumo Teste GPS:
             Localizações: ${viewModel.focusLocations.value.size}
             GPS: ${if (isGPSEnabled()) "ON" else "OFF"}
             API: ${if (isInternetConnected()) "CONECTADO" else "DESCONECTADO"}
        """.trimIndent()

        Toast.makeText(requireContext(), summary, Toast.LENGTH_LONG).show()
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun setupRecyclerView() {
        // Configurar adapter com callbacks para ações
        adapter = FocusLocationAdapter(
            onEditClick = { location ->
                Log.d("FocusLocations", "Editar localização: ${location.name}")
                showAddLocationDialog(location) // Abrir diálogo em modo edição
            },
            onDeleteClick = { location ->
                Log.d("FocusLocations", "Eliminar localização: ${location.name}")
                showDeleteConfirmation(location) // Pedir confirmação antes de eliminar
            },
            onToggleClick = { location, enabled ->
                Log.d("FocusLocations", "Toggle localização ${location.id}: $enabled")
                viewModel.toggleFocusLocation(location.id!!, enabled) // Ativar/desativar
            }
        )

        binding.recyclerViewLocations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FocusLocationsFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observar lista de localizações
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.focusLocations.collectLatest { locations ->
                Log.d("FocusLocations", "Localizações atualizadas: ${locations.size} items")
                adapter.submitList(locations)
                binding.emptyState.root.visibility = if (locations.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewLocations.visibility = if (locations.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Observar estado de loading
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
                    viewModel.clearError() // Limpar erro após mostrar
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
        // Floating Action Button para adicionar nova localização
        binding.fabAddLocation.setOnClickListener {
            Log.d("FocusLocations", "✅ Clicado em adicionar localização")
            // Mostrar diálogo vazio para NOVA localização
            showAddLocationDialog(null)
        }

        // Botão de navegação na toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Voltar para ecrã anterior
        }
    }

    private fun showAddLocationDialog(existingLocation: FocusLocation? = null) {
        Log.d("FocusLocations", " Mostrando diálogo para: ${if (existingLocation == null) "NOVA localização" else "EDIÇÃO da localização ID: ${existingLocation.id}"}")

        val dialog = AddFocusLocationDialogFragment().apply {
            setLocation(existingLocation) // Passar localização existente ou null
            setOnSaveListener { savedLocation ->
                Log.d("FocusLocations", " Recebida localização do diálogo:")
                Log.d("FocusLocations", "  ID: ${savedLocation.id}")
                Log.d("FocusLocations", "  Nome: ${savedLocation.name}")

                // Cria ou atualiza consoante o estado do existingLocation
                if (existingLocation == null) {
                    // existingLocation é null = Nova localização
                    Log.d("FocusLocations", " É uma nova localização, a chamar CREATE")
                    viewModel.createFocusLocation(savedLocation)
                } else {
                    // existingLocation não é null = Edição
                    Log.d("FocusLocations", " É uma edição, a chamar UPDATE para ID: ${existingLocation.id}")
                    viewModel.updateFocusLocation(savedLocation)
                }
            }
        }

        dialog.show(parentFragmentManager, "AddFocusLocationDialog")
    }

    private fun showAddLocationDialogWithPrefilledData(address: String, latitude: Double, longitude: Double) {
        Log.d("FocusLocations", " Mostrando diálogo com dados pré-preenchidos")

        val dialog = AddFocusLocationDialogFragment().apply {
            // Null porque indica que é uma nova localização
            setLocation(null)
            setOnSaveListener { savedLocation ->
                Log.d("FocusLocations", " Recebida localização do diálogo (com dados pré-preenchidos)")
                Log.d("FocusLocations", "  ID: ${savedLocation.id}")

                // existingLocation é null, então deve criar
                if (savedLocation.id == null) {
                    Log.d("FocusLocations", " A chamar CREATE para nova localização")
                    viewModel.createFocusLocation(savedLocation)
                } else {
                    Log.e("FocusLocations", " ERRO: ID não deveria existir em nova localização!")
                    viewModel.updateFocusLocation(savedLocation)
                }
            }
        }

        dialog.show(parentFragmentManager, "AddFocusLocationDialog")

        // Usar metodo prefillFields para pré-preencher campos
        dialog.prefillFields(
            name = address,
            address = address,
            latitude = latitude,
            longitude = longitude,
            radius = 100f,
            notificationMessage = "Vamos pôr as mãos ao trabalho!"
        )
    }

    private fun testDirectCreate() {
        Log.d("FocusLocations", " TESTE DIRETO: Criando localização sem diálogo")

        val testLocation = FocusLocation(
            id = null, // ← IMPORTANTE: null para nova localização
            name = "Localização de Teste Direto",
            address = "Rua Teste, Lisboa",
            latitude = 38.736946,
            longitude = -9.142685,
            radius = 100f,
            enabled = true,
            notificationMessage = "Teste direto"
        )

        Log.d("FocusLocations", " Chamando createFocusLocation no ViewModel")
        viewModel.createFocusLocation(testLocation)
    }

    private fun showDeleteConfirmation(location: FocusLocation) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Localização")
            .setMessage("Tem a certeza que deseja eliminar '${location.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteFocusLocation(location.id!!) // Eliminar após confirmação
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

        // Filtrar apenas permissões não concedidas
        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
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
            Log.w("FocusLocations", "Sem permissões para obter localização")
            Toast.makeText(requireContext(), "Permissões de localização necessárias", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("FocusLocations", "Obtendo localização atual...")

        // Verificação dupla de permissão (requisito da API)
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
                    Log.d("FocusLocations", "📍 Localização obtida: ${location.latitude}, ${location.longitude}")

                    // Obter endereço a partir das coordenadas (reverse geocoding)
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses: List<Address>? = try {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1 // Apenas o melhor resultado
                        )
                    } catch (e: Exception) {
                        Log.e("FocusLocations", "Erro no Geocoder: ${e.message}")
                        null
                    }

                    val addressName = if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val sb = StringBuilder()
                        if (address.thoroughfare != null) sb.append(address.thoroughfare) // Nome da rua
                        if (address.locality != null) { // Cidade
                            if (sb.isNotEmpty()) sb.append(", ")
                            sb.append(address.locality)
                        }
                        sb.toString()
                    } else {
                        "Localização atual" // Fallback se Geocoder falhar
                    }

                    Log.d("FocusLocations", " Endereço: $addressName")

                    // Mostrar diálogo com dados pré-preenchidos da localização atual
                    showAddLocationDialogWithPrefilledData(addressName, location.latitude, location.longitude)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Não foi possível obter a localização. Ative o GPS.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.w("FocusLocations", " Localização é nula - GPS pode estar desligado")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao obter localização: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FocusLocations", " Erro ao obter localização", e)
            }
    }

    private fun testDifferentFormats() {
        Log.d("FocusLocations", " A testar diferentes formatos da API")

        // Formato atual (simples)
        val testLocation1 = FocusLocation(
            id = null,
            name = "Teste Formato Simples",
            address = "Rua Teste, Lisboa",
            latitude = 38.736946,
            longitude = -9.142685,
            radius = 100f,
            enabled = true,
            notificationMessage = "Teste simples"
        )

        Log.d("FocusLocations", " A enviar formato simples...")
        viewModel.createFocusLocation(testLocation1)

        // Aguardar e testar outro formato
        view?.postDelayed({
            // Formato com notification_message em snake_case
            val testLocation2 = FocusLocation(
                id = null,
                name = "Teste Snake Case",
                address = "Rua Teste, Porto",
                latitude = 41.157944,
                longitude = -8.629105,
                radius = 150f,
                enabled = true,
                notificationMessage = "Teste snake_case"
            )

            Log.d("FocusLocations", " A enviar formato snake_case...")
            viewModel.createFocusLocation(testLocation2)
        }, 2000)
    }


    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpar binding para evitar derrame de memória
        Log.d("FocusLocations", "View destruída")
    }
}