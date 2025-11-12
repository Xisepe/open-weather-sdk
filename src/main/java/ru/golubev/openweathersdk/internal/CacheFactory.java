package ru.golubev.openweathersdk.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.CacheConfigurer;

/**
 * Factory class for creating configured cache instances.
 *
 * <p>Provides centralized cache creation with consistent configuration
 * using Caffeine caching library. Creates time-based and size-bound caches
 * suitable for weather data storage.</p>
 *
 * <p><b>Cache characteristics:</b></p>
 * <ul>
 *   <li>Time-based expiration (write TTL)</li>
 *   <li>Size-bound eviction (LRU policy)</li>
 *   <li>Thread-safe operations</li>
 *   <li>High performance concurrent access</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * CacheConfigurer config = new CacheConfigurer()
 *     .ttl(Duration.ofMinutes(15))
 *     .maxSize(100);
 *
 * Cache<String, WeatherResponse> cache = CacheFactory.create(config);
 * }</pre>
 *
 * @see CacheConfigurer
 * @see Caffeine
 * @since 1.0
 */
class CacheFactory {

    /**
     * Creates a configured cache instance based on the provided configuration.
     *
     * @param <K> the type of cache keys (typically String for city names)
     * @param <V> the type of cache values (typically WeatherResponse)
     * @param configurer the cache configuration specifying TTL and size limits
     * @return configured Cache instance ready for use
     * @throws NullPointerException if configurer is null
     *
     * <p><b>Configuration applied:</b></p>
     * <ul>
     *   <li>{@code expireAfterWrite(configurer.ttl())} - entries expire after TTL</li>
     *   <li>{@code maximumSize(configurer.maxSize())} - LRU eviction at max size</li>
     * </ul>
     *
     * <p><b>Default behavior with standard config:</b></p>
     * <pre>{@code
     * // Creates cache that:
     * // - Expires entries 10 minutes after write
     * // - Holds maximum 10 entries
     * // - Uses LRU eviction when full
     * CacheFactory.create(new CacheConfigurer());
     * }</pre>
     */
    public static <K, V> Cache<K, V> create(CacheConfigurer configurer) {
        return Caffeine.newBuilder()
                .expireAfterWrite(configurer.ttl())
                .maximumSize(configurer.maxSize())
                .build();
    }
}