package pt.ipt.dam2025.nocrastination.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.FragmentAboutAppDialogBinding

class AboutAppDialogFragment : DialogFragment() {

    private var _binding: FragmentAboutAppDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutAppDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupContent()
        setupClickListeners()
    }

    private fun setupContent() {
        // Informação obrigatória do guião
        binding.apply {
            textCourseName.text = "Licenciatura em Engenharia Informática"
            textSubject.text = "Desenvolvimento de Aplicações Móveis"
            textAcademicYear.text = "3º ano, 1º semestre - Ano Letivo 2025/26"

            // Informação dos autores (substituir pelas tuas informações)
            textAuthors.text = """
                Nome: Rodrigo Calisto
                Número: 12345
                
                Nome: [Nome do Colega]
                Número: [Número do Colega]
            """.trimIndent()

            // Bibliotecas usadas
            textLibraries.text = """
                • AndroidX Libraries
                • Material Components
                • Kotlin Coroutines
                • Room Database (para implementação futura)
                • Retrofit (para implementação futura)
            """.trimIndent()
        }
    }

    private fun setupClickListeners() {
        binding.buttonClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // Definir tamanho do dialog
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}