# Streamx CLI

This project provides utilities for managing the mesh:
* It allows to run a defined mesh from commands,
* It allows to ingest data into mesh.

For more information, see the [StreamX CLI Reference](https://www.streamx.dev/guides/main/streamx-command-line-interface-reference.html).

## Structure

The project consists of the following modules:
* Entrypoint - validates Java version, executes first phase of initialization
* Core - parses and executes commands
* Distribution - module responsible for releasing package and distributing to `scoop` and `homebrew`
* e2e-tests - contains and executes end-to-end tests for built package

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw clean install
```
It produces the `streamx-cli-<project.version>-runner.jar` file in the `distribution/target` directory.

The application is packaged as an _über-jar_.
You can run it with `java -jar distribution/target/*-runner.jar`.

## Running tests for the application

You can run all tests including e2e against `streamx` jar from this code using:
```shell script
./mvnw clean install -P all-tests
```

E2e tests are omitted for:
```shell script
./mvnw clean install
```

## Configuration

There are several ways of configuring and several properties to configure. 

For details refer to [StreamX CLI Reference](https://www.streamx.dev/guides/main/streamx-command-line-interface-reference.html).