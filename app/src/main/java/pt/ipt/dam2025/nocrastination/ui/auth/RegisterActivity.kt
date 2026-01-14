package pt.ipt.dam2025.nocrastination.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    // Injeção de ViewModel com Koin
    private val authViewModel: AuthViewModel by viewModel()

    // Constantes para política de chaves
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MIN_USERNAME_LENGTH = 3

        // Padrões regex para validação de password
        private val UPPERCASE_PATTERN = Pattern.compile("[A-Z]")
        private val LOWERCASE_PATTERN = Pattern.compile("[a-z]")
        private val DIGIT_PATTERN = Pattern.compile("[0-9]")
        private val SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")
    }

    // Setup register state observer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        // Setup register state observer
        authViewModel.registerState.observe(this) { state ->
            when (state) {
                is pt.ipt.dam2025.nocrastination.utils.Resource.Loading -> {
                    registerButton.isEnabled = false
                }
                is pt.ipt.dam2025.nocrastination.utils.Resource.Success -> {
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Registo bem-sucedido!", Toast.LENGTH_SHORT).show()
                    finish() // Volta para LoginActivity após registo bem-sucedido
                }
                is pt.ipt.dam2025.nocrastination.utils.Resource.Error -> {
                    registerButton.isEnabled = true
                    val errorMessage = state.message ?: "Erro desconhecido"
                    Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    registerButton.isEnabled = true
                }
            }
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Valida se todos os campos estão preenchidos
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Valida o tamnho do nome de utilizador
            if (name.length < MIN_USERNAME_LENGTH) {
                Toast.makeText(
                    this,
                    "O nome de utilizador deve ter pelo menos $MIN_USERNAME_LENGTH caracteres",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Valida email
            if (!isValidEmail(email)) {
                emailEditText.error = "Por favor, insira um email válido"
                return@setOnClickListener
            }

            // Valida robusteza da password
            val passwordValidation = validatePassword(password)
            if (!passwordValidation.isValid) {
                Toast.makeText(
                    this,
                    passwordValidation.errorMessage,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Verifica se as passwords são iguais
            if (password != confirmPassword) {
                Toast.makeText(this, "As passwords não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Redireciona para o Login caso tudo esteja válido
            authViewModel.register(name, email, password)
        }

        backToLoginTextView.setOnClickListener {
            finish() // Volta para LoginActivity
        }
    }

    /**
     * Valida formato de email (mesma implementação que LoginActivity)
     */
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    /**
     * Valida a força da password conforme política definida
     * @param password Password a validar
     * @return PasswordValidationResult com resultado da validação
     */
    private fun validatePassword(password: String): PasswordValidationResult {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return PasswordValidationResult(
                false,
                "A password deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres"
            )
        }


        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos uma letra maiúscula"
            )
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos uma letra minúscula"
            )
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos um número"
            )
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos um caractere especial (!@#$%^&*()_+-=[]{};':\"|,.<>?)"
            )
        }

        return PasswordValidationResult(true, null)
    }

    /**
     * Data class para resultado da validação de password
     */
    data class PasswordValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}