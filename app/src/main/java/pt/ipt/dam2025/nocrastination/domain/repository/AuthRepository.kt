package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.UserProfile
import pt.ipt.dam2025.nocrastination.utils.Resource

interface AuthRepository {

    /**
     * Autentica um utilizador na aplicação
     * @param email Email ou identificador do utilizador
     * @param password Palavra-passe do utilizador
     * @return Resource<Unit> - Resultado da operação (sucesso/erro)
     *         Unit indica que o principal efeito é guardar o token localmente
     */
    suspend fun login(email: String, password: String): Resource<Unit>

    /**
     * Regista um novo utilizador na aplicação
     * @param username Nome de utilizador único
     * @param email Email válido do utilizador
     * @param password Palavra-passe (deve ser segura)
     * @return Resource<Unit> - Normalmente faz login automático após registo
     */
    suspend fun register(username: String, email: String, password: String): Resource<Unit>

    /**
     * Obtém o perfil do utilizador atualmente autenticado na aplicação
     * @return Resource<UserProfile> - Perfil completo do utilizador
     *         Inclui preferências e informações pessoais
     */
    suspend fun getCurrentUser(): Resource<UserProfile>

    /**
     * Termina a sessão do utilizador atual
     * Remove token de autenticação e dados de sessão locais
     * Não é suspend porque é uma operação local rápida
     */
    fun logout()

    /**
     * Verifica se existe uma sessão ativa de utilizador
     * @return Boolean - true se o utilizador estiver autenticado
     *         Baseia-se na existência de token guardado localmente
     */
    fun isLoggedIn(): Boolean
}