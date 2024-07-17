package dev.thoq

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.google.gson.Gson as GsonK
import kotlinx.coroutines.Dispatchers as DispatchersK
import okhttp3.Callback as CallbackK
import okhttp3.OkHttpClient as OkHttpClientK
import okhttp3.Request as RequestK
import okhttp3.Response as ResponseK
import java.io.IOException as IOExceptionJ

data class ApiResponse(
    val hits: List<Hit>
)

data class Hit(
    val recipe: Recipe?
)

data class Recipe(
    val label: String?
)

const val appID = "64972f46"
const val appKey = "b94b68d18635a79f08f7a70d11b10ddf"
val client = OkHttpClientK()

suspend fun fetchAPI(searchTerm: String): List<String> {
    val url = "https://api.edamam.com/search?q=$searchTerm&app_id=$appID&app_key=$appKey"

    val request = RequestK.Builder()
        .url(url)
        .build()

    var recipesList = listOf<String>()

    val callback = object : CallbackK {
        override fun onFailure(call: okhttp3.Call, e: IOExceptionJ) {
            Log.error("Failed to execute request: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: ResponseK) {
            when {
                !response.isSuccessful -> { Log.error("Unexpected code $response"); return }
            }

            val responseBody = response.body?.string()
            val gson = GsonK()
            val result = gson.fromJson(responseBody, ApiResponse::class.java)
            val recipes = result.hits.mapNotNull { hit: Hit -> hit.recipe?.label }

            recipes.also { recipesList = it }
        }
    }

    client.newCall(request).enqueue(callback)

    withContext(DispatchersK.IO) { Thread.sleep(5000) }
    return recipesList
}

fun main() {
    Log.accent("---------------------------------------------------")
    Log.welcome("Welcome to Recipe Finder!")
    Log.credit("Developed by Tristan")
    Log.input("Please provide a search term: ")
    val searchTerm = readlnOrNull() ?: ""
    when {
        searchTerm.isBlank() -> { Log.warn("Search term cannot be empty."); main() }
    }
    Log.accent("---------------------------------------------------")
    val foundRecipes = runBlocking { fetchAPI(searchTerm) }
    displayResults(foundRecipes)
}

fun displayResults(recipes: List<String>) {
    when {
        recipes.isEmpty() -> { Log.warn("No recipes found."); main() }
        else -> {
            Log.info("Found recipes:")
            recipes.forEach {
                Log.recipe(it + " " + "https://google.com/search?q=$it".replace(" ", "+"))
            }
            main()
        }
    }
}

class Log {
    companion object {
        private const val ANSI_RESET = "\u001B[0m" // Reset
        private const val ANSI_INPUT = "\u001B[34m" // Blue
        private const val ANSI_WARN = "\u001B[33m" // Yellow
        private const val ANSI_ERROR = "\u001B[31m" // Red
        private const val ANSI_INFO = "\u001B[36m" // Cyan
        private const val ANSI_RECIPE = "\u001B[35m" // Purple
        private const val ANSI_ACCENT = ANSI_INPUT // Blue
        private const val ANSI_CREDIT = "\u001B[32m" // Green
        private const val ANSI_WELCOME = ANSI_CREDIT // Green

        fun info(message: String) { println("$ANSI_INFO[INFO] $message$ANSI_RESET") }
        fun warn(message: String) { println("$ANSI_WARN[WARN] $message$ANSI_RESET") }
        fun error(message: String) { println("$ANSI_ERROR[ERROR] $message$ANSI_RESET") }
        fun input(message: String) { print("$ANSI_INPUT[INPUT] $message$ANSI_ACCENT") }
        fun recipe(message: String) { println("$ANSI_RECIPE[RECIPE] $message$ANSI_RESET") }
        fun accent(message: String) { println("$ANSI_ACCENT$message$ANSI_RESET") }
        fun credit(message: String) { println("$ANSI_CREDIT[CREDIT] $message$ANSI_RESET") }
        fun welcome(message: String) { println("$ANSI_WELCOME[WELCOME] $message$ANSI_RESET") }
    }
}
