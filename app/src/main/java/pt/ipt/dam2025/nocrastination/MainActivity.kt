package pt.ipt.dam2025.nocrastination

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipleiria.nocrastination.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mostrar título com nome do utilizador se disponível
        val userName = intent.getStringExtra("USER_NAME") ?: "Utilizador"
        supportActionBar?.title = "Bem-vindo, $userName"

        // Mostrar email recebido do login (apenas para debug)
        val userEmail = intent.getStringExtra("USER_EMAIL")
        if (userEmail != null) {
            Toast.makeText(this, "Login efetuado com: $userEmail", Toast.LENGTH_SHORT).show()
        }

        // TODO: Aqui irás implementar a interface principal da app
        // com as funcionalidades anti-procrastinação
    }
}


/*import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pt.ipt.dam2025.nocrastination.ui.theme.NocrastinationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NocrastinationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NocrastinationTheme {
        Greeting("Android")
    }
}*/