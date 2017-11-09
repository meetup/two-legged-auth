# Two legged Auth
[![Build Status](https://travis-ci.org/meetup/two-legged-auth.svg?branch=master)](https://travis-ci.org/meetup/two-legged-auth)

Generic open library providing easy way of performing get/post method using 2-legged authentication.

## Usage
Specify the `com.meetup.auth.config.Configuration` using `loadConfigurationFromEnv`,
Than instantiate `com.meetup.auth.MeetupConsumer`.<br>
Use consumer to perform get/post requests.


```scala
import com.meetup.auth.config.Configuration
import com.meetup.auth.MeetupConsumer

val configuration = Configuration.loadConfigurationFromEnv(issuer = "app-service", userAgent = "APP/0.1")
                                   ("APP_PRIVATE_KEY",
                                    "APP_PUBLIC_KEY",
                                    "APP_CLASSIC_OAUTH2_CLIENT_KEY",
                                    "APP_CHAPSTICK_ACCESS_TOKEN_URL",
                                    "APP_CHAPSTICK_API_ROOT_URL",
                                    "APP_TEST_API_KEY",
                                    "APP_FF_WHITELISTONLY")

val meetupConsumer = MeetupConsumer(configuration)

```
