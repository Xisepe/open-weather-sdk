package ru.golubev.openweathersdk.internal;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import ru.golubev.openweathersdk.WeatherSdkConfigurer.UpdateMode;
import ru.golubev.openweathersdk.SupportedLanguage;
import ru.golubev.openweathersdk.Units;
import ru.golubev.openweathersdk.model.WeatherResponse;

/**
 * Weather client implementation with automatic background polling for cached cities.
 *
 * <p>Extends {@link WeatherClientImpl} to provide automatic refresh of cached
 * weather data at regular intervals. This ensures frequently accessed weather
 * data remains fresh without explicit API calls.</p>
 *
 * <p><b>Polling behavior:</b></p>
 * <ul>
 *   <li>Automatically starts polling after construction</li>
 *   <li>Polls all cities currently in the cache</li>
 *   <li>Uses configured polling interval</li>
 *   <li>Runs async API calls to avoid blocking</li>
 *   <li>Automatically stops polling on close()</li>
 * </ul>
 *
 * <p><b>Use cases:</b></p>
 * <ul>
 *   <li>Applications requiring fresh data without manual refresh</li>
 *   <li>Dashboards displaying multiple locations</li>
 *   <li>Real-time monitoring systems</li>
 * </ul>
 *
 * @see WeatherClientImpl
 * @see UpdateMode#POLLING
 * @since 1.0
 */
@Slf4j
public class PollingWeatherClientImpl extends WeatherClientImpl {

    /**
     * Interval between automatic polling cycles.
     */
    private final Duration pollingInterval;

    /**
     * Scheduled executor for managing polling tasks.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructs a polling weather client with specified configuration.
     *
     * @param loggingEnabled enables operation logging
     * @param language language for weather descriptions
     * @param units measurement units system
     * @param isCacheShared indicates if cache is shared with other clients
     * @param responseCache cache instance for weather data
     * @param weatherApi OpenWeather API client
     * @param pollingInterval interval between polling cycles
     */
    public PollingWeatherClientImpl(boolean loggingEnabled,
                                    SupportedLanguage language,
                                    Units units,
                                    boolean isCacheShared,
                                    Cache<String, WeatherResponse> responseCache,
                                    OpenWeatherApi weatherApi,
                                    Duration pollingInterval) {
        super(loggingEnabled, language, units, isCacheShared, responseCache, weatherApi);
        this.pollingInterval = pollingInterval;
        startPolling();
    }

    /**
     * Performs graceful shutdown of polling client.
     *
     * <p>Stops polling scheduler and performs base client cleanup
     * via {@link WeatherClientImpl#close()}.</p>
     */
    @Override
    public void close() {
        super.close();
        stopPolling();
    }

    /**
     * Starts the background polling scheduler.
     *
     * <p>Schedules the first polling task to run after the configured interval.
     * Subsequent polls are scheduled by the poll() method itself.</p>
     */
    private void startPolling() {
        if (loggingEnabled) {
            log.info("PollingWeatherClientImpl start polling with pollingInterval = {}", pollingInterval);
        }
        scheduler.schedule(this::poll, pollingInterval.toSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Executes a single polling cycle for all cached cities.
     *
     * <p><b>Polling process:</b></p>
     * <ol>
     *   <li>Retrieves all cities currently in cache</li>
     *   <li>Initiates async API call for each city</li>
     *   <li>Updates cache with fresh responses</li>
     *   <li>Schedules next polling cycle</li>
     * </ol>
     *
     * <p><b>Note:</b> Uses async API calls to avoid blocking the polling thread
     * and allow parallel data refresh for multiple cities.</p>
     */
    private void poll() {
        if (loggingEnabled) {
            log.debug("PollingWeatherClientImpl execute poll.");
        }

        getCachedCities().forEach(city -> {
            if  (loggingEnabled) {
                log.debug("PollingWeatherClientImpl start polling for city = {}", city);
            }

            getWeatherAsync(city).whenComplete((weatherResponse, throwable) -> {
                if (weatherResponse != null && loggingEnabled) {
                    log.debug("PollingWeatherClientImpl finished polling city = {}", city);
                }
            });
        });

        // Schedule next polling cycle
        scheduler.schedule(this::poll, pollingInterval.toSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Stops the background polling scheduler.
     *
     * <p>Initiates graceful shutdown of the scheduler, allowing running
     * tasks to complete but preventing new tasks from starting.</p>
     */
    private void stopPolling() {
        if (loggingEnabled) {
            log.info("PollingWeatherClientImpl stop polling.");
        }

        scheduler.shutdown();
    }
}