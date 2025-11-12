package ru.golubev.openweathersdk;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of languages supported by the OpenWeather API for weather descriptions.
 *
 * <p>This enum provides type-safe access to all language codes supported by the OpenWeather API.
 * Using these languages will return weather descriptions in the specified language instead of English.</p>
 *
 * <p><b>Important notes:</b></p>
 * <ul>
 *   <li>Language affects only weather descriptions (e.g., "light rain", "clear sky")</li>
 *   <li>All other data (temperatures, coordinates, etc.) remains unchanged</li>
 *   <li>Not all weather conditions may be available in all languages</li>
 *   <li>Default language is English if not specified</li>
 * </ul>
 *
 * <p><b>Language coverage:</b> OpenWeather API supports over 40 languages worldwide,
 * including major European, Asian, Middle Eastern, and African languages.</p>
 *
 * @see <a href="https://openweathermap.org/api/one-call-3#multi">OpenWeather Multi-language Support</a>
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SupportedLanguage {
    AFRIKAANS("af"),
    ALBANIAN("sq"),
    ARABIC("ar"),
    AZERBAIJANI("az"),
    BASQUE("eu"),
    BELARUSIAN("be"),
    BULGARIAN("bg"),
    CATALAN("ca"),
    CHINESE_SIMPLIFIED("zh_cn"),
    CHINESE_TRADITIONAL("zh_tw"),
    CROATIAN("hr"),
    CZECH("cz"),
    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    FINNISH("fi"),
    FRENCH("fr"),
    GALICIAN("gl"),
    GERMAN("de"),
    GREEK("el"),
    HEBREW("he"),
    HINDI("hi"),
    HUNGARIAN("hu"),
    ICELANDIC("is"),
    INDONESIAN("id"),
    ITALIAN("it"),
    JAPANESE("ja"),
    KOREAN("kr"),
    KURMANJI("ku"),
    LATVIAN("la"),
    LITHUANIAN("lt"),
    MACEDONIAN("mk"),
    NORWEGIAN("no"),
    PERSIAN("fa"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    PORTUGUESE_BRAZIL("pt_br"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SERBIAN("sr"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SPANISH("es"),
    SWEDISH("sv"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("uk"),
    VIETNAMESE("vi"),
    ZULU("zu");

    /**
     * The language code used in OpenWeather API requests.
     *
     * <p>This is the standardized code that OpenWeather API expects for language parameter.
     * The codes follow IETF language tags or OpenWeather-specific conventions.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>"en" - English</li>
     *   <li>"es" - Spanish</li>
     *   <li>"zh_cn" - Chinese Simplified</li>
     *   <li>"pt_br" - Portuguese (Brazil)</li>
     * </ul>
     */
    private final String code;

    /**
     * Returns the language code string representation.
     *
     * <p>This method is provided for convenience when building API requests.
     * It returns the same value as {@link #code}.</p>
     *
     * @return the language code string (e.g., "fr", "de", "es")
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String code = SupportedLanguage.GERMAN.toString(); // Returns "de"
     * }</pre>
     */
    @Override
    public String toString() {
        return code;
    }

    /**
     * Converts a language code string to the corresponding {@code SupportedLanguage} enum value.
     *
     * <p>This method performs case-insensitive lookup of the language code.
     * It's useful for converting user input or configuration values to the type-safe enum.</p>
     *
     * @param code the language code to convert (e.g., "es", "FR", "zh_cn")
     * @return the corresponding {@code SupportedLanguage} enum value
     * @throws IllegalArgumentException if the provided code does not match any supported language
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * SupportedLanguage lang1 = SupportedLanguage.fromCode("es");     // Returns SPANISH
     * SupportedLanguage lang2 = SupportedLanguage.fromCode("ZH_CN");  // Returns CHINESE_SIMPLIFIED
     * SupportedLanguage lang3 = SupportedLanguage.fromCode("fr");     // Returns FRENCH
     * }</pre>
     *
     * <p><b>Exception example:</b></p>
     * <pre>{@code
     * try {
     *     SupportedLanguage lang = SupportedLanguage.fromCode("xx");
     * } catch (IllegalArgumentException e) {
     *     // Handle unsupported language code
     *     System.out.println(e.getMessage()); // "Unsupported language code: xx"
     * }
     * }</pre>
     */
    public static SupportedLanguage fromCode(String code) {
        for (SupportedLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + code);
    }
}
