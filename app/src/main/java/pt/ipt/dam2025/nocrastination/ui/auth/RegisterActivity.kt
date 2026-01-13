package pt.ipt.dam2025.nocrastination.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModel()

    // Password policy constants
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MIN_USERNAME_LENGTH = 3

        private val UPPERCASE_PATTERN = Pattern.compile("[A-Z]")
        private val LOWERCASE_PATTERN = Pattern.compile("[a-z]")
        private val DIGIT_PATTERN = Pattern.compile("[0-9]")
        private val SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")
    }

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
                    finish()
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

            // Validate all fields are filled
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate username length
            if (name.length < MIN_USERNAME_LENGTH) {
                Toast.makeText(
                    this,
                    "O nome de utilizador deve ter pelo menos $MIN_USERNAME_LENGTH caracteres",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Validate email
            if (!isValidEmail(email)) {
                emailEditText.error = "Por favor, insira um email válido"
                return@setOnClickListener
            }

            // Validate password strength
            val passwordValidation = validatePassword(password)
            if (!passwordValidation.isValid) {
                Toast.makeText(
                    this,
                    passwordValidation.errorMessage,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "As passwords não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // All validations passed - call ViewModel to register
            authViewModel.register(name, email, password)
        }

        backToLoginTextView.setOnClickListener {
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

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

    data class PasswordValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}