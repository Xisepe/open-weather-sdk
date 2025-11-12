package ru.golubev.openweathersdk.internal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.golubev.openweathersdk.SupportedLanguage;
import ru.golubev.openweathersdk.Units;
import ru.golubev.openweathersdk.exception.OpenWeatherApiClientException;
import ru.golubev.openweathersdk.exception.RequestCancellationException;
import ru.golubev.openweathersdk.exception.ShutdownException;
import ru.golubev.openweathersdk.exception.WeatherApiException;
import ru.golubev.openweathersdk.exception.WeatherExceptionHandler;
import ru.golubev.openweathersdk.model.WeatherResponse;

/**
 * Main client for interacting with the OpenWeather API.
 *
 * <p>This class provides both synchronous and asynchronous methods to retrieve
 * current weather data from the OpenWeather API. It handles HTTP communication,
 * response parsing, error handling, and resource management.</p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Synchronous and asynchronous weather data retrieval</li>
 *   <li>Multi-language support for weather descriptions</li>
 *   <li>Multiple unit systems (metric/imperial)</li>
 *   <li>Comprehensive error handling and exception mapping</li>
 *   <li>Configurable HTTP client (shared or dedicated)</li>
 *   <li>Graceful shutdown and resource cleanup</li>
 *   <li>Extensive logging capabilities</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe and can be used concurrently
 * from multiple threads. The {@link AtomicBoolean} ensures safe shutdown state management.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Synchronous call
 * WeatherResponse weather = api.weather("London", SupportedLanguage.ENGLISH, Units.METRIC);
 *
 * // Asynchronous call
 * CompletableFuture<WeatherResponse> future = api.weatherAsync("Paris", SupportedLanguage.FRENCH, Units.METRIC);
 * future.thenAccept(response -> {
 *     System.out.println("Temperature: " + response.getTemperature().getTemp());
 * });
 *
 * // Shutdown when done
 * api.shutdown();
 * }</pre>
 *
 * @see WeatherResponse
 * @see SupportedLanguage
 * @see Units
 * @see OpenWeatherApiClientException
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public final class OpenWeatherApi {

    /**
     * Base URL for OpenWeather API endpoints.
     */
    private static final String BASE_URL = "http://api.openweathermap.org";

    /**
     * Weather endpoint path for current weather data.
     */
    private static final String WEATHER_ENDPOINT = "/data/2.5/weather";

    /**
     * Query parameter name for city name.
     */
    private static final String CITY_QUERY_PARAM = "q";

    /**
     * Query parameter name for language.
     */
    private static final String LANGUAGE_QUERY_PARAM = "lang";

    /**
     * Query parameter name for units system.
     */
    private static final String UNITS_QUERY_PARAM = "units";

    /**
     * Query parameter name for API key authentication.
     */
    private static final String APPID_QUERY_PARAM = "appid";

    /**
     * OpenWeather API key for authentication.
     *
     * <p>This key is required for all API requests and is included in every call
     * to the OpenWeather service.</p>
     */
    private final String apiKey;

    /**
     * Flag indicating whether debug and warning logging is enabled.
     *
     * <p>When enabled, provides detailed logging of API calls, responses, and errors.
     * Recommended for development and debugging purposes.</p>
     */
    private final boolean loggingEnabled;

    /**
     * HTTP client used for making API requests.
     *
     * <p>Can be either a shared client (provided externally) or a dedicated client
     * created specifically for this API instance.</p>
     */
    private final OkHttpClient client;

    /**
     * Indicates whether the HTTP client is shared with other components.
     *
     * <p>If true, the client will not be shutdown when {@link #shutdown()} is called,
     * as it may be used by other parts of the application.</p>
     */
    private final boolean isClientShared;

    /**
     * JSON object mapper for serialization and deserialization.
     *
     * <p>Configured to ignore unknown properties in API responses and includes
     * custom mixin for {@link WeatherResponse} serialization.</p>
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addMixIn(WeatherResponse.class, WeatherResponseMixin.class);

    /**
     * Atomic boolean tracking the shutdown state of this API instance.
     *
     * <p>Prevents operations on a shutdown instance and ensures thread-safe
     * state management.</p>
     */
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * Retrieves current weather data synchronously for the specified city.
     *
     * <p>This method blocks until the API response is received and processed.
     * Use for simple applications or when asynchronous processing is not required.</p>
     *
     * @param city the city name for which to retrieve weather data; must not be null or empty
     * @param lang the language for weather descriptions; must not be null
     * @param unit the measurement system for weather data; must not be null
     * @return {@link WeatherResponse} containing comprehensive weather information
     * @throws ShutdownException if the API instance has been shut down
     * @throws RequestCancellationException if the HTTP request was cancelled
     * @throws OpenWeatherApiClientException for network or IO-related errors
     * @throws WeatherApiException for API-level errors (invalid API key, city not found, etc.)
     * @throws IllegalStateException for response parsing failures
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try {
     *     WeatherResponse response = api.weather("London", SupportedLanguage.ENGLISH, Units.METRIC);
     *     System.out.println("Temperature: " + response.getTemperature().getTemp());
     * } catch (WeatherApiException e) {
     *     System.out.println("API error: " + e.getMessage());
     * } catch (OpenWeatherApiClientException e) {
     *     System.out.println("Network error: " + e.getMessage());
     * }
     * }</pre>
     *
     * @see WeatherResponse
     * @see SupportedLanguage
     * @see Units
     */
    public WeatherResponse weather(String city, SupportedLanguage lang, Units unit) {
        if (loggingEnabled) {
            log.debug("OpenWeatherApi sync call; args=[city={}, lang={}, units={}]", city, lang, unit);
        }
        checkShutdown();

        Call call = buildCall(city, lang, unit);

        try (Response response = call.execute()) {
            return handleResponse(response);
        } catch (IOException e) {
            if (call.isCanceled()) {
                if (loggingEnabled) {
                    log.debug("OpenWeatherApi request was cancelled", e);
                }
                throw new RequestCancellationException(e);
            }

            if (loggingEnabled) {
                log.warn("OpenWeatherApi error while calling OpenWeatherApi", e);
            }
            throw new OpenWeatherApiClientException(e);
        }
    }


    /**
     * Retrieves current weather data asynchronously for the specified city.
     *
     * <p>This method returns immediately with a {@link CompletableFuture} that
     * will be completed when the API response is available. Suitable for
     * non-blocking applications and reactive programming patterns.</p>
     *
     * @param city the city name for which to retrieve weather data; must not be null or empty
     * @param lang the language for weather descriptions; must not be null
     * @param unit the measurement system for weather data; must not be null
     * @return {@link CompletableFuture} that will be completed with the {@link WeatherResponse}
     *         or completed exceptionally with an appropriate exception
     * @throws ShutdownException if the API instance has been shut down
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * CompletableFuture<WeatherResponse> future = api.weatherAsync(
     *     "Tokyo", SupportedLanguage.JAPANESE, Units.METRIC);
     *
     * future.thenApply(response -> {
     *     System.out.println("Temperature: " + response.getTemperature().getTemp());
     *     return response;
     * }).exceptionally(throwable -> {
     *     if (throwable instanceof WeatherApiException) {
     *         System.out.println("API error: " + throwable.getMessage());
     *     } else if (throwable instanceof OpenWeatherApiClientException) {
     *         System.out.println("Network error: " + throwable.getMessage());
     *     }
     *     return null;
     * });
     * }</pre>
     *
     * @see CompletableFuture
     * @see FutureWrapper
     */
    public CompletableFuture<WeatherResponse> weatherAsync(String city, SupportedLanguage lang, Units unit) {
        if (loggingEnabled) {
            log.debug("OpenWeatherApi async call; args=[city={}, lang={}, units={}]", city, lang, unit);
        }
        checkShutdown();

        Call call = buildCall(city, lang, unit);
        return FutureWrapper.wrap(call, loggingEnabled)
            .thenApply(this::handleResponse);
    }

    /**
     * Verifies that the API instance has not been shut down.
     *
     * @throws ShutdownException if the API instance has been shut down
     */
    private void checkShutdown() {
        if (shutdown.get()) {
            if (loggingEnabled) {
                log.warn("OpenWeatherApi has been shut down");
            }
            throw new ShutdownException();
        }
    }

    /**
     * Builds an HTTP call for the weather API request.
     *
     * @param city the target city name
     * @param lang the language for descriptions
     * @param unit the measurement system
     * @return configured {@link Call} ready for execution
     */
    private Call buildCall(String city, SupportedLanguage lang, Units unit) {
        Request request = new Request.Builder()
            .url(BASE_URL + WEATHER_ENDPOINT + buildQuery(city, lang, unit))
            .get()
            .build();

        return client.newCall(request);
    }

    /**
     * Constructs the query string for the API request.
     *
     * @param city the target city name (will be URL-encoded)
     * @param lang the language for descriptions
     * @param unit the measurement system
     * @return formatted query string including all parameters
     */
    private String buildQuery(String city, SupportedLanguage lang, Units unit) {
        StringBuilder query = new StringBuilder();
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);

        query
            .append('?')
            .append(CITY_QUERY_PARAM).append('=').append(encodedCity).append('&')
            .append(LANGUAGE_QUERY_PARAM).append('=').append(lang).append('&')
            .append(UNITS_QUERY_PARAM).append('=').append(unit).append('&')
            .append(APPID_QUERY_PARAM).append('=').append(apiKey);

        return query.toString();
    }

    /**
     * Processes the HTTP response and converts it to a {@link WeatherResponse}.
     *
     * <p>Handles both successful responses and API errors, converting them to
     * appropriate exceptions or parsed response objects.</p>
     *
     * @param response the HTTP response from OpenWeather API
     * @return parsed {@link WeatherResponse} object
     * @throws WeatherApiException for API-level errors (4xx, 5xx responses)
     * @throws IllegalStateException for response parsing failures
     */
    private WeatherResponse handleResponse(Response response) {
        if (!response.isSuccessful()) {
            try {
                WeatherApiException ex = WeatherExceptionHandler.parse(response.message(), response.body().string());

                if (loggingEnabled) {
                    log.debug("OpenWeatherApi received error response.", ex);
                }
                throw ex;
            } catch (IOException e) {
                if (loggingEnabled) {
                    log.error("OpenWeatherApi received unhandled response.", e);
                }
                throw new IllegalStateException("OpenWeatherApi received unhandled response.");
            }
        }
        try {
            String responseBody = response.body().string();

            if (loggingEnabled) {
                log.debug("OpenWeatherApi received response: {}", responseBody);
            }
            return objectMapper.readValue(responseBody, WeatherResponse.class);
        } catch (IOException e) {
            if (loggingEnabled) {
                log.error("Failed to parse response", e);
            }
            throw new IllegalStateException("Unable to parse response", e);
        }
    }


    /**
     * Shuts down the API instance and releases resources.
     *
     * <p>After shutdown, all subsequent method calls will throw {@link ShutdownException}.
     * If using a shared HTTP client, only the API instance is shut down, not the client.
     * If using a dedicated client, the HTTP client is properly shutdown.</p>
     *
     * <p><b>Note:</b> This method is idempotent - calling it multiple times has no
     * additional effect after the first successful call.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * try {
     *     // Use API...
     * } finally {
     *     api.shutdown();
     * }
     * }</pre>
     *
     * @see HttpClientShutdown
     */
    public void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            if (loggingEnabled) {
                log.debug("OpenWeatherApi already shut down");
            }
            return;
        }

        if (isClientShared) {
            return;
        }
        HttpClientShutdown.shutdown(client, loggingEnabled);
    }
}
