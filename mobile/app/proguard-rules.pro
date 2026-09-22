# O Gson le e escreve estes modelos por reflexao: se a ofuscacao (R8) for ativada no release,
# os nomes dos campos precisam ser preservados para casar com o JSON da API.
-keep class br.com.fiap.aguiainova.domain.model.** { *; }
-keep class br.com.fiap.aguiainova.data.remote.dto.** { *; }
-keepattributes Signature, *Annotation*
