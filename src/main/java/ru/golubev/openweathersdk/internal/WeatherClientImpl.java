package ru.golubev.openweathersdk.internal;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.golubev.openweathersdk.WeatherClient;
import ru.golubev.openweathersdk.SupportedLanguage;
import ru.golubev.openweathersdk.Units;
import ru.golubev.openweathersdk.model.WeatherResponse;

/**
 * Default implementation of {@link WeatherClient} with caching support.
 *
 * <p>Provides synchronous and asynchronous weather data retrieval with transparent
 * caching to reduce API calls and improve performance. Implements the core
 * weather client functionality used by both on-demand and polling modes.</p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Transparent response caching with configurable TTL</li>
 *   <li>Thread-safe cache operations</li>
 *   <li>Support for both sync and async data retrieval</li>
 *   <li>Configurable language and units</li>
 *   <li>Graceful resource cleanup</li>
 * </ul>
 *
 * <p><b>Cache behavior:</b></p>
 * <ul>
 *   <li>Cache hit: returns immediately from local cache</li>
 *   <li>Cache miss: calls OpenWeather API and caches response</li>
 *   <li>Cache keys: city names (case-sensitive)</li>
 *   <li>Eviction: based on TTL and LRU policy</li>
 * </ul>
 *
 * @see PollingWeatherClientImpl
 * @see WeatherClient
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class WeatherClientImpl implements WeatherClient, AutoCloseable {

    /**
     * Enables debug and informational logging for client operations.
     */
    protected final boolean loggingEnabled;

    /**
     * Language for weather descriptions in API responses.
     */
    private final SupportedLanguage language;

    /**
     * Measurement units system for weather data.
     */
    private final Units units;

    /**
     * Indicates whether the cache instance is shared with other clients.
     * When true, cache invalidation is skipped during cleanup.
     */
    private final boolean isCacheShared;

    /**
     * Cache instance for storing weather responses by city name.
     */
    private final Cache<String, WeatherResponse> responseCache;

    /**
     * Underlying API client for OpenWeather service communication.
     */
    private final OpenWeatherApi weatherApi;

    /**
     * Retrieves current weather data for specified city with caching.
     *
     * <p><b>Execution flow:</b></p>
     * <ol>
     *   <li>Check cache for existing response</li>
     *   <li>If cache hit, return cached response immediately</li>
     *   <li>If cache miss, call OpenWeather API</li>
     *   <li>Cache API response for future requests</li>
     *   <li>Return weather data</li>
     * </ol>
     *
     * @param city the city name for weather lookup
     * @return current weather data for the specified city
     *
     * <p><b>Logging examples:</b></p>
     * <pre>
     * DEBUG - WeatherClient#getWeather called with city: London
     * DEBUG - Cache hit for city: London
     * DEBUG - Cache miss. Calling remote API for city: Paris
     * DEBUG - Response value for city: Paris
     * </pre>
     */
    @Override
    public WeatherResponse getWeather(String city) {
        if (loggingEnabled) {
            log.debug("WeatherClient#getWeather called with city: {}", city);
        }

        WeatherResponse response = responseCache.getIfPresent(city);

        if (response != null) {
            if (loggingEnabled) {
                log.debug("Cache hit for city: {}", city);
            }
            return response;
        }

        if (loggingEnabled) {
            log.debug("Cache miss. Calling remote API for city: {}", city);
        }

        response = weatherApi.weather(city, language, units);
        responseCache.put(city, response);

        if (loggingEnabled) {
            log.debug("Response value for city: {}", city);
        }
        return response;
    }

    /**
     * Retrieves current weather data asynchronously with caching support.
     *
     * <p><b>Execution flow:</b></p>
     * <ol>
     *   <li>Check cache for existing response</li>
     *   <li>If cache hit, return completed future with cached data</li>
     *   <li>If cache miss, initiate async API call</li>
     *   <li>Cache response when async call completes</li>
     *   <li>Return CompletableFuture for the operation</li>
     * </ol>
     *
     * @param city the city name for weather lookup
     * @return CompletableFuture that will be completed with weather data
     *
     * <p><b>Note:</b> Currently uses hardcoded RUSSIAN language and METRIC units
     * for async calls - this should be fixed to use configured language/units.</p>
     */
    @Override
    public CompletableFuture<WeatherResponse> getWeatherAsync(String city) {
        if (loggingEnabled) {
            log.debug("WeatherClient#getWeatherAsync called with city: {}", city);
        }

        WeatherResponse response = responseCache.getIfPresent(city);

        if (response != null) {
            if (loggingEnabled) {
                log.debug("Cache hit for city: {}", city);
            }

            return CompletableFuture.completedFuture(response);
        }

        if (loggingEnabled) {
            log.debug("Cache miss. Calling remote API for city: {}", city);
        }

        return weatherApi.weatherAsync(city, language, units)
            .whenComplete((r, e) -> {
                if (loggingEnabled) {
                    log.debug("Async weather call complete for  city: {}", city);
                }

                if (r != null) {
                    responseCache.put(city, r);
                    if (loggingEnabled) {
                        log.debug("Response value for city: {}", city);
                    }
                }
            });
    }

    /**
     * Returns set of city names currently in the cache.
     *
     * @return set of cached city names
     *
     * <p><b>Usage:</b> Primarily used by {@link PollingWeatherClientImpl} to
     * determine which cities to refresh during polling cycles.</p>
     */
    protected Set<String> getCachedCities() {
        return responseCache.asMap().keySet();
    }

    /**
     * Performs graceful cleanup of client resources.
     *
     * <p><b>Cleanup steps:</b></p>
     * <ul>
     *   <li>Invalidates cache if it's not shared</li>
     *   <li>Shuts down underlying API client</li>
     *   <li>Logs cleanup progress if logging enabled</li>
     * </ul>
     *
     * <p><b>Note:</b> Shared caches are not invalidated to avoid affecting
     * other clients using the same cache instance.</p>
     */
    @Override
    public void close() {
        if (loggingEnabled) {
            log.info("Closing WeatherClient.");
        }

        if (!isCacheShared) {
            responseCache.invalidateAll();
            if (loggingEnabled) {
                log.debug("WeatherClient cache is cleared.");
            }
        }

        if (loggingEnabled) {
            log.debug("Closing OpenWeatherApi.");
        }
        weatherApi.shutdown();
    }
}