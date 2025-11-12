package ru.golubev.openweathersdk.exception;

/**
 * Exception indicating that an HTTP request to OpenWeather API was explicitly cancelled.
 *
 * <p>This exception is thrown when a weather API request is cancelled before completion,
 * either by explicit user action or system-initiated cancellation. It extends
 * {@link OpenWeatherApiClientException} to distinguish cancellation from other
 * client-side errors.</p>
 *
 * <p><b>Common cancellation scenarios:</b></p>
 * <ul>
 *   <li>Explicit call to {@code CompletableFuture.cancel()}</li>
 *   <li>HTTP client timeout configuration</li>
 *   <li>Application shutdown during in-flight requests</li>
 *   <li>User-initiated cancellation in UI applications</li>
 *   <li>Circuit breaker patterns interrupting long-running requests</li>
 * </ul>
 *
 * <p><b>Exception hierarchy:</b></p>
 * <pre>
 * OpenWeatherApiClientException
 * └── RequestCancellationException (cancelled requests)
 * </pre>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * CompletableFuture<WeatherResponse> future = client.getWeatherAsync("London");
 *
 * // Cancel the request after timeout
 * scheduledExecutor.schedule(() -> future.cancel(true), 5, TimeUnit.SECONDS);
 *
 * try {
 *     WeatherResponse response = future.get();
 * } catch (CancellationException e) {
 *     // Handle cancellation
 * } catch (ExecutionException e) {
 *     if (e.getCause() instanceof RequestCancellationException) {
 *         // Request was explicitly cancelled
 *         log.debug("Weather request cancelled by user");
 *     }
 * }
 * }</pre>
 *
 * @see OpenWeatherApiClientException
 * @see java.util.concurrent.CompletableFuture#cancel(boolean)
 * @since 1.0
 */
public class RequestCancellationException extends OpenWeatherApiClientException {

    /**
     * Constructs a new RequestCancellationException with the underlying cause.
     *
     * @param cause the original exception that caused the cancellation,
     *              typically an IOException or InterruptedException
     */
    public RequestCancellationException(Throwable cause) {
        super("Request was cancelled.", cause);
    }
}