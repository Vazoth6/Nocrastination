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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the Toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        setupNavigation()
    }

    private fun setupNavigation() {
        try {
            // Get NavHostFragment
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

            // Get NavController
            navController = navHostFragment.navController

            // Set up bottom navigation
            binding.bottomNavigation.setupWithNavController(navController)

            // Prevent duplicate fragment creation on reselect
            binding.bottomNavigation.setOnItemReselectedListener {
                // Do nothing on reselect to prevent recreating the same fragment
            }

            // Configure AppBar with the fragments you want as top-level destinations
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.tasksFragment,
                    R.id.pomodoroFragment,
                    R.id.statisticsFragment,
                    R.id.profileFragment
                )
            )

            // Setup ActionBar with NavController
            setupActionBarWithNavController(navController, appBarConfiguration)

            // Update title based on fragment
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.tasksFragment -> supportActionBar?.title = "Tarefas"
                    R.id.pomodoroFragment -> supportActionBar?.title = "Pomodoro"
                    R.id.statisticsFragment -> supportActionBar?.title = "Estatísticas"
                    R.id.profileFragment -> supportActionBar?.title = "Perfil"
                }
            }

            Log.d("MainActivity", "Navigation setup successful")

        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation setup failed: ${e.message}", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}