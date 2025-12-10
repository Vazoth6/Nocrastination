package pt.ipt.dam2025.nocrastination

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Receber email do login
        val email = intent.getStringExtra("USER_EMAIL")

        // Mostrar email
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = "Bem-vindo, $email!"

        Toast.makeText(this, "Entrou na Main Activity", Toast.LENGTH_SHORT).show()
    }
}


/*import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2025.nocrastination.databinding.ActivityMainBinding

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
}*/