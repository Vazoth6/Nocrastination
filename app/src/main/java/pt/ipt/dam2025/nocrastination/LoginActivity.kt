package pt.ipt.dam2025.nocrastination

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import pt.ipt.dam2025.nocrastination.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Remover a action bar (opcional, para um visual mais limpo)
        supportActionBar?.hide()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Botão de Login
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }

        // Link para Registo
        binding.registerLinkTextView.setOnClickListener {
            navigateToRegister()
        }

        // Permitir login ao pressionar Enter no campo de password
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }
    }

    private fun attemptLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        if (validateLoginFields(email, password)) {
            // Simulação de login (substituir por chamada à API real)
            if (isValidCredentials(email, password)) {
                // Login bem-sucedido - navegar para MainActivity
                navigateToMain()
            } else {
                showError(getString(R.string.login_error))
            }
        }
    }

    private fun validateLoginFields(email: String, password: String): Boolean {
        var isValid = true

        // Validar email
        if (email.isEmpty()) {
            binding.emailInputLayout.error = getString(R.string.field_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }

        // Validar password
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = getString(R.string.field_required)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = getString(R.string.password_length)
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }

        return isValid
    }

    private fun isValidCredentials(email: String, password: String): Boolean {
        // TODO: Substituir por autenticação real com API
        // Por enquanto, aceita qualquer email/password não vazios para testes
        return email.isNotEmpty() && password.isNotEmpty() && password.length >= 6
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        // Adicionar animação de transição (opcional)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)

        // Passar dados do utilizador se necessário
        val email = binding.emailEditText.text.toString().trim()
        intent.putExtra("USER_EMAIL", email)

        // Limpar a stack de activities para não voltar ao login com back button
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish() // Terminar a LoginActivity para não voltar com back button
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Limpar campos ao voltar do registo (opcional)
        // binding.passwordEditText.text?.clear()
    }
}
