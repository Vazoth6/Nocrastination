package pt.ipt.dam2025.nocrastination

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
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
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    Toast.makeText(this, "Registo bem-sucedido!", Toast.LENGTH_SHORT).show()
                    // Voltar para a LoginActivity
                    finish()
                } else {
                    Toast.makeText(this, "As passwords não coincidem", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        backToLoginTextView.setOnClickListener {
            finish()
        }
    }
}


/*import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam2025.nocrastination.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Remover a action bar
        supportActionBar?.hide()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Botão de Registo
        binding.registerButton.setOnClickListener {
            attemptRegistration()
        }

        // Link para voltar ao Login
        binding.backToLoginTextView.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun attemptRegistration() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (validateRegistrationFields(name, email, password, confirmPassword)) {
            // Simulação de registo (substituir por chamada à API real)
            registerUser(name, email, password)
        }
    }

    private fun validateRegistrationFields(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Validar nome
        if (name.isEmpty()) {
            binding.nameInputLayout.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }

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

        // Validar confirmação de password
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = getString(R.string.field_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = getString(R.string.password_mismatch)
            isValid = false
        } else {
            binding.confirmPasswordInputLayout.error = null
        }

        return isValid
    }

    private fun registerUser(name: String, email: String, password: String) {
        // TODO: Substituir por chamada à API real
        // Simulação de registo bem-sucedido

        // Guardar dados localmente (para testes)
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_password", password) // NOTA: Nunca guardar passwords em plain text!
            apply()
        }

        showSuccessAndNavigate()
    }

    private fun showSuccessAndNavigate() {
        Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_LONG).show()

        // Voltar para LoginActivity após 1 segundo
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, 1000)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)

        // Passar o email preenchido para facilitar o login
        val email = binding.emailEditText.text.toString().trim()
        if (email.isNotEmpty()) {
            intent.putExtra("REGISTERED_EMAIL", email)
        }

        startActivity(intent)
        finish() // Terminar a RegisterActivity
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}*/
