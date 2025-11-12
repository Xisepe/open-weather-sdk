# Open Weather SDK ☁️

A lightweight Java SDK for accessing the [OpenWeather API](https://openweathermap.org/api).  
Designed to be simple, type-safe, and compatible with Java 8+ (compiled with toolchain targeting Java 11).

---

## Features

- Simple and fluent API for current weather and forecasts
- Asynchronous requests using `CompletableFuture`
- Built-in exception handling for API responses
- Supports Java 11+

---

## Installation

### Option 1: Local Build

```bash
git clone https://github.com/Xisepe/open-weather-sdk.git
cd open-weather-sdk
./gradlew clean build publishToMavenLocal
```

Then in your Gradle project:

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.github.Xisepe:open-weather-sdk:1.0.0")
}
```

### Option 2: JitPack Dependency

For complete documentation follow: **[https://jitpack.io/#Xisepe/open-weather-sdk/1.0.0](https://jitpack.io/#Xisepe/open-weather-sdk/1.0.0)**

For Gradle add JitPack to your Gradle repositories:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

Then add the dependency:

```kotlin
dependencies {
    implementation("com.github.Xisepe:open-weather-sdk:1.0.0")
}
```

For Maven add the JitPack repository to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Then add the dependency:

```xml
<dependency>
  <groupId>com.github.Xisepe</groupId>
  <artifactId>open-weather-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## Documentation

### Remote GitHub Pages Documentation

The complete SDK reference (JavaDoc) is available here:  
👉 **[https://xisepe.github.io/open-weather-sdk/](https://xisepe.github.io/open-weather-sdk/)**

This documentation is automatically generated from the latest `main` branch using **GitHub Actions**.  
It includes:

- Full Java API reference
- Descriptions of all public classes and exception types
- Usage examples and parameter details
- Links between model objects and API endpoints

### Build local

To generate JavaDoc (with Lombok support):

```bash
./gradlew delombok javadoc
```

Output will be in:
```
build/docs/javadoc
```

---

## Example Usage

```java
OpenWeatherClient client = new WeatherSdk.instance().build("YOUR_API_KEY");

WeatherResponse response = client.getWeather("London");

System.out.println("Temperature: " + response.getTemperature().getTemp());
```

Async usage:

```java
client.getWeatherAsync("Paris")
      .thenAccept(weather -> System.out.println(weather.getTemperature().getTemp()))
      .join();
```

---

## Exception Handling

The SDK automatically maps HTTP error codes to exception types:

| HTTP Code | Exception Type | Description |
|------------|----------------|--------------|
| 401 | `ApiKeyException` | Invalid or unauthorized API key |
| 404 | `NotFoundException` | City or resource not found |
| 429 | `TooManyRequests` | Rate limit exceeded |
| 500–504 | `InternalApiException` | Server-side errors |
| Other | `WeatherApiException` | Generic API exception |

---

## Publishing

### Local Maven
```bash
./gradlew publishToMavenLocal
```

### CI/CD (via GitHub Actions)

Use GitHub’s built-in `GITHUB_TOKEN` for authentication:

```yaml
- name: Publish to Maven Local
  run: ./gradlew publish
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## Toolchain Compatibility

| Java Version | Supported | Notes |
|---------------|------------|-------|
| 11 | ✅ | Default compilation target |
| 17 | ✅ | Fully compatible |
| 21 | ✅ | Runtime compatible |

---

## License

[Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0)

© 2025 Xisepe
