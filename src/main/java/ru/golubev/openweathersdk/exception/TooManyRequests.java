package ru.golubev.openweathersdk.exception;

/**
 * Exception representing HTTP 429 Too Many Requests error from OpenWeather API.
 *
 * <p>This exception is thrown when the rate limit for the API key has been exceeded.
 * OpenWeather API imposes limits on the number of requests per minute/hour/day
 * based on the subscription plan.</p>
 *
 * <p><b>Common causes:</b></p>
 * <ul>
 *   <li>Exceeding requests per minute limit</li>
 *   <li>Exceeding daily API call quota</li>
 *   <li>Burst requests beyond allowed limits</li>
 * </ul>
 *
 * <p><b>Recommended actions:</b></p>
 * <ul>
 *   <li>Implement request throttling in your application</li>
 *   <li>Add retry logic with exponential backoff</li>
 *   <li>Cache responses to reduce API calls</li>
 *   <li>Consider upgrading API subscription plan</li>
 * </ul>
 *
 * @see WeatherApiException
 * @since 1.0
 */
public class TooManyRequests extends WeatherApiException {

    public TooManyRequests(String status, String message) {
        super(429, status, message);
    }
}