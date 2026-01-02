package pt.ipt.dam2025.nocrastination.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.databinding.FragmentProfileBinding
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.ui.auth.LoginActivity
import pt.ipt.dam2025.nocrastination.ui.dialogs.AboutAppDialogFragment

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Injetar o ViewModel do Koin
    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carregar dados reais do usuário (você precisará implementar isso)
        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        // TODO: Implementar carregamento real do perfil do usuário
        // Por enquanto, vamos usar dados mock
        binding.apply {
            textUserName.text = "Rodrigo Calisto"
            textUserEmail.text = "rodrigo@example.com"

            // Estatísticas do perfil
            textTasksCompleted.text = "12 tarefas"
            textTotalFocusTime.text = "25h 30m"
            textCurrentStreak.text = "7 dias"
            textLevel.text = "Nível 5"
        }
    }

    private fun setupClickListeners() {
        // Botão de logout
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Botão de editar perfil
        binding.buttonEditProfile.setOnClickListener {
            Toast.makeText(context, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        // Botão de definições
        binding.buttonSettings.setOnClickListener {
            Toast.makeText(context, "Definições em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        // SECÇÃO "SOBRE A APP" (OBRIGATÓRIO)
        binding.cardAboutApp.setOnClickListener {
            openAboutAppDialog()
        }

        // Links úteis
        binding.cardHelp.setOnClickListener {
            openHelpPage()
        }

        binding.cardPrivacy.setOnClickListener {
            openPrivacyPolicy()
        }

        binding.cardContact.setOnClickListener {
            sendFeedbackEmail()
        }
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Terminar Sessão")
            .setMessage("Tem a certeza que deseja terminar sessão?")
            .setPositiveButton("Terminar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performLogout() {
        try {
            // 1. Limpar todas as preferências manualmente para garantir
            val prefs = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 2. Chamar logout no ViewModel (se tiver lógica adicional)
            authViewModel.logout()

            // 3. Navegar para LoginActivity e limpar pilha de atividades
            val intent = Intent(requireActivity(), LoginActivity::class.java)

            // Flags importantes para limpar a pilha
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            // 4. Finalizar a atividade atual
            requireActivity().finish()

            Toast.makeText(context, "Sessão terminada com sucesso", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao terminar sessão: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAboutAppDialog() {
        val dialog = AboutAppDialogFragment()
        dialog.show(parentFragmentManager, "AboutAppDialog")
    }

    private fun openHelpPage() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
        startActivity(intent)
    }

    private fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
        startActivity(intent)
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:feedback@nocrastination.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback - App Nocrastination")
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, "Nenhuma app de email encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}