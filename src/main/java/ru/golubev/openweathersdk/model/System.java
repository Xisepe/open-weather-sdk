package ru.golubev.openweathersdk.model;

import lombok.Value;

/**
 * Contains astronomical information about sunrise and sunset times.
 *
 * <p>This class provides solar data for the requested location, including exact times
 * of sunrise and sunset in Unix timestamp format.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * System system = response.getSys();
 *
 * Date sunrise = new Date(system.getSunrise() * 1000);
 * Date sunset = new Date(system.getSunset() * 1000);
 *
 * System.out.println("Sunrise: " + sunrise);
 * System.out.println("Sunset: " + sunset);
 * }</pre>
 *
 * @since 1.0
 */
@Value
public class System {

    /**
     * Sunrise time in Unix timestamp format (seconds since January 1, 1970 UTC).
     *
     * <p><b>Note:</b> This timestamp is in seconds, not milliseconds. Multiply by 1000
     * when converting to Java {@link java.util.Date}.</p>
     *
     * <p><b>Example:</b> 1675753200 represents Tuesday, February 7, 2023 7:00:00 AM GMT</p>
     */
    long sunrise;

    /**
     * Sunset time in Unix timestamp format (seconds since January 1, 1970 UTC).
     *
     * <p><b>Note:</b> This timestamp is in seconds, not milliseconds. Multiply by 1000
     * when converting to Java {@link java.util.Date}.</p>
     *
     * <p><b>Example:</b> 1675792800 represents Tuesday, February 7, 2023 6:00:00 PM GMT</p>
     */
    long sunset;
}