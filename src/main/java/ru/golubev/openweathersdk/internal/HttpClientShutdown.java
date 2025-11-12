package ru.golubev.openweathersdk.internal;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Utility class for graceful shutdown of OkHttpClient instances.
 *
 * <p>Provides proper resource cleanup for HTTP clients including connection pools,
 * dispatchers, and cache. Ensures no resource leaks when application shuts down.</p>
 *
 * <p><b>Shutdown procedure:</b></p>
 * <ol>
 *   <li>Cancel all pending requests</li>
 *   <li>Shutdown dispatcher executor</li>
 *   <li>Evict connection pool</li>
 *   <li>Close response cache (if present)</li>
 * </ol>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Graceful shutdown in application termination
 * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
 *     HttpClientShutdown.shutdown(httpClient, true);
 * }));
 *
 * // Direct shutdown call
 * HttpClientShutdown.shutdown(client, false);
 * }</pre>
 *
 * @see OkHttpClient
 * @since 1.0
 */
@UtilityClass
@Slf4j
class HttpClientShutdown {

    /**
     * Performs graceful shutdown of an OkHttpClient instance.
     *
     * @param client the HTTP client to shutdown
     * @param logging whether to log shutdown progress and warnings
     *
     * <p><b>Shutdown steps:</b></p>
     * <ul>
     *   <li>{@code dispatcher.cancelAll()} - cancels all pending requests</li>
     *   <li>{@code executorService.shutdown()} - shuts down dispatcher executor</li>
     *   <li>{@code connectionPool.evictAll()} - clears connection pool</li>
     *   <li>{@code cache.close()} - closes response cache if present</li>
     * </ul>
     *
     * <p><b>Important notes:</b></p>
     * <ul>
     *   <li>If using shared client, ensure no other components are using it</li>
     *   <li>In-flight requests may be interrupted</li>
     *   <li>Cache closure failures are logged as warnings but don't stop shutdown</li>
     *   <li>Method is idempotent - safe to call multiple times</li>
     * </ul>
     *
     * <p><b>Logging output when enabled:</b></p>
     * <pre>{@code
     * INFO  - Shutting down OkHttpClient client.
     * WARN  - Failed to close OkHttp cache (if cache close fails)
     * INFO  - OkHttpClient is shut down.
     * }</pre>
     */
    public void shutdown(OkHttpClient client, boolean logging) {
        if (logging) {
            log.info("Shutting down OkHttpClient client.");
        }

        Dispatcher dispatcher = client.dispatcher();
        ExecutorService executorService = dispatcher.executorService();

        // Gracefully cancel/finish all running requests
        dispatcher.cancelAll();

        // Shut down executor (if you own it)
        executorService.shutdown();

        // Close the connection pool
        client.connectionPool().evictAll();

        // Close cache if you have one
        if (client.cache() != null) {
            try {
                client.cache().close();
            } catch (IOException e) {
                if (logging) {
                    log.warn("Failed to close OkHttp cache", e);
                }
            }
        }

        if (logging) {
            log.info("OkHttpClient is shut down.");
        }
    }

}
