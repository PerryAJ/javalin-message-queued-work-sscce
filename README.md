# Async Javalin API Experiments

This project was put together as a minimal reproduction/demo of a Javalin-based application that uses an async message
channel for orchestrating work between the http gateway (javalin), and a loosely-coupled 'worker'.

This project is not intended for production use, and should not be viewed as anything more than a simple tech demo that
has been slapped together quickly as a mechanism for discussion.

# Description

The `App` class is the main entry point of the application.  In it, a Javalin instance is created to handle two
endpoints: `/simple` and `/dispatched`.  These two endpoints take different code paths to accomplish the same
simulated 'work', ultimately returning the results of the http request via Javalin's `future()` api.

The `/simple` endpoint receives a GET request, and then computes 'work' asynchronously in a `CompletableFuture.runAsync`.

The `/dispatched` endpoint does the same 'work', but follows a different path in which:

1. request is received by Javalin handler in the App class
2. request is converted into a simple serializable 'proxy' of the Request
3. a reference to the Future returned to `Javalin.future()` is held in a local map, keyed on a unique 'request id'
4. The proxy is sent to an async message queue, where a 'worker' is listening
5. The 'worker' captures the Request proxy, does some work, and then fires off a 'Response' object, containing the result of the work
6. A message channel listener on the App class captures the 'Response' event, reads its contents and completes the future in the map that Javalin is waiting on

# Running

Clone the project, and run `./gradlew run` from the root of the repo (or `gradlew.bat` on windows systems)
