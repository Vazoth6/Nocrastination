package pt.ipt.dam2025.nocrastination.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.android.ext.android.inject
import pt.ipt.dam2025.nocrastination.MainActivity
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.data.datasource.remote.ApiClient
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import pt.ipt.dam2025.nocrastination.utils.Resource
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    // Injeção de dependências com Koin
    private val authViewModel: AuthViewModel by inject()
    private val preferenceManager: PreferenceManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificação rápida antes de qualquer tarefa
        // Acesso direto ao SharedPreferences para verificar token
        val prefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)

        Log.d("LoginActivity", "Token direto do SharedPreferences: $token")

        if (!token.isNullOrEmpty()) {
            Log.d("LoginActivity", "Token encontrado, navegando para MainActivity")
            navigateToMain()
            return // Sai da função, não mostra ecrã de login
        }

        // Só se não houver token, continuar com o login
        setContentView(R.layout.activity_login)

        ApiClient.initialize(this)

        testConnection()

        // Inicializa vistas
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLinkTextView = findViewById<TextView>(R.id.registerLinkTextView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Observador para vários estados no Login
        authViewModel.loginState.observe(this, Observer { state ->
            Log.d("LoginActivity", "Observer chamado com estado: $state")
            when (state) {
                is Resource.Loading -> {
                    loginButton.isEnabled = false
                    progressBar.isVisible = true
                    Log.d("LoginActivity", " A carregar...")
                }
                is Resource.Success -> {
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                    Log.d("LoginActivity", " Login bem-sucedido!")
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is Resource.Error -> {
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                    val errorMessage = state.message ?: "Erro desconhecido"
                    Log.e("LoginActivity", " Erro no login: $errorMessage")
                    Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    loginButton.isEnabled = true
                    progressBar.isVisible = false
                }
            }
        })

        // Configura o botão de Login
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

        // Configura o link de Registo
        registerLinkTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        Log.d("LoginActivity", "✅ Activity inicializada corretamente")
    }

    /**
     * Verifica se o utilizador já está autenticado
     * @return Boolean true se existir token
     */
    private fun isUserLoggedIn(): Boolean {
        // Verifique diretamente nas preferências
        return preferenceManager.getAuthToken() != null
    }

    /**
     * Navega para a MainActivity (ecrã principal)
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // IMPORTANTE: finalizar a LoginActivity
    }

    /**
     * Valida formato de email com regex simples
     * @param email Email a validar
     * @return Boolean true se email válido
     */
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    /**
     * Testa a conexão com o servidor Strapi
     * Apenas para debugging, pode ser removido em produção
     */
    private fun testConnection() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .hostnameVerifier { _, _ -> true } // Bypass SSL em desenvolvimento
                    .build()

                val request = Request.Builder()
                    .url("http://10.0.2.2:1337") // Endereço local do emulador
                    .build()

                val response = client.newCall(request).execute()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("ConnectionTest", " Strapi responde: ${response.code}")
                    } else {
                        Log.w("ConnectionTest", " Strapi respondeu: ${response.code}")
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("ConnectionTest", "❌ Erro: ${e.message}", e)
                }
            }
        }
    }
}