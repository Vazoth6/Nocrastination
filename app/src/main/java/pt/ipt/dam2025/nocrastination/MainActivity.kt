package pt.ipt.dam2025.nocrastination

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import pt.ipt.dam2025.nocrastination.databinding.ActivityMainBinding

// Atividade principal da aplicação
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Definir a Toolbar como ActionBar
        setSupportActionBar(binding.toolbar)

        setupNavigation()
    }

    // Configurar sistema de navegação
    private fun setupNavigation() {
        try {
            // Obter NavHostFragment
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

            // Obter NavController
            navController = navHostFragment.navController

            // Configurar navegação inferior
            binding.bottomNavigation.setupWithNavController(navController)

            // Prevenir criação duplicada de fragmentos ao reselecionar
            binding.bottomNavigation.setOnItemReselectedListener {
                // Não fazer nada ao reselecionar
            }

            // Configurar AppBar com os destinos de topo
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.tasksFragment,
                    R.id.pomodoroFragment,
                    R.id.statisticsFragment,
                    R.id.profileFragment
                )
            )

            // Configurar ActionBar com NavController
            setupActionBarWithNavController(navController, appBarConfiguration)

            // Atualizar título baseado no fragmento atual
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.tasksFragment -> {
                        supportActionBar?.title = "Tarefas"
                        // Mostrar botão de zonas de foco apenas no ecrã de tarefas
                        binding.toolbar.menu.findItem(R.id.action_focus_locations)?.isVisible = true
                    }
                    R.id.focusLocationsFragment -> {
                        supportActionBar?.title = "Zonas de Foco"
                        // Ocultar botão quando estiver neste ecrã
                        binding.toolbar.menu.findItem(R.id.action_focus_locations)?.isVisible = false
                    }
                    R.id.pomodoroFragment -> {
                        supportActionBar?.title = "Pomodoro"

                        binding.toolbar.menu.findItem(R.id.action_focus_locations)?.isVisible = false
                    }
                    R.id.statisticsFragment -> {
                        supportActionBar?.title = "Estatísticas"
                        binding.toolbar.menu.findItem(R.id.action_focus_locations)?.isVisible = false
                    }
                    R.id.profileFragment -> {
                        supportActionBar?.title = "Perfil"
                        binding.toolbar.menu.findItem(R.id.action_focus_locations)?.isVisible = false
                    }
                }
            }

            Log.d("MainActivity", "Navigation setup com sucesso")

        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation setup falhou: ${e.message}", e)
        }
    }

    // Suportar navegação para cima (Up navigation)
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}