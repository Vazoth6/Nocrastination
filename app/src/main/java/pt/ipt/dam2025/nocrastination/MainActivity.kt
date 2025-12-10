package pt.ipt.dam2025.nocrastination

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import pt.ipt.dam2025.nocrastination.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Configurar Navigation Component
        navController = findNavController(R.id.nav_host_fragment)

        // Configurar Bottom Navigation
        binding.bottomNavigation.setupWithNavController(navController)

        // Configurar AppBar com os destinos de top-level
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.tasksFragment,
                R.id.pomodoroFragment,
                R.id.statisticsFragment,
                R.id.profileFragment
            )
        ).also {

            setupActionBarWithNavController(navController, it)
        }

        // Opcional: Mudar título da ActionBar baseado no fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.tasksFragment -> supportActionBar?.title = "Tarefas"
                R.id.pomodoroFragment -> supportActionBar?.title = "Pomodoro"
                R.id.statisticsFragment -> supportActionBar?.title = "Estatísticas"
                R.id.profileFragment -> supportActionBar?.title = "Perfil"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}