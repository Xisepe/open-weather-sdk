package ru.golubev.openweathersdk.exception;

/**
 * Exception representing HTTP 404 Not Found error from OpenWeather API.
 *
 * <p>This exception is thrown when the requested city or location cannot be found
 * in the OpenWeather database. This typically indicates an invalid city name,
 * misspelling, or a location that is not covered by the service.</p>
 *
 * <p><b>Common causes:</b></p>
 * <ul>
 *   <li>Misspelled city name</li>
 *   <li>City name in wrong language</li>
 *   <li>Small villages or remote locations not in database</li>
 *   <li>Incorrect country code specification</li>
 * </ul>
 *
 * <p><b>Recommended actions:</b></p>
 * <ul>
 *   <li>Verify city name spelling</li>
 *   <li>Try different language variations</li>
 *   <li>Use coordinates instead of city name</li>
 *   <li>Check OpenWeather coverage for the location</li>
 * </ul>
 *
 * @see WeatherApiException
 * @since 1.0
 */
public class NotFoundException extends WeatherApiException {

    public NotFoundException(String status, String message) {
        super(404, status, message);
    }
}