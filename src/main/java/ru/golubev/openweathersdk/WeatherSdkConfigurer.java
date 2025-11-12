package ru.golubev.openweathersdk;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Configuration class for OpenWeather SDK client setup.
 *
 * <p>Provides fluent API for configuring various aspects of the weather client
 * including caching, HTTP behavior, localization, and update strategies.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * WeatherSdkConfigurer config = new WeatherSdkConfigurer("api-key")
 *     .updateMode(UpdateMode.POLLING)
 *     .pollingInterval(Duration.ofMinutes(10))
 *     .language(SupportedLanguage.SPANISH)
 *     .units(Units.METRIC)
 *     .loggingEnabled(true)
 *     .cache(cacheCfg -> cacheCfg
 *         .ttl(Duration.ofMinutes(15))
 *         .maxSize(100))
 *     .http(httpCfg -> httpCfg
 *         .connectTimeout(Duration.ofSeconds(5))
 *         .readTimeout(Duration.ofSeconds(10)));
 * }</pre>
 *
 * @see UpdateMode
 * @see SupportedLanguage
 * @see Units
 * @since 1.0
 */
@Accessors(fluent = true)
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public final class WeatherSdkConfigurer {

    /**
     * Defines strategies for updating weather data in the SDK.
     */
    public enum UpdateMode {

        /**
         * Data is fetched on-demand when explicitly requested.
         * More efficient for infrequent access patterns.
         */
        ON_DEMAND,

        /**
         * Data is automatically fetched at regular intervals.
         * Better for applications requiring frequent, fresh data.
         */
        POLLING;
    }

    /**
     * OpenWeather API key for authentication (required).
     * Excluded from toString for security reasons.
     */
    @ToString.Exclude
    private final String apiKey;

    /**
     * Weather data update strategy.
     *
     * <p><b>Default:</b> {@link UpdateMode#ON_DEMAND}</p>
     *
     * @see UpdateMode
     */
    private UpdateMode updateMode = UpdateMode.ON_DEMAND;

    /**
     * Interval for automatic data polling in POLLING mode.
     *
     * <p>Ignored when {@link #updateMode} is {@link UpdateMode#ON_DEMAND}.</p>
     *
     * <p><b>Default:</b> 5 minutes</p>
     */
    private Duration pollingInterval = Duration.ofMinutes(5);

    /**
     * Language for weather descriptions and messages.
     *
     * <p><b>Default:</b> {@link SupportedLanguage#ENGLISH}</p>
     *
     * @see SupportedLanguage
     */
    private SupportedLanguage language = SupportedLanguage.ENGLISH;

    /**
     * Measurement units system for weather data.
     *
     * <p><b>Default:</b> {@link Units#METRIC}</p>
     *
     * @see Units
     */
    private Units units = Units.METRIC;

    /**
     * Enable debug and informational logging for SDK operations.
     *
     * <p><b>Default:</b> false</p>
     */
    private boolean loggingEnabled = false;

    /**
     * Use shared cache instance across multiple clients.
     *
     * <p>When true, all clients with this setting will share the same cache instance.
     * Cannot be used with {@link UpdateMode#POLLING} mode.</p>
     *
     * <p><b>Default:</b> true</p>
     */
    private boolean sharedCache = true;

    /**
     * Use shared HTTP client instance across multiple clients.
     *
     * <p>When true, all clients with this setting will share the same HTTP client,
     * optimizing resource usage and connection pooling.</p>
     *
     * <p><b>Default:</b> true</p>
     */
    private boolean sharedClient = true;

    /**
     * Cache-specific configuration options.
     */
    private final CacheConfigurer cacheConfigurer = new CacheConfigurer();

    /**
     * HTTP client-specific configuration options.
     */
    private final HttpClientConfigurer httpClientConfigurer = new HttpClientConfigurer();

    /**
     * Customize cache configuration using fluent API.
     *
     * @param customizer consumer that modifies the cache configuration
     * @return this configurer instance for method chaining
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * .cache(cfg -> cfg
     *     .ttl(Duration.ofMinutes(30))
     *     .maxSize(500))
     * }</pre>
     */
    public WeatherSdkConfigurer cache(Consumer<CacheConfigurer> customizer) {
        if (customizer != null) {
            customizer.accept(cacheConfigurer);
        }
        return this;
    }

    /**
     * Customize HTTP client configuration using fluent API.
     *
     * @param customizer consumer that modifies the HTTP client configuration
     * @return this configurer instance for method chaining
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * .http(cfg -> cfg
     *     .connectTimeout(Duration.ofSeconds(3))
     *     .readTimeout(Duration.ofSeconds(15)))
     * }</pre>
     */
    public WeatherSdkConfigurer http(Consumer<HttpClientConfigurer> customizer) {
        if (customizer != null) {
            customizer.accept(httpClientConfigurer);
        }
        return this;
    }


    /**
     * Cache configuration options for weather data caching.
     *
     * <p>Controls how weather data is cached to reduce API calls and improve performance.</p>
     */
    @Accessors(fluent = true)
    @Getter
    @Setter
    @ToString
    public static class CacheConfigurer {

        /**
         * Time-to-live for cached weather data entries.
         *
         * <p>Determines how long weather data remains valid in cache before being evicted.
         * Shorter TTL provides fresher data but increases API usage.</p>
         *
         * <p><b>Default:</b> 10 minutes</p>
         */
        private Duration ttl = Duration.ofMinutes(10);

        /**
         * Maximum number of entries in the cache.
         *
         * <p>When cache reaches this size, least recently used entries are evicted.
         * Higher values use more memory but cache more locations.</p>
         *
         * <p><b>Default:</b> 10 entries</p>
         */
        private long maxSize = 10;

    }

    /**
     * HTTP client configuration options for network communication.
     *
     * <p>Controls HTTP client behavior including timeouts, concurrency, and threading.</p>
     */
    @Accessors(fluent = true)
    @Getter
    @Setter
    @ToString
    public static class HttpClientConfigurer {

        /**
         * Connection establishment timeout.
         *
         * <p><b>Default:</b> 10 seconds</p>
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
         * Socket read timeout for data transfer.
         *
         * <p><b>Default:</b> 10 seconds</p>
         */
        private Duration readTimeout = Duration.ofSeconds(10);

        /**
         * Socket write timeout for data transfer.
         *
         * <p><b>Default:</b> 10 seconds</p>
         */
        private Duration writeTimeout = Duration.ofSeconds(10);

        /**
         * Complete call timeout from connect to finish.
         *
         * <p><b>Default:</b> 10 seconds</p>
         */
        private Duration callTimeout = Duration.ofSeconds(10);

        /**
         * Maximum number of concurrent HTTP requests.
         *
         * <p><b>Default:</b> 64 requests</p>
         */
        private int maxConcurrentRequests = 64;

        /**
         * Custom executor service for async HTTP operations.
         *
         * <p>If null, OkHttp's default executor will be used.</p>
         *
         * <p><b>Default:</b> null (use default)</p>
         */
        private ExecutorService executor;

    }

}
