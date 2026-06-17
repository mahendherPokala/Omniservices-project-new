package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- Gemini API Request Models using Moshi ---
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null
)

data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

// --- Retrofit API Service ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- High-Level Call Facilitator ---
object GeminiRepository {
    private const val TAG = "GeminiRepository"

    suspend fun getConcregeReply(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is unconfigured. Utilizing smart fallback parser.")
            return@withContext getLocalFallbackResponse(prompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = """
                You are OmniService AI Concierge, a brilliant personal home service assistant.
                You help customers book haircuts, electrical work, plumbing, cleaning, AC repairs, and home maintenance.
                Keep responses polite, exciting, clear, and under 3 paragraphs. Focus on offering direct service bookings.
                When user asks about booking, say you can instantly set up the booking for them.
            """.trimIndent())))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val textValue = response.candidates?.getOrNull(0)?.content?.parts?.getOrNull(0)?.text
            if (!textValue.isNullOrBlank()) {
                textValue
            } else {
                getLocalFallbackResponse(prompt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API request failed: ${e.message}. Using fallback.", e)
            getLocalFallbackResponse(prompt)
        }
    }

    private fun getLocalFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi ") || lower.contains("hey") -> {
                "Hello there! Welcome to OmniService AI Concierge. I can help you solve any household issues or book professional services instantly. Try asking me:\n\n• 'I need a premium haircut tomorrow'\n• 'My kitchen sink is leaking, help!'\n• 'Can someone clean my apartment today?'"
            }
            lower.contains("hair") || lower.contains("cut") || lower.contains("salon") -> {
                "I can book our elite stylist, John Miller, for a Men's Haircut, Spa & Styling starting at ₹499. Would you like me to schedule him for tomorrow?"
            }
            lower.contains("sink") || lower.contains("leak") || lower.contains("plumb") || lower.contains("water") -> {
                "Oh no, plumbing emergencies can be stressful! I can dispatch Alice Simmons or Robert Carter to resolve this leak. Diagnostic fees start at ₹599. Ready to lock this in?"
            }
            lower.contains("clog") || lower.contains("clean") || lower.contains("dust") || lower.contains("vacuum") -> {
                "Perfect! We have Alice Simmons available today for a Deep Home Vacuuming & Sanitization session starting at ₹899. Would you like to check her slot?"
            }
            lower.contains("ac") || lower.contains("cooler") || lower.contains("heat") || lower.contains("repair") -> {
                "Absolutely! David Chen is our AC wizard. He offers Complete Inverter AC Gas Refill & Service starting at ₹1299. His local rating is 4.9⭐ with over 400 completions. Shall we schedule him?"
            }
            lower.contains("painting") || lower.contains("wall") -> {
                "We can book Sarah Williams for beautiful Wall Textured Painting starting at ₹1499. Let me know if you would like custom color consultations!"
            }
            lower.contains("status") || lower.contains("my booking") || lower.contains("track") -> {
                "You can view your active bookings and follow real-time route dispatches directly on the 'Active Bookings' list in your Customer Hub!"
            }
            else -> {
                "I understand! I can help you with services in Salon, Plumbing, Cleaning, AC Repair, and Painting. Tell me what needs attention, or select 'Auto Assign Specialist' to match the nearest provider instantly!"
            }
        }
    }
}
