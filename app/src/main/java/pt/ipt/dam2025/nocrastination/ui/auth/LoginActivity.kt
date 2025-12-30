// LoginActivity.kt
package pt.ipt.dam2025.nocrastination.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.MainActivity
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.utils.Resource
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        testConnection()

        // Check if user is already logged in
        if (authViewModel.isLoggedIn()) {
            navigateToMain()
            return
        }

        // Initialize views
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLinkTextView = findViewById<TextView>(R.id.registerLinkTextView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Setup login state observer
        authViewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is Resource.Loading -> {
                    loginButton.isEnabled = false
                    progressBar.isVisible = true
                }
                is Resource.Success -> {
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is Resource.Error -> {
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                    val errorMessage = state.message ?: "Erro desconhecido"
                    Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                }
            }
        })

        // Configure login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

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
                    emailEditText.error = null
                    passwordEditText.error = null
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

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun testConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Teste direto com OkHttp (sem Retrofit)
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("http://10.0.2.2:1337")
                    .build()

                val response = client.newCall(request).execute()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@LoginActivity,
                            "✅ Conexão OK! Status: ${response.code}",
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoginActivity,
                            "⚠️ Strapi respondeu: ${response.code}",
                            Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity,
                        "❌ Erro: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    Log.e("ConnectionTest", "Erro: ${e.message}", e)
                }
            }
        }
    }
}