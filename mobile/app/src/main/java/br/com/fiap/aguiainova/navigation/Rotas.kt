package br.com.fiap.aguiainova.navigation

import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Perfil

/** Todas as rotas do app em um so lugar. Rotas com parametro expoem um construtor para evitar montar texto na mao. */
object Rotas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    const val HOME_OPERADOR = "home_operador"
    const val HOME_GESTOR = "home_gestor"
    const val HOME_LIDERANCA = "home_lideranca"

    const val IDEIAS = "ideias"
    const val ESTRATEGIAS = "estrategias"
    const val PROJETOS = "projetos"
    const val DASHBOARD = "dashboard"
    const val PERFIL = "perfil"

    const val ARGUMENTO_ID = "id"
    const val ARGUMENTO_ABA = "aba"

    /** Aba do assistente de IA na tela de estrategias (so existe para o gestor). */
    const val ABA_ASSISTENTE = "assistente"

    // Padroes (usados no NavGraph) e construtores (usados para navegar)
    const val PADRAO_ESTRATEGIAS = "estrategias?$ARGUMENTO_ABA={$ARGUMENTO_ABA}"
    const val PADRAO_IDEIA_FORM = "ideias/form?$ARGUMENTO_ID={$ARGUMENTO_ID}"
    const val PADRAO_ESTRATEGIA_FORM = "estrategias/form?$ARGUMENTO_ID={$ARGUMENTO_ID}"
    const val PADRAO_ESTRATEGIA_DETALHE = "estrategia/{$ARGUMENTO_ID}"
    const val PROJETO_NOVO = "projetos/novo"
    const val PADRAO_PROJETO_DETALHE = "projetos/{$ARGUMENTO_ID}"

    /** Sem [aba] abre a tela de estrategias na aba padrao (as vigentes); com [aba], direto nela. */
    fun estrategias(aba: String? = null) = ESTRATEGIAS + if (aba == null) "" else "?$ARGUMENTO_ABA=$aba"

    /** Sem [id] abre o formulario de criacao; com [id], o de edicao. */
    fun ideiaForm(id: String? = null) = "ideias/form" + parametroOpcional(id)

    fun estrategiaForm(id: String? = null) = "estrategias/form" + parametroOpcional(id)

    fun estrategiaDetalhe(id: String) = "estrategia/$id"

    fun projetoDetalhe(id: String) = "projetos/$id"

    fun homeDe(perfil: Perfil): String = when (perfil) {
        Perfil.OPERADOR -> HOME_OPERADOR
        Perfil.GESTOR -> HOME_GESTOR
        Perfil.LIDERANCA -> HOME_LIDERANCA
    }

    private fun parametroOpcional(id: String?) = if (id == null) "" else "?$ARGUMENTO_ID=$id"
}

/** Troca de aba da barra inferior: mantem o estado de cada aba e nao empilha destinos repetidos. */
fun NavController.irParaAba(rota: String, perfil: Perfil) {
    navigate(rota) {
        popUpTo(Rotas.homeDe(perfil)) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
