package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}

suspend fun generateAppBlueprint(category: String, techStack: String, userPrompt: String): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return "Erreur : La clé d'API Gemini n'est pas configurée dans le panneau de Secrets d'AI Studio. Veuillez configurer la clé GEMINI_API_KEY pour générer de vraies propositions d'architecture !"
    }

    val systemPrompt = """
        Tu es un architecte et ingénieur Android de niveau Principal chez Google. 
        L'utilisateur souhaite savoir quel genre d'applications Android sur mesure tu peux concevoir avec Jetpack Compose, Material 3, Room DB et Retrofit.
        Génère une proposition de blueprint/architecture d'application mobile extrêmement détaillée et séduisante en français, structurée avec des puces élégantes. Sans bla-bla marketing superflu et sans salutations. Va direct au fait.
        Garde un ton très professionnel, techniquement précis et inspirant.
        Utilise le format suivant :
        
        📱 NOM LOGIQUE : [Nom créatif d'app basé sur la saisie de l'utilisateur]
        🎯 OBJECTIF principal & Expérience utilisateur
        🎨 RECOMMANDATIONS VISUELLES (Animations Compose, thèmes, transitions fluides)
        🛠️ ARCHITECTURE ET CODE (ViewModel, Room Entités recommandées, services Retrofit, flow de données)
        🚀 3 FONCTIONNALITÉS TECHNIQUES ESSENTIELLES
        ⏱️ ESTIMATION DE DEV (en heures)
    """.trimIndent()

    val userFullPrompt = "Catégorie de l'application : $category. \nStack technique recommandée : $techStack. \nIdée/Description ou cible : $userPrompt"

    val request = GenerateContentRequest(
        contents = listOf(
            Content(parts = listOf(Part(text = userFullPrompt)))
        ),
        systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
        generationConfig = GenerationConfig(temperature = 0.7f)
    )

    return try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        responseText ?: "Aucun blueprint généré. Veuillez réessayer."
    } catch (e: Exception) {
        "Erreur lors de la communication avec l'API Gemini : ${e.localizedMessage ?: e.message}"
    }
}

suspend fun chatWithGemini(messagesHistory: List<Pair<String, Boolean>>): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return "Clé d'API non configurée. (Mode démo : je simule une créativité exceptionnelle car la clé GEMINI_API_KEY est manquante dans votre panneau de Secrets!)"
    }

    val systemPrompt = """
        Tu es l'Architecte Mobile IA Génératif suprême de CreaFlow Studio. Ton but est de concevoir, guider et conseiller l'utilisateur dans l'imagination et le développement d'applications mobiles Jetpack Compose de rituels zen, lecteurs audio, chats, et bases de données Room.
        Sois dynamique, créatif, concis et réponds toujours en français chaleureux. Structure tes réponses avec des puces claires et propose des astuces techniques (Ex: utiliser LaunchedEffect, collectAsStateWithLifecycle, etc.).
    """.trimIndent()

    // Package history in a single cohesive prompt block
    val fullPrompt = buildString {
        append("Voici l'historique récent de notre conversation :\n\n")
        messagesHistory.forEach { (text, isUser) ->
            if (isUser) {
                append("User: $text\n")
            } else {
                append("Assistant IA: $text\n")
            }
        }
        append("\nS'il te plaît, fournis la réponse suivante en français :\nAssistant IA: ")
    }

    val request = GenerateContentRequest(
        contents = listOf(
            Content(parts = listOf(Part(text = fullPrompt)))
        ),
        systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
        generationConfig = GenerationConfig(temperature = 0.7f)
    )

    return try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        responseText ?: "Impossible de charger une réponse. Réessayez."
    } catch (e: Exception) {
        "Erreur d'API : ${e.localizedMessage ?: e.message}"
    }
}
