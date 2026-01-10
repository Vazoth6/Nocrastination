package pt.ipt.dam2025.nocrastination.ui.dialogs

import android.annotation.SuppressLint
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

    @SuppressLint("SetTextI18n")
    private fun setupContent() {
        // Informação obrigatória do guião
        binding.apply {
            textCourseName.text = "Curso: Licenciatura em Engenharia Informática"
            textSubject.text = "Disciplina: Desenvolvimento de Aplicações Móveis"
            textAcademicYear.text = "Ano Letivo: 25/26"

            // Informação dos autores (substituir pelas tuas informações)
            textAuthors.text = """
                Nome: Rodrigo Miguel Oliveira Calisto
                Número: 24851
            """.trimIndent()

            // Bibliotecas usadas
            textLibraries.text = """
                • androidx.core:core-ktx - Kotlin extensions para AndroidX Core
                • androidx.appcompat:appcompat - Compatibilidade com versões anteriores
                • com.google.android.material:material - Material Design Components
                • androidx.constraintlayout:constraintlayout - ConstraintLayout
                • androidx.recyclerview:recyclerview - RecyclerView
                • androidx.cardview:cardview - CardView
                • androidx.activity:activity-compose - Activity para Compose
                • androidx.compose.ui:ui - Compose UI core
                • androidx.compose.ui:ui-graphics - Compose Graphics
                • androidx.compose.ui:ui-tooling-preview - Compose Tooling Preview
                • androidx.compose.material3:material3 - Material 3 para Compose
                • com.github.bumptech.glide:glide - Glide (carregamento de imagens)
                • androidx.lifecycle:lifecycle-runtime-ktx - Lifecycle Runtime
                • androidx.lifecycle:lifecycle-livedata-ktx - LiveData
                • androidx.lifecycle:lifecycle-viewmodel-ktx - ViewModel
                • androidx.activity:activity-ktx - Activity KTX extensions
                • androidx.fragment:fragment-ktx - Fragment KTX extensions
                • androidx.navigation:navigation-fragment-ktx - Navigation Component
                • androidx.navigation:navigation-ui-ktx - Navigation UI
                • com.squareup.retrofit2:retrofit - Retrofit HTTP client
                • com.squareup.retrofit2:converter-gson - GSON converter para Retrofit
                • com.squareup.okhttp3:okhttp - OkHttp client
                • com.squareup.okhttp3:logging-interceptor - OkHttp logging interceptor
                • org.chromium.net:cronet-embedded - Cronet networking engine
                • org.jetbrains.kotlinx:kotlinx-coroutines-android - Coroutines para Android
                • io.insert-koin:koin-android - Koin para Android
                • io.insert-koin:koin-androidx-compose - Koin para Compose
                • io.insert-koin:koin-androidx-workmanager - Koin para WorkManager
                • com.auth0.android:jwtdecode - JWT decoding
                • com.google.android.gms:play-services-location - Location Services
                • com.google.android.gms:play-services-maps - Google Maps
                • com.google.android.libraries.places:places - Places API
                • androidx.work:work-runtime-ktx - WorkManager
                • com.jakewharton.threetenabp:threetenabp - ThreeTenABP (date/time)
                • junit:junit - JUnit
                • androidx.test.ext:junit - AndroidX JUnit extensions
                • androidx.test.espresso:espresso-core - Espresso
                • androidx.compose.ui:ui-test-junit4 - Compose UI testing
                • androidx.compose.ui:ui-test-manifest - Compose test manifest
                • io.insert-koin:koin-test - Koin testing
                • io.insert-koin:koin-test-junit4 - Koin JUnit testing
                • androidx.compose.ui:ui-tooling - Compose tooling
                • org.jetbrains.kotlin:kotlin-stdlib - Kotlin Standard Library
                • androidx.compose:compose-bom - Compose Bill of Materials
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