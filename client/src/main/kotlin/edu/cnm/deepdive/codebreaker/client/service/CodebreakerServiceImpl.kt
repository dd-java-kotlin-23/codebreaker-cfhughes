package edu.cnm.deepdive.codebreaker.client.service

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import edu.cnm.deepdive.codebreaker.client.dto.ErrorResponse
import edu.cnm.deepdive.codebreaker.client.dto.GameRequest
import edu.cnm.deepdive.codebreaker.client.dto.GameResponse
import edu.cnm.deepdive.codebreaker.client.dto.GuessRequest
import edu.cnm.deepdive.codebreaker.client.dto.GuessResponse
import edu.cnm.deepdive.codebreaker.client.web.CodebreakerApi
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal object CodebreakerServiceImpl : CodebreakerService {
    private val client: OkHttpClient
    private val moshi: Moshi
    private val api: CodebreakerApi
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val properties = loadProperties()
        client = buildClient(properties)
        moshi = buildMoshi()
        api = buildApi(properties, moshi, client)
    }

    override fun startGame(game: GameRequest): CompletableFuture<GameResponse> {
        return scope.future {
            handleResponse(api.startGame(game))
        }
    }

    override fun getGame(gameId: String): CompletableFuture<GameResponse> {
        return scope.future {
            handleResponse(api.getGame(gameId))
        }
    }

    override fun deleteGame(gameId: String): CompletableFuture<Void?> {
        return scope.future {
            handleResponse(api.deleteGame(gameId))
        }
    }

    override fun submitGuess(
        gameId: String,
        guess: GuessRequest
    ): CompletableFuture<GuessResponse> {
        return scope.future {
            handleResponse(api.submitGuess(gameId, guess))
        }
    }

    override fun getGuess(
        gameId: String,
        guessId: String
    ): CompletableFuture<GuessResponse> {
        return scope.future {
            handleResponse(api.getGuess(gameId, guessId))
        }
    }

    private fun <T> handleResponse(response: Response<T>): T =
        if (response.isSuccessful) {
            response.body() ?: throw RuntimeException()
        } else {
            handleUnsuccessfulResponse(response)
        }

    private fun handleResponse(response: Response<Unit>): Void? =
        if (response.isSuccessful) {
            null
        } else {
            handleUnsuccessfulResponse(response)
        }

    private fun <T> handleUnsuccessfulResponse(response: Response<T>): Nothing {
        val adapter = moshi.adapter(ErrorResponse::class.java)
        val errorText = response.errorBody().toString()
        val exception = adapter
            .fromJson(errorText)
            ?.let {
                ApiException(it)
            }
            ?: RuntimeException()
        throw exception
    }

}

private class OffsetDateTimeAdapter {
    @ToJson
    fun toJson(value: OffsetDateTime): String =
        value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    @FromJson
    fun fromJson(value: String): OffsetDateTime =
        OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

private const val PROPERTIES_FILE = "web-service.properties"
private const val LOG_LEVEL_KEY = "logLevel"
private const val BASE_URL_KEY = "baseUrl"

private fun loadProperties(): Properties =
    CodebreakerServiceImpl::class.java
        .classLoader
        .getResourceAsStream(PROPERTIES_FILE)
        .use {
            Properties().apply {
                load(it)
            }
        }

private fun buildMoshi(): Moshi =
    Moshi.Builder()
        .add(OffsetDateTimeAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

private fun buildClient(properties: Properties): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
        .setLevel(
            HttpLoggingInterceptor.Level.valueOf(
                properties.getProperty(LOG_LEVEL_KEY).uppercase()
            )
        )
    return OkHttpClient.Builder()
        .addNetworkInterceptor(interceptor)
        .build()
}

private fun buildApi(
    properties: Properties,
    @Suppress("SameParameterValue") moshi: Moshi,
    @Suppress("SameParameterValue") client: OkHttpClient
): CodebreakerApi =
    Retrofit.Builder()
        .baseUrl(properties.getProperty(BASE_URL_KEY))
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()
        .create(CodebreakerApi::class.java)

