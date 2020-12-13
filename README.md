# Pluto Kotlin Client SDK
Kotlin client SDK for pluto.

 # Pluto Swift Client SDK

![build_check](https://github.com/MuShare/PlutoSwiftClientSDK/workflows/build_check/badge.svg)
[![License](https://img.shields.io/cocoapods/l/PlutoSDK.svg?style=flat)](https://cocoapods.org/pods/PlutoSDK)

Kotlin client SDK for Pluto login microservice, which simplify the implementation for signing in with email, Google, WeChat and Apple.

## Installation

The Kotlin SDK for Pluto is available through maven repo provided by Github.

You may need create a github access token as a credential, and add the maven repo into your `build.gradle` file.

```groovy
repositories {
    maven {
        url 'https://maven.pkg.github.com/MuShare/Pluto-Kotlin-SDK'
        credentials {
            username = '<Github username>'
            password = '<Github access token>'
        }
    }
    jcenter()
    maven { url "https://jitpack.io" }
}
```

Then add the following dependency your `build.gradle` file.

```groovy
implementation 'org.mushare:pluto-kotlin-client-sdk:0.6.5'
```

**The following code MUST be added to the `proguard-rules.pro` file.** 
Please create a new one if there is no `proguard-rules.pro` file.

```
-keepclassmembers enum * { *; }
```

## Document

To auth with Pluto, set up it at first in your `Application` class.

```kotlin
Pluto.initialize(appContext, plutoPath, plutoAppId)
```

### Sign in, Sign up and Sign out

Implement signing in and signing up with the following methods

- `Pluto.getInstance()?.register()`
- `Pluto.getInstance()?.resendValidationEmail()`
- `Pluto.getInstance()?.loginWithAccount()`
- `Pluto.getInstance()?.loginWithGoogle()`
- `Pluto.getInstance()?.loginWithApple()`
- `Pluto.getInstance()?.resetPassword()`
- `Pluto.getInstance()?.logout()`

### Token Management

After signing in, get token or the header with token with the following methods

- `Pluto.getInstance()?.getToken()`
- `Pluto.getInstance()?.getHeaders()`

Get and update user information with 

- `Pluto.getInstance()?.currentUser`
- `Pluto.getInstance()?.myInfo()`
- `Pluto.getInstance()?.updateName()`
- `Pluto.getInstance()?.uploadAvatar()`

Get scopes from jwt token with

- `Pluto.getInstance()?.getScopes()`

### Account Binding

Bind and unbind accounts

- `Pluto.getInstance()?.bind()`
- `Pluto.getInstance()?.unbind()`

Get avaliable login types and bindings

- `Pluto.getInstance()?.availiableLoginTypes`
- `Pluto.getInstance()?.availiableBindings`

## License

 Pluto Kotlin Client SDK is available under the MIT license. See the LICENSE file for more info.
