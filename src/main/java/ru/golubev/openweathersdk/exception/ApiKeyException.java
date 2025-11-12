package ru.golubev.openweathersdk.exception;

/**
 * Exception representing HTTP 401 Unauthorized error from OpenWeather API.
 *
 * <p>This exception is thrown when the provided API key is invalid, expired,
 * or doesn't have sufficient permissions for the requested operation.</p>
 *
 * <p><b>Common causes:</b></p>
 * <ul>
 *   <li>Invalid or malformed API key</li>
 *   <li>Expired API key (for trial accounts)</li>
 *   <li>API key blocked due to policy violation</li>
 *   <li>Missing API key in request</li>
 *   <li>Insufficient subscription level for requested feature</li>
 * </ul>
 *
 * <p><b>Recommended actions:</b></p>
 * <ul>
 *   <li>Verify API key is correctly copied</li>
 *   <li>Check API key expiration date</li>
 *   <li>Regenerate API key in OpenWeather account</li>
 *   <li>Upgrade subscription plan if needed</li>
 *   <li>Verify API key is included in requests</li>
 * </ul>
 *
 * @see WeatherApiException
 * @since 1.0
 */
public class ApiKeyException extends WeatherApiException {

    public ApiKeyException(String status, String message) {
        super(401, status, message);
    }
}