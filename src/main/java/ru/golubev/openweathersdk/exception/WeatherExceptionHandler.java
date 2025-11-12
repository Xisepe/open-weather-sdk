package ru.golubev.openweathersdk.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import ru.golubev.openweathersdk.model.ErrorResponse;

/**
 * Utility class for parsing OpenWeather API error responses and converting them
 * to appropriate exception types.
 *
 * <p>This handler processes JSON error responses from the OpenWeather API and
 * maps them to specific exception classes based on the HTTP status code and
 * error content. This provides more granular error handling for API consumers.</p>
 *
 * <p><b>Error code mapping:</b></p>
 * <table border="1">
 *   <caption>Mapping of HTTP codes to exception types</caption>
 *   <tr>
 *     <th>HTTP Code</th>
 *     <th>Exception Type</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>401</td>
 *     <td>{@link ApiKeyException}</td>
 *     <td>Invalid or unauthorized API key</td>
 *   </tr>
 *   <tr>
 *     <td>404</td>
 *     <td>{@link NotFoundException}</td>
 *     <td>City or resource not found</td>
 *   </tr>
 *   <tr>
 *     <td>429</td>
 *     <td>{@link TooManyRequests}</td>
 *     <td>Rate limit exceeded</td>
 *   </tr>
 *   <tr>
 *     <td>500, 502, 503, 504</td>
 *     <td>{@link InternalApiException}</td>
 *     <td>Server-side errors</td>
 *   </tr>
 *   <tr>
 *     <td>Other codes</td>
 *     <td>{@link WeatherApiException}</td>
 *     <td>Generic API exception</td>
 *   </tr>
 * </table>
 *
 * <p><b>Example usage in API client:</b></p>
 * <pre>{@code
 * if (!response.isSuccessful()) {
 *     String errorBody = response.body().string();
 *     WeatherApiException exception = WeatherExceptionHandler.parse(
 *         response.message(), errorBody);
 *     throw exception;
 * }
 * }</pre>
 *
 * <p><b>Fallback behavior:</b> If JSON parsing fails, returns a generic
 * {@code WeatherApiException} with code -1 and "Unknown error" message.</p>
 *
 * @see WeatherApiException
 * @see ErrorResponse
 * @since 1.0
 */
@UtilityClass
public class WeatherExceptionHandler {

    /**
     * Jackson ObjectMapper for JSON deserialization of error responses.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses OpenWeather API error response and returns appropriate exception.
     *
     * @param status HTTP status text from the response
     * @param payload JSON error response body from OpenWeather API
     * @return specific WeatherApiException subclass based on error code
     *
     * <p><b>Parsing process:</b></p>
     * <ol>
     *   <li>Deserializes JSON payload to {@link ErrorResponse} object</li>
     *   <li>Extracts error code and message</li>
     *   <li>Maps to specific exception type based on code</li>
     *   <li>Returns generic exception if parsing fails</li>
     * </ol>
     *
     * <p><b>Example error response JSON:</b></p>
     * <pre>{@code
     * {
     *   "cod": 401,
     *   "message": "Invalid API key"
     * }
     * }</pre>
     */
    public WeatherApiException parse(String status, String payload) {
        try {
            ErrorResponse errorResponse = mapper.readValue(payload, ErrorResponse.class);

            switch (errorResponse.getCod()) {
                case 401:
                    return new ApiKeyException(status, errorResponse.getMessage());
                case 404:
                    return new NotFoundException(status, errorResponse.getMessage());
                case 429:
                    return new TooManyRequests(status, errorResponse.getMessage());
                case 500:
                case 502:
                case 503:
                case 504:
                    return new InternalApiException(errorResponse.getCod(), status, errorResponse.getMessage());
                default:
                    return new WeatherApiException(errorResponse.getCod(), status, errorResponse.getMessage());
            }
        } catch (JsonProcessingException e) {
            return new WeatherApiException(-1, status, "Unknown error");
        }
    }
}