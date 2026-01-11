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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.FragmentProfileBinding
import pt.ipt.dam2025.nocrastination.domain.models.User
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.UserProfileViewModel
import pt.ipt.dam2025.nocrastination.ui.auth.LoginActivity
import pt.ipt.dam2025.nocrastination.ui.dialogs.AboutAppDialogFragment
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()
    private val userProfileViewModel: UserProfileViewModel by viewModel()

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

        // Verificar se o usuário está logado
        val preferenceManager = PreferenceManager(requireContext())
        val token = preferenceManager.getAuthToken()

        if (token.isNullOrEmpty()) {
            showLoginRequired()
        } else {
            setupObservers()
            setupClickListeners()
            loadProfile()
        }
    }

    private fun loadProfile() {
        userProfileViewModel.loadProfile()
    }

    private fun showLoginRequired() {
        binding.apply {
            contentLayout.visibility = View.GONE
            progressBar.visibility = View.GONE

            val message = "Por favor, faça login para ver seu perfil"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            buttonLogout.text = "Fazer Login"
            buttonLogout.setOnClickListener {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            userProfileViewModel.profileState.collectLatest { profile ->
                profile?.let { updateUI(it) }
            }
        }

        lifecycleScope.launch {
            userProfileViewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            userProfileViewModel.errorMessage.collectLatest { error ->
                error?.let {
                    Toast.makeText(context, "Erro: $it", Toast.LENGTH_SHORT).show()

                    // Se for erro 401 (não autorizado), token pode ter expirado
                    if (it.contains("401") || it.contains("Unauthorized")) {
                        showLoginRequired()
                    }

                    userProfileViewModel.clearError()
                }
            }
        }
    }

    private fun updateUI(profile: UserProfile) {
        binding.apply {
            textUserName.text = profile.fullName
            textUserEmail.text = "${profile.userId}"

            // Carregar avatar
            profile.avatarUrl?.let { avatarUrl ->
                Glide.with(this@ProfileFragment)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(imageAvatar)
            }

            // Atualizar configurações Pomodoro
            textTasksCompleted.text = "${profile.dailyGoalMinutes} min/dia"
            textTotalFocusTime.text = "${profile.pomodoroWorkDuration}min trabalho"
            textCurrentStreak.text = "${profile.pomodoroShortBreak}min pausa curta"
            textLevel.text = "${profile.pomodoroLongBreak}min pausa longa"
        }
    }

    private fun setupClickListeners() {
        // Botão de logout - MANTIDO COMO ESTAVA
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Botão de editar perfil
        binding.buttonEditProfile.setOnClickListener {
            openEditProfile()
        }

        // Botão de definições
        binding.buttonSettings.setOnClickListener {
            openSettings()
        }

        // SECÇÃO "SOBRE A APP"
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
            // Limpar preferências
            val preferenceManager = PreferenceManager(requireContext())
            preferenceManager.clearAll()

            // Chamar logout no ViewModel
            authViewModel.logout()

            // Redirecionar para login
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()

            Toast.makeText(context, "Sessão terminada com sucesso", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao terminar sessão: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEditProfile() {
        Toast.makeText(context, "Função em desenvolvimento", Toast.LENGTH_SHORT).show()
    }

    private fun openSettings() {
        Toast.makeText(context, "Definições em desenvolvimento", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        // Recarregar perfil quando voltar ao fragment
        val preferenceManager = PreferenceManager(requireContext())
        if (!preferenceManager.getAuthToken().isNullOrEmpty()) {
            loadProfile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}