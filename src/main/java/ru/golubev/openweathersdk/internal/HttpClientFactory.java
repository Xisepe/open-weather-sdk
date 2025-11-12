package ru.golubev.openweathersdk.internal;

import java.util.Optional;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.HttpClientConfigurer;

/**
 * Factory class for creating configured HTTP client instances.
 *
 * <p>Centralizes HTTP client creation with consistent timeout and concurrency
 * configuration. Supports custom executors and connection pooling.</p>
 *
 * <p><b>Client features:</b></p>
 * <ul>
 *   <li>Configurable timeouts (connect, read, write, call)</li>
 *   <li>Connection pooling and reuse</li>
 *   <li>Concurrent request limiting</li>
 *   <li>Custom executor support</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * HttpClientConfigurer config = new HttpClientConfigurer()
 *     .connectTimeout(Duration.ofSeconds(5))
 *     .readTimeout(Duration.ofSeconds(10))
 *     .maxConcurrentRequests(32);
 *
 * OkHttpClient client = HttpClientFactory.create(config);
 * }</pre>
 *
 * @see HttpClientConfigurer
 * @see OkHttpClient
 * @since 1.0
 */
class HttpClientFactory {

    /**
     * Creates a configured HTTP client instance based on the provided configuration.
     *
     * @param configurer the HTTP client configuration specifying timeouts and concurrency
     * @return configured OkHttpClient instance ready for use
     * @throws NullPointerException if configurer is null
     *
     * <p><b>Configuration applied:</b></p>
     * <ul>
     *   <li>{@code connectTimeout} - connection establishment timeout</li>
     *   <li>{@code readTimeout} - socket read timeout</li>
     *   <li>{@code writeTimeout} - socket write timeout</li>
     *   <li>{@code callTimeout} - complete call timeout</li>
     *   <li>{@code maxConcurrentRequests} - limits concurrent requests</li>
     *   <li>{@code executor} - custom executor for async operations (optional)</li>
     * </ul>
     *
     * <p><b>Dispatcher configuration:</b></p>
     * <pre>{@code
     * // Both maxRequests and maxRequestsPerHost are set to
     * // the same value from maxConcurrentRequests
     * dispatcher.setMaxRequests(config.maxConcurrentRequests());
     * dispatcher.setMaxRequestsPerHost(config.maxConcurrentRequests());
     * }</pre>
     *
     * <p><b>Default behavior with standard config:</b></p>
     * <pre>{@code
     * // Creates client with:
     * // - 10 second timeouts for all operations
     * // - 64 concurrent request limit
     * // - Default executor service (Executors.newCachedThreadPool())
     * HttpClientFactory.create(new HttpClientConfigurer());
     * }</pre>
     */
    public static OkHttpClient create(HttpClientConfigurer configurer) {
        Dispatcher dispatcher = Optional.ofNullable(configurer.executor())
            .map(Dispatcher::new)
            .orElse(new Dispatcher());

        dispatcher.setMaxRequests(configurer.maxConcurrentRequests());
        dispatcher.setMaxRequestsPerHost(configurer.maxConcurrentRequests());

        return new OkHttpClient.Builder()
                .connectTimeout(configurer.connectTimeout())
                .readTimeout(configurer.readTimeout())
                .writeTimeout(configurer.writeTimeout())
                .callTimeout(configurer.callTimeout())
                .dispatcher(dispatcher)
                .build();
    }
}