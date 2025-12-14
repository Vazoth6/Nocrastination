package pt.ipt.dam2025.nocrastination

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepository
import pt.ipt.dam2025.nocrastination.presentation.viewmodels.AuthViewModel
import pt.ipt.dam2025.nocrastination.presentations.viewmodels.AuthViewModelFactory
import pt.ipt.dam2025.nocrastination.utils.Resource

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val authRepository = AuthRepository(applicationContext)
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]


        // Initialize views
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLinkTextView = findViewById<TextView>(R.id.registerLinkTextView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Setup login state observer using LiveData.observe()
        authViewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is Resource.Loading -> {
                    // Show loading state
                    loginButton.isEnabled = false
                    progressBar.isVisible = true
                }

                is Resource.Success -> {
                    // Hide loading state
                    loginButton.isEnabled = true
                    progressBar.isVisible = false

                    // Login successful
                    Toast.makeText(
                        this@LoginActivity,
                        "Login bem-sucedido!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("USER_EMAIL", emailEditText.text.toString())
                    startActivity(intent)
                    finish() // Close LoginActivity
                }

                is Resource.Error -> {
                    // Hide loading state
                    loginButton.isEnabled = true
                    progressBar.isVisible = false

                    // Show error message
                    val errorMessage = state.message ?: "Erro desconhecido"
                    Toast.makeText(
                        this@LoginActivity,
                        "Erro: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    // For any other state (initial state)
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                }
            }
        })

        // Configure login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            // Validation
            when {
                email.isEmpty() -> {
                    emailEditText.error = "Por favor, insira o email"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    passwordEditText.error = "Por favor, insira a senha"
                    return@setOnClickListener
                }
                !isValidEmail(email) -> {
                    emailEditText.error = "Por favor, insira um email válido"
                    return@setOnClickListener
                }
                else -> {
                    // Clear errors
                    emailEditText.error = null
                    passwordEditText.error = null

                    // Call ViewModel to handle login
                    authViewModel.login(email, password)
                }
            }
        }

        // Configure registration link
        registerLinkTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}