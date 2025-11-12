package ru.golubev.openweathersdk.internal;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import ru.golubev.openweathersdk.SupportedLanguage;
import ru.golubev.openweathersdk.Units;
import ru.golubev.openweathersdk.WeatherClient;
import ru.golubev.openweathersdk.WeatherSdk;
import ru.golubev.openweathersdk.WeatherSdkConfigurer;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.CacheConfigurer;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.HttpClientConfigurer;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.UpdateMode;
import ru.golubev.openweathersdk.model.WeatherResponse;

/**
 * Main entry point for OpenWeather SDK - singleton factory for creating and managing weather clients.
 *
 * <p>Provides centralized management of weather API clients with shared resources, caching,
 * and lifecycle management. Implements singleton pattern to ensure consistent configuration
 * across the application.</p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Singleton instance management</li>
 *   <li>Shared HTTP client and cache resources</li>
 *   <li>Multiple API key support</li>
 *   <li>Thread-safe client creation and access</li>
 *   <li>Graceful shutdown and resource cleanup</li>
 *   <li>Configurable caching strategies</li>
 *   <li>Support for both on-demand and polling modes</li>
 * </ul>
 *
 * <p><b>Basic usage pattern:</b></p>
 * <pre>{@code
 * // Get SDK instance
 * OpenWeatherSdk sdk = OpenWeatherSdk.getInstance();
 *
 * // Configure shared resources (optional)
 * sdk.sharedClient(httpCfg -> httpCfg
 *     .connectTimeout(Duration.ofSeconds(5))
 *     .readTimeout(Duration.ofSeconds(10)));
 *
 * // Create weather client
 * WeatherClient client = sdk.build("your-api-key", cfg -> cfg
 *     .language(SupportedLanguage.ENGLISH)
 *     .units(Units.METRIC)
 *     .loggingEnabled(true));
 *
 * // Use client
 * WeatherResponse weather = client.current().byCityName("London");
 *
 * // Shutdown when done
 * sdk.close();
 * }</pre>
 *
 * <p><b>Resource management:</b></p>
 * <ul>
 *   <li>Shared HTTP client - connection pooling and reuse</li>
 *   <li>Shared cache - reduced memory footprint for multiple clients</li>
 *   <li>Automatic cleanup on shutdown</li>
 *   <li>Thread-safe concurrent access</li>
 * </ul>
 *
 * @see WeatherClient
 * @see WeatherSdkConfigurer
 * @see UpdateMode
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenWeatherSdk implements WeatherSdk {

    /**
     * Default maximum cache size for shared cache instances.
     */
    public static final int DEFAULT_CACHE_SIZE = 50;

    /**
     * Default time-to-live for cached weather data.
     */
    public static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(15);

    /**
     * Registry of created clients keyed by API key.
     * Provides thread-safe access to all active clients.
     */
    private final ConcurrentMap<String, WeatherClientImpl> clients = new ConcurrentHashMap<>();

    /**
     * Atomic flag indicating SDK shutdown state.
     * Prevents operations after shutdown and ensures thread-safe state transitions.
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * Lock for synchronizing shared resource initialization and modification.
     * Ensures consistent state during concurrent access.
     */
    private final Lock updateLock = new ReentrantLock();

    /**
     * Shared HTTP client instance for connection pooling and resource optimization.
     * Initialized lazily on first use or via explicit configuration.
     */
    private OkHttpClient sharedHttpClient;

    /**
     * Shared cache instance for weather data across multiple clients.
     * Reduces memory footprint and improves data consistency.
     */
    private Cache<String, WeatherResponse> sharedCache;

    /**
     * Singleton holder implementing lazy initialization pattern.
     * Ensures thread-safe singleton creation without synchronization overhead.
     */
    private static final class SingletonHolder {

        private static final OpenWeatherSdk INSTANCE = new OpenWeatherSdk();
    }


    /**
     * Returns the singleton instance of OpenWeather SDK.
     *
     * @return the singleton SDK instance
     *
     * <p><b>Thread safety:</b> This method is thread-safe and guaranteed to return
     * the same instance across all threads.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * OpenWeatherSdk sdk = OpenWeatherSdk.getInstance();
     * }</pre>
     */
    public static OpenWeatherSdk getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Verifies that the SDK has not been shut down.
     *
     * @throws IllegalStateException if the SDK has been shut down
     */
    private void ensureNotShutdown() {
        if (shutdown.get()) {
            log.warn("OpenWeatherSdk has been shut down");
            throw new IllegalStateException("OpenWeatherSdk has been shut down and cannot be used anymore.");
        }
    }

    /**
     * Configures the shared HTTP client for all subsequently created weather clients.
     *
     * <p>This method must be called before creating any clients. The shared client
     * enables connection pooling and optimal resource usage across multiple clients.</p>
     *
     * @param configurer consumer that customizes HTTP client configuration
     * @return this SDK instance for method chaining
     * @throws IllegalStateException if SDK is shut down or shared client already initialized
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * sdk.sharedClient(cfg -> cfg
     *     .connectTimeout(Duration.ofSeconds(3))
     *     .readTimeout(Duration.ofSeconds(15))
     *     .maxConcurrentRequests(32));
     * }</pre>
     *
     * <p><b>Note:</b> Can only be called once per SDK instance.</p>
     */
    @Override
    public OpenWeatherSdk sharedClient(Consumer<HttpClientConfigurer> configurer) {
        ensureNotShutdown();
        updateLock.lock();
        try {
            if (sharedHttpClient != null) {
                log.error("OpenWeatherSdk shared http client has already been initialized");
                throw new IllegalStateException("sharedClient has already been initialized");
            }
            HttpClientConfigurer config = new HttpClientConfigurer();
            configurer.accept(config);
            sharedHttpClient = buildClient(config);

            log.debug("OpenWeatherSdk shared http client has been initialized.");

            return this;
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Gets the shared HTTP client, initializing with defaults if not already configured.
     *
     * @return the shared HTTP client instance
     */
    private OkHttpClient getSharedClientOrDefault() {
        ensureNotShutdown();
        updateLock.lock();
        try {
            if (sharedHttpClient == null) {
                log.debug("OpenWeatherSdk building default shared http client.");
                sharedHttpClient = buildClient(new HttpClientConfigurer());
            }
            return sharedHttpClient;
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Configures the shared cache for all subsequently created weather clients.
     *
     * <p>The shared cache reduces memory usage and improves data consistency
     * when multiple clients access the same location data.</p>
     *
     * @param configurer consumer that customizes cache configuration
     * @return this SDK instance for method chaining
     * @throws IllegalStateException if SDK is shut down or shared cache already initialized
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * sdk.sharedCache(cfg -> cfg
     *     .ttl(Duration.ofMinutes(30))
     *     .maxSize(200));
     * }</pre>
     *
     * <p><b>Restriction:</b> Shared cache cannot be used with {@link UpdateMode#POLLING} mode.</p>
     */
    @Override
    public OpenWeatherSdk sharedCache(Consumer<CacheConfigurer> configurer) {
        ensureNotShutdown();
        updateLock.lock();
        try {
            if (sharedCache != null) {
                log.error("OpenWeatherSdk shared cache has already been initialized");
                throw new IllegalStateException("sharedCache has already been initialized");
            }
            CacheConfigurer config = new CacheConfigurer();
            configurer.accept(config);
            sharedCache = buildCache(config);

            log.debug("OpenWeatherSdk shared cache has been initialized.");

            return this;
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Gets the shared cache, initializing with defaults if not already configured.
     *
     * @return the shared cache instance
     */
    private Cache<String, WeatherResponse> getSharedCacheOrDefault() {
        ensureNotShutdown();
        updateLock.lock();
        try {
            if (sharedCache == null) {
                log.debug("OpenWeatherSdk building default shared cache.");
                sharedCache = buildCache(new CacheConfigurer()
                                             .maxSize(DEFAULT_CACHE_SIZE)
                                             .ttl(DEFAULT_CACHE_TTL));
            }
            return sharedCache;
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Creates a new weather client with the specified API key and configuration.
     *
     * @param apiKey the OpenWeather API key (required)
     * @param customizer consumer that customizes the client configuration
     * @return configured weather client instance
     * @throws IllegalStateException if SDK is shut down
     * @throws NullPointerException if apiKey is null
     * @throws IllegalArgumentException if client with this API key already exists
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * WeatherClient client = sdk.build("api-key", cfg -> cfg
     *     .updateMode(UpdateMode.POLLING)
     *     .pollingInterval(Duration.ofMinutes(10))
     *     .language(SupportedLanguage.SPANISH)
     *     .units(Units.METRIC)
     *     .loggingEnabled(true));
     * }</pre>
     *
     * <p><b>Note:</b> Each API key can only be used to create one client instance.</p>
     */
    @Override
    public WeatherClientImpl build(String apiKey, Consumer<WeatherSdkConfigurer> customizer) {
        ensureNotShutdown();
        Objects.requireNonNull(apiKey, "API key cannot be null");

        log.debug("OpenWeatherSdk building client for apiKey {}", apiKey.substring(3) + "*****");

        if (clients.containsKey(apiKey)) {
            log.error("API key has already been initialized");
            throw new IllegalArgumentException("Client with this API key already exists");
        }

        WeatherSdkConfigurer configurer = new WeatherSdkConfigurer(apiKey);
        customizer.accept(configurer);

        WeatherClientImpl sdk = build(configurer);
        clients.put(apiKey, sdk);

        log.debug("OpenWeatherSdk successfully built client.");
        return sdk;
    }

    /**
     * Creates a new weather client with the specified API key and default configuration.
     *
     * @param apiKey the OpenWeather API key (required)
     * @return configured weather client instance with default settings
     * @throws IllegalStateException if SDK is shut down
     * @throws NullPointerException if apiKey is null
     * @throws IllegalArgumentException if client with this API key already exists
     *
     * <p><b>Default configuration:</b></p>
     * <ul>
     *   <li>Update mode: {@link UpdateMode#ON_DEMAND}</li>
     *   <li>Language: {@link SupportedLanguage#ENGLISH}</li>
     *   <li>Units: {@link Units#METRIC}</li>
     *   <li>Logging: disabled</li>
     *   <li>Shared cache: enabled</li>
     *   <li>Shared client: enabled</li>
     * </ul>
     */
    @Override
    public WeatherClientImpl build(String apiKey) {
        return build(apiKey, cfg -> {});
    }

    /**
     * Internal method to build weather client from configuration.
     *
     * @param cfg the weather SDK configuration
     * @return configured weather client instance
     * @throws IllegalArgumentException if shared cache is used with polling mode
     */
    private WeatherClientImpl build(WeatherSdkConfigurer cfg) {
        ensureNotShutdown();

        log.debug("OpenWeatherSdk built with configurer {}", cfg);

        if (cfg.sharedCache() && cfg.updateMode() == UpdateMode.POLLING) {
            log.error("Cannot use shared cache with client in POLLING mode due to implementation.");
            throw new IllegalArgumentException("Cannot use shared cache with client in POLLING mode due to implementation.");
        }

        // --- Cache setup ---
        Cache<String, WeatherResponse> cache = cfg.sharedCache()
            ? getSharedCacheOrDefault()
            : CacheFactory.create(cfg.cacheConfigurer());

        // --- HTTP client setup ---
        OkHttpClient httpClient = cfg.sharedClient()
            ? getSharedClientOrDefault()
            : HttpClientFactory.create(cfg.httpClientConfigurer());

        // --- OpenWeatherApi setup ---
        OpenWeatherApi openWeatherApi = new OpenWeatherApi(
            cfg.apiKey(),
            cfg.loggingEnabled(),
            httpClient,
            cfg.sharedClient()
        );

        // --- Client type selection ---
        if (cfg.updateMode() == UpdateMode.POLLING) {
            return new PollingWeatherClientImpl(
                cfg.loggingEnabled(),
                cfg.language(),
                cfg.units(),
                cfg.sharedCache(),
                cache,
                openWeatherApi,
                cfg.pollingInterval()
            );
        }

        return new WeatherClientImpl(
            cfg.loggingEnabled(),
            cfg.language(),
            cfg.units(),
            cfg.sharedCache(),
            cache,
            openWeatherApi
        );
    }

    /**
     * Builds HTTP client from configuration.
     */
    private OkHttpClient buildClient(HttpClientConfigurer configurer) {
        if (configurer == null) {
            log.error("configurer is null");
            throw new IllegalStateException("configurer is null");
        }
        return HttpClientFactory.create(configurer);
    }

    /**
     * Builds cache from configuration.
     */
    private Cache<String, WeatherResponse> buildCache(CacheConfigurer configurer) {
        if (configurer == null) {
            log.error("configurer is null");
            throw new IllegalStateException("configurer is null");
        }
        return CacheFactory.create(configurer);
    }

    /**
     * Retrieves a previously created weather client by API key.
     *
     * @param apiKey the API key used to create the client
     * @return the weather client instance, or null if no client exists for the key
     * @throws IllegalStateException if SDK is shut down
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * WeatherClient client = sdk.client("api-key");
     * if (client != null) {
     *     WeatherResponse weather = client.current().byCityName("Paris");
     * }
     * }</pre>
     */
    @Override
    public WeatherClientImpl client(String apiKey) {
        ensureNotShutdown();
        return clients.get(apiKey);
    }

    /**
     * Removes and shuts down a weather client.
     *
     * @param apiKey the API key of the client to remove
     * @throws IllegalStateException if SDK is shut down
     *
     * <p><b>Note:</b> This method performs graceful shutdown of the client
     * including cache cleanup and API connection termination.</p>
     */
    @Override
    public void deleteClient(String apiKey) {
        ensureNotShutdown();

        log.debug("OpenWeatherSdk deleting client with API key {}", apiKey.substring(3) + "*****");

        WeatherClientImpl client = clients.remove(apiKey);
        if (client != null) {
            client.close();
        }
    }

    /**
     * Performs complete shutdown of the SDK and all associated resources.
     *
     * <p>This method should be called when the SDK is no longer needed, typically
     * during application shutdown. It performs graceful cleanup of all resources:</p>
     *
     * <ul>
     *   <li>Shuts down all created weather clients</li>
     *   <li>Clears client registry</li>
     *   <li>Shuts down shared HTTP client</li>
     *   <li>Clears and invalidates shared cache</li>
     * </ul>
     *
     * <p><b>Idempotent:</b> Multiple calls have no additional effect after the first successful call.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Application shutdown hook
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     *     OpenWeatherSdk.getInstance().close();
     * }));
     *
     * // Or explicit shutdown
     * try {
     *     // Use SDK...
     * } finally {
     *     OpenWeatherSdk.getInstance().close();
     * }
     * }</pre>
     *
     * <p><b>Note:</b> After shutdown, all SDK operations will throw IllegalStateException.</p>
     */
    @Override
    public void close() {
        if (!shutdown.compareAndSet(false, true)) {
            log.warn("OpenWeatherSdk shutdown() called again — already shut down");
            return;
        }

        updateLock.lock();
        try {
            log.info("Shutting down OpenWeatherSdk...");

            // Gracefully shutdown all clients
            clients.values().forEach(WeatherClientImpl::close);
            clients.clear();

            // Shutdown shared resources
            if (sharedHttpClient != null) {
                log.debug("OpenWeatherSdk clearing shared http client.");

                HttpClientShutdown.shutdown(sharedHttpClient, true);
                sharedHttpClient = null;

                log.debug("OpenWeatherSdk clearing http client.");
            }

            if (sharedCache != null) {
                log.debug("OpenWeatherSdk clearing shared cache.");

                sharedCache.invalidateAll();
                sharedCache.cleanUp();
                sharedCache = null;

                log.debug("OpenWeatherSdk cleared shared cache.");
            }

            log.info("OpenWeatherSdk successfully shut down");
        } finally {
            updateLock.unlock();
        }
    }
}
