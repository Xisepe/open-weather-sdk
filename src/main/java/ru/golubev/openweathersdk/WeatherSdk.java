package ru.golubev.openweathersdk;

import java.util.function.Consumer;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.CacheConfigurer;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.HttpClientConfigurer;
import ru.golubev.openweathersdk.internal.OpenWeatherSdk;

/**
 * Main interface for OpenWeather SDK functionality.
 *
 * <p>Defines the contract for creating and managing weather API clients with
 * shared resource configuration and lifecycle management.</p>
 *
 * <p><b>Usage through static factory method:</b></p>
 * <pre>{@code
 * WeatherSdk sdk = WeatherSdk.instance();
 *
 * WeatherClient client = sdk.build("api-key", cfg -> cfg
 *     .language(SupportedLanguage.ENGLISH)
 *     .units(Units.METRIC));
 * }</pre>
 *
 * @see OpenWeatherSdk for implementation details and comprehensive documentation
 * @see WeatherClient
 * @see WeatherSdkConfigurer
 * @since 1.0
 */
public interface WeatherSdk extends AutoCloseable {

    /**
     * Creates a weather client with specified API key and default configuration.
     *
     * @param apiKey OpenWeather API key for authentication
     * @return configured weather client instance
     * @see OpenWeatherSdk#build(String) for implementation details
     */
    WeatherClient build(String apiKey);

    /**
     * Creates a weather client with customized configuration.
     *
     * @param apiKey OpenWeather API key for authentication
     * @param customizer consumer for configuring client parameters
     * @return configured weather client instance
     * @see OpenWeatherSdk#build(String, Consumer) for implementation details
     */
    WeatherClient build(String apiKey, Consumer<WeatherSdkConfigurer> customizer);

    /**
     * Configures shared HTTP client for all subsequently created clients.
     *
     * @param configurer consumer for HTTP client configuration
     * @return this SDK instance for method chaining
     * @see OpenWeatherSdk#sharedClient(Consumer) for implementation details
     */
    WeatherSdk sharedClient(Consumer<HttpClientConfigurer> configurer);

    /**
     * Configures shared cache for all subsequently created clients.
     *
     * @param configurer consumer for cache configuration
     * @return this SDK instance for method chaining
     * @see OpenWeatherSdk#sharedCache(Consumer) for implementation details
     */
    WeatherSdk sharedCache(Consumer<CacheConfigurer> configurer);

    /**
     * Retrieves existing client by API key.
     *
     * @param apiKey the API key used during client creation
     * @return weather client instance or null if not found
     * @see OpenWeatherSdk#client(String) for implementation details
     */
    WeatherClient client(String apiKey);

    /**
     * Removes and shuts down client with specified API key.
     *
     * @param apiKey the API key of client to remove
     * @see OpenWeatherSdk#deleteClient(String) for implementation details
     */
    void deleteClient(String apiKey);

    /**
     * Returns the singleton SDK instance.
     *
     * @return singleton WeatherSdk instance
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Preferred usage pattern
     * WeatherSdk sdk = WeatherSdk.instance();
     * }</pre>
     */
    static WeatherSdk instance() {
        return OpenWeatherSdk.getInstance();
    }

}
