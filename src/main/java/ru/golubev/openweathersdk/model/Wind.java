package ru.golubev.openweathersdk.model;

import lombok.Value;

/**
 * Represents wind conditions including speed and direction.
 *
 * <p>This class provides meteorological data about wind at the measurement location.
 * Wind speed is the primary measurement provided in standard responses.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Wind wind = response.getWind();
 *
 * System.out.println("Wind speed: " + wind.getSpeed() + " m/s");
 *
 * // Convert to km/h
 * double windKmh = wind.getSpeed() * 3.6;
 * System.out.println("Wind speed: " + windKmh + " km/h");
 * }</pre>
 *
 * @since 1.0
 */
@Value
public class Wind {
    /**
     * Wind speed in meters per second (m/s).
     *
     * <p>Represents the horizontal speed of air movement at the measurement location.</p>
     *
     * <p><b>Conversion formulas:</b></p>
     * <ul>
     *   <li><b>Kilometers per hour:</b> {@code speed * 3.6}</li>
     *   <li><b>Miles per hour:</b> {@code speed * 2.237}</li>
     *   <li><b>Knots:</b> {@code speed * 1.944}</li>
     * </ul>
     *
     * <p><b>Beaufort scale reference:</b></p>
     * <ul>
     *   <li><b>0-0.2 m/s:</b> Calm</li>
     *   <li><b>0.3-1.5 m/s:</b> Light air</li>
     *   <li><b>1.6-3.3 m/s:</b> Light breeze</li>
     *   <li><b>3.4-5.4 m/s:</b> Gentle breeze</li>
     *   <li><b>5.5-7.9 m/s:</b> Moderate breeze</li>
     *   <li><b>8.0-10.7 m/s:</b> Fresh breeze</li>
     *   <li><b>10.8-13.8 m/s:</b> Strong breeze</li>
     *   <li><b>13.9-17.1 m/s:</b> High wind</li>
     *   <li><b>17.2-20.7 m/s:</b> Gale</li>
     *   <li><b>20.8-24.4 m/s:</b> Strong gale</li>
     *   <li><b>24.5-28.4 m/s:</b> Storm</li>
     *   <li><b>28.5-32.6 m/s:</b> Violent storm</li>
     *   <b>&gt;32.6 m/s:</b> Hurricane force</li>
     * </ul>
     *
     * <p><b>Example:</b> 5.5 (moderate breeze)</p>
     */
    Double speed;
}