package pt.ipt.dam2025.nocrastination.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2025.nocrastination.R
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    // Password policy constants
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MIN_USERNAME_LENGTH = 3

        // Regular expressions for password validation
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

            // All validations passed - register successful
            Toast.makeText(this, "Registo bem-sucedido!", Toast.LENGTH_SHORT).show()
            finish()
        }

        backToLoginTextView.setOnClickListener {
            finish()
        }
    }

    /**
     * Validates password against strong password policy
     * Returns a PasswordValidationResult with isValid flag and error message
     */
    private fun validatePassword(password: String): PasswordValidationResult {
        // Check minimum length
        if (password.length < MIN_PASSWORD_LENGTH) {
            return PasswordValidationResult(
                false,
                "A password deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres"
            )
        }

        // Check for at least one uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos uma letra maiúscula"
            )
        }

        // Check for at least one lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos uma letra minúscula"
            )
        }

        // Check for at least one digit
        if (!DIGIT_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos um número"
            )
        }

        // Check for at least one special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return PasswordValidationResult(
                false,
                "A password deve conter pelo menos um caractere especial (!@#$%^&*()_+-=[]{};':\"|,.<>?)"
            )
        }

        // All checks passed
        return PasswordValidationResult(true, null)
    }

    /**
     * Data class to hold password validation result
     */
    data class PasswordValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}