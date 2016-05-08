# TWLocator App

## What is this

- This is the Android App Twitter Locator

## How to use this code

You'll need IntelliJ Idea with Android development plugin or Android Studio 1.0 and upwards.
We're using Gradle plugin 1.0.0

## Modules

This App's project is divided in several IntelliJ Modules:

- api: wrapper for server-side API, JSON parsing, etc.
- app: Android App (activities, etc.)

Using different modules we can use different test suites (JUnit for pure Java code, AndroidTestCase for Android code)

## Module api

```
src
|- main
  |-
|- test: contains all unit tests for this module
  |- java: code for Unit tests
  |- resources: JSON stub data used to create stubs
```

## Module App

### Package

Contains the android.App subclass. This class kicks off dependency injection (see Dagger below). Also, see Dependency Injection package

### Package app.di.modules

Configuration for Dagger dependency injection

---


## Main Third party Libraries used

### Gson

For converting JSON into Model objects

### Retrofit

For wrapping the REST Apis.
[http://square.github.io/retrofit/]()

### Otto

_An enhanced event bus with emphasis on Android support_

[Otto Page](http://square.github.io/otto/)

### Butterknife

### Dagger

[Main Dagger page](http://square.github.io/dagger/)

A [perfect introduction to Dagger](http://antonioleiva.com/dagger-android-part-2/)

### Android Jobqueue

---

