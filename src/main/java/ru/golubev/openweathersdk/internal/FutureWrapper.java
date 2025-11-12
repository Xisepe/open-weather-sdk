package ru.golubev.openweathersdk.internal;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import ru.golubev.openweathersdk.exception.OpenWeatherApiClientException;
import ru.golubev.openweathersdk.exception.RequestCancellationException;

/**
 * Utility class for wrapping OkHttp {@link Call} objects into {@link CompletableFuture} instances.
 *
 * <p>This class provides asynchronous handling of HTTP requests with proper exception
 * management and logging capabilities. It converts OkHttp's callback-based API into
 * a more modern {@code CompletableFuture} approach suitable for reactive programming.
 * Request completion is done asynchronously in ForkJoinPool.</p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Converts asynchronous OkHttp calls to {@code CompletableFuture}</li>
 *   <li>Provides configurable logging for request/response lifecycle</li>
 *   <li>Handles request cancellation gracefully</li>
 *   <li>Maps different failure types to appropriate exceptions</li>
 *   <li>Ensures proper resource cleanup</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe and stateless, as indicated by
 * the {@link UtilityClass} annotation. All methods can be safely called from multiple threads.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Create an OkHttp call
 * Call call = httpClient.newCall(request);
 *
 * // Wrap it in a CompletableFuture with logging enabled
 * CompletableFuture<Response> future = FutureWrapper.wrap(call, true);
 *
 * // Process the response asynchronously
 * future.thenApply(response -> {
 *     // Handle successful response
 *     return processWeatherData(response);
 * }).exceptionally(throwable -> {
 *     // Handle failures
 *     log.error("Request failed", throwable);
 *     return null;
 * });
 * }</pre>
 *
 * @see Call
 * @see CompletableFuture
 * @see Callback
 * @since 1.0
 */
@Slf4j
@UtilityClass
final class FutureWrapper {

    /**
     * Wraps an OkHttp {@link Call} into a {@link CompletableFuture} with optional logging.
     *
     * <p>This method converts the callback-based OkHttp execution model into a
     * {@code CompletableFuture} that can be used with modern asynchronous programming
     * patterns like chaining, composition, and exception handling.</p>
     *
     * <p><b>Lifecycle events:</b></p>
     * <ul>
     *   <li><b>Successful response:</b> Future completes with the {@link Response} object</li>
     *   <li><b>Network failure:</b> Future completes exceptionally with {@link OpenWeatherApiClientException}</li>
     *   <li><b>Request cancellation:</b> Future completes exceptionally with {@link RequestCancellationException}</li>
     *   <li><b>Future cancellation:</b> Underlying OkHttp call is cancelled automatically</li>
     * </ul>
     *
     * <p><b>Error handling:</b></p>
     * <ul>
     *   <li>{@link RequestCancellationException} - when the call is explicitly cancelled</li>
     *   <li>{@link OpenWeatherApiClientException} - for all other IO failures</li>
     * </ul>
     *
     * @param call the OkHttp call to wrap; must not be {@code null} and should not be executed yet
     * @param loggingEnabled if {@code true}, enables debug and warning logs for request lifecycle events
     * @return a {@link CompletableFuture} that will be completed with the HTTP response or failed with an exception
     * @throws NullPointerException if the {@code call} parameter is {@code null}
     *
     * <p><b>Example with logging enabled:</b></p>
     * <pre>{@code
     * CompletableFuture<Response> future = FutureWrapper.wrap(call, true);
     * // Logs: "Received response from api.openweathermap.org" on success
     * // Logs: "Failed while performing request" on failure with exception details
     * // Logs: "Request was cancelled" on cancellation
     * }</pre>
     *
     * <p><b>Example with logging disabled:</b></p>
     * <pre>{@code
     * CompletableFuture<Response> future = FutureWrapper.wrap(call, false);
     * // No logging output regardless of outcome
     * }</pre>
     *
     * @see CompletableFuture
     * @see Call#enqueue(Callback)
     */
    public CompletableFuture<Response> wrap(Call call, boolean loggingEnabled) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        call.enqueue(new Callback() {

            /**
             * Handles successful HTTP responses from the server.
             *
             * <p>This method is invoked when a valid HTTP response is received,
             * regardless of the HTTP status code (including 4xx and 5xx responses).</p>
             *
             * @param call the call that generated this response
             * @param response the HTTP response received from the server
             */
            @Override
            public void onResponse(Call call, Response response) {
                if (loggingEnabled) {
                    log.debug("Received response from {}", call.request().url().host());
                }
                future.completeAsync(() -> response);
            }

            /**
             * Handles future cancellation by propagating it to the underlying OkHttp call.
             *
             * <p>This completion handler ensures that if the {@code CompletableFuture} is
             * cancelled, the underlying HTTP request is also cancelled to free up resources
             * and prevent unnecessary network activity.</p>
             */
            @Override
            public void onFailure(Call call, IOException e) {
                OpenWeatherApiClientException exception = null;

                if (call.isCanceled()) {
                    exception = new RequestCancellationException(e);
                } else {
                    exception = new OpenWeatherApiClientException(e);
                }

                if (loggingEnabled) {
                    log.warn("Failed while preforming request", exception);
                }

                future.completeExceptionally(exception);
            }
        });

        future.whenComplete((res, err) -> {
            if (future.isCancelled()) {
                if (loggingEnabled) {
                    log.debug("Request was cancelled");
                }

                call.cancel();
            }
        });

        return future;
    }
}