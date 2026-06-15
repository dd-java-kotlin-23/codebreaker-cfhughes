# Dependency injection approach: Dagger across `client`, `services`, and the UI layers

## Current state

- **`client`** (Kotlin): `CodebreakerProxy` interface with a `companion object { val instance = CodebreakerProxyImpl }`; `CodebreakerProxyImpl` is a Kotlin `object` (JVM-wide singleton) that builds Moshi/OkHttp/Retrofit in its `init`.
- **`services`** (Java, compiled under the kotlin-jvm plugin): `CodebreakerServiceImpl` grabs `CodebreakerProxy.getInstance()` in a field (the `FIXME`).
- The version catalog already has what we need: `dagger-core`, `dagger-compiler`, `hilt`, plus the `ksp` and `kapt` plugins. `@Inject`/`@Singleton` (`javax.inject`) come transitively with `dagger-core`, so no extra annotations dependency is required.

## The key cross-platform constraint

**Hilt is Android-only.** If we put Hilt annotations in the shared `client`/`services` modules, the JavaFX build breaks. So the rule is:

> Shared library modules (`client`, `services`) use **plain Dagger 2** — only `@Module`, `@Provides`/`@Binds`, `@Inject`, `@Singleton`. They expose *modules*, never a `@Component`. Each UI app owns its own graph: JavaFX builds a plain Dagger `@Component`; Android uses Hilt (which *is* Dagger) and just `includes` the shared modules.

## Encapsulation: prefer `@Provides` over `@Binds` at module boundaries

We currently hide implementations (`internal object CodebreakerProxyImpl`, package-private `CodebreakerServiceImpl`). To keep that hiding under multi-module Dagger, use **`@Provides`**, not `@Binds`:

- `@Binds` is inlined into the generated `@Component`, so the component (generated in the *UI* package) must be able to *name* the impl type → forces impls to be `public`.
- A `@Provides` factory is generated **in the module's own package/module**, so it can construct a package-private/`internal` impl while the component only sees the public return type. Encapsulation preserved.

---

## Recommended changes

### 1. `client` — `CodebreakerProxyImpl` becomes an injectable class

Drop the `object` singleton and the `companion object { instance }`. Move the Retrofit/Moshi/OkHttp construction into a Dagger module; the impl just receives its collaborators.

`CodebreakerProxy.kt`:
```kotlin
interface CodebreakerProxy {
    fun startGame(game: GameRequest): CompletableFuture<GameResponse>
    fun getGame(gameId: String): CompletableFuture<GameResponse>
    fun deleteGame(gameId: String): CompletableFuture<Void?>
    fun submitGuess(gameId: String, guess: GuessRequest): CompletableFuture<GuessResponse>
    fun getGuess(gameId: String, guessId: String): CompletableFuture<GuessResponse>
    fun shutdown()          // implements the existing TODO; cancels the scope
    // companion object removed
}
```

`CodebreakerProxyImpl.kt` (stays `internal`):
```kotlin
internal class CodebreakerProxyImpl(
    private val api: CodebreakerApi,
    private val moshi: Moshi,
    private val scope: CoroutineScope
) : CodebreakerProxy {
    override fun startGame(game: GameRequest) =
        scope.future { handleResponse(api.startGame(game)) }
    // ... other methods unchanged ...
    override fun shutdown() { scope.cancel() }
    // handleResponse helpers unchanged
}
```

New public `ClientModule.kt` (the only thing client exposes to DI):
```kotlin
@Module
object ClientModule {

    @Provides @Singleton
    fun properties(): Properties = loadProperties()

    @Provides @Singleton
    fun moshi(): Moshi = buildMoshi()

    @Provides @Singleton
    fun okHttpClient(properties: Properties): OkHttpClient = buildClient(properties)

    @Provides @Singleton
    fun api(properties: Properties, moshi: Moshi, client: OkHttpClient): CodebreakerApi =
        buildApi(properties, moshi, client)

    @Provides @Singleton
    fun scope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides @Singleton
    fun codebreakerProxy(
        api: CodebreakerApi, moshi: Moshi, scope: CoroutineScope
    ): CodebreakerProxy = CodebreakerProxyImpl(api, moshi, scope)   // @Provides → impl stays internal
}
```
(`loadProperties`/`buildMoshi`/`buildClient`/`buildApi` are the existing top-level helpers, reused.)

`client/build.gradle.kts` — add KSP (cleaner than kapt for Kotlin):
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)        // new
    jacoco
    alias(libs.plugins.openapi)
}
dependencies {
    implementation(libs.dagger.core)
    ksp(libs.dagger.compiler)      // new
    // ... existing ...
}
```

### 2. `services` — constructor injection + a module

`CodebreakerServiceImpl.java`:
```java
class CodebreakerServiceImpl implements CodebreakerService {
  private final CodebreakerProxy proxy;

  @Inject
  CodebreakerServiceImpl(CodebreakerProxy proxy) {   // replaces the FIXME field
    this.proxy = proxy;
  }

  @Override public void shutdown() { proxy.shutdown(); }   // replaces the TODO
  // ... everything else unchanged ...
}
```

New public `ServiceModule.java` (same package as the impl, so it can see the package-private class):
```java
@Module(includes = ClientModule.class)   // pulls in the whole client graph
public class ServiceModule {
  @Provides @Singleton
  CodebreakerService codebreakerService(CodebreakerProxy proxy) {
    return new CodebreakerServiceImpl(proxy);   // @Provides keeps impl package-private
  }
}
```

`services/build.gradle.kts` — Java annotation processing (the kotlin-jvm plugin applies the Java plugin, so `annotationProcessor` is available for the `.java` sources):
```kotlin
dependencies {
    implementation(project(":client"))
    implementation(libs.dagger.core)
    annotationProcessor(libs.dagger.compiler)   // new
    // ... existing ...
}
```
> If these service files are ever converted to Kotlin, switch this module to KSP like `client`.

### 3. UI layers own the `@Component`

**JavaFX** (plain Dagger, in the `javafx` module):
```java
@Singleton
@Component(modules = ServiceModule.class)   // ClientModule comes in transitively
public interface CodebreakerComponent {
  CodebreakerService codebreakerService();
}
```
```java
// in Application.init()
CodebreakerService service = DaggerCodebreakerComponent.create().codebreakerService();
```
Tip: the same component can expose injected JavaFX controllers and be registered as the FXML `controllerFactory`, so controllers receive `CodebreakerService` automatically.

**Android** (Hilt, in the `app` module — Hilt is Dagger underneath, so it consumes the same plain modules):
```kotlin
@Module(includes = [ServiceModule::class])
@InstallIn(SingletonComponent::class)     // maps the @Singleton to Hilt's app scope
object AppDiModule
```
Then `@HiltViewModel` view models / `@AndroidEntryPoint` components just `@Inject` `CodebreakerService`. The shared modules stay Hilt-free.

---

## Why this works for both targets

- Shared code depends only on plain Dagger + `javax.inject` — compiles identically for JVM/desktop and Android.
- `@Provides` boundaries let `CodebreakerProxyImpl` stay `internal` and `CodebreakerServiceImpl` stay package-private — the current encapsulation is preserved, just sourced from the DI graph instead of a static `instance`.
- The JVM-wide `object`/`getInstance()` singletons are replaced by component-scoped `@Singleton`s, which is what makes the graph testable (build a component with a fake `CodebreakerProxy`).
- Each UI framework wires the graph the idiomatic way (Dagger component for JavaFX, Hilt for Android) without the libraries knowing or caring which.

## Open items / follow-ups

- Add a `shutdown()` function to `CodebreakerProxy` (currently a TODO) and cancel the `CoroutineScope` in the impl.
- Remove `CodebreakerProxy.instance` and the `CodebreakerServiceImpl` static lookup once the components exist.
- The `cli` module is currently empty; the JavaFX/Android `@Component`s land when those modules are created.
