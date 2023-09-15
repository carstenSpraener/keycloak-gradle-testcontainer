# keycloak-gradle-testcontainer
A keycloak test container able to run under gradle. 

[![CI build](https://github.com/carstenSpraener/keycloak-gradle-testcontainer/actions/workflows/gradle.yml/badge.svg)](https://github.com/carstenSpraener/keycloak-gradle-testcontainer/actions/workflows/gradle.yml)
![](https://img.shields.io/github/v/release/carstenSpraener/keycloak-gradle-testcontainer?label=Release)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.carstenSpraener/keycloak-gradle-testcontainer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.carstenSpraener%22%20AND%20a:%22keycloak-gradle-testcontainer%22)
![](https://img.shields.io/github/license/carstenSpraener/keycloak-gradle-testcontainer?label=License)
![](https://img.shields.io/badge/Keycloak-22.0-blue)


This is an extend of the [KeycloakTestcontainer from dasniko](https://github.com/dasniko/testcontainers-keycloak). This extension enables the container to start providers in a gradle environment. Also it provides a mechanism to enhance the configuration of the container with your own configuration settings.

For detailed information of the underlying testcontainer see the well documented [readme of the testcontainer-keycloak from dasniko here](https://github.com/dasniko/testcontainers-keycloak/blob/main/README.md).

The enhancements with this tiny extensions are the following:

### Adding a provider to the keycloak testcontainer

The original testcontainer expects a maven environment where the resources and classes are all in the same subdirectory. This is different in gradle. In order to enable a provider in a gradle environment you need to:

* Use the XKeycloakTestcontainer
* add the `build/classes/java/main` directory to the testcontainers provider jar
* add the `build/resouces/main` directory to the testcontainers provider.jar

An example is in the Testcase:

__NOTE: Since this is a test it adds the "test"-directories to the provider.jar__

```java
    @Container
    private static final XKeycloakContainer testContainer = new XKeycloakContainer()
            // add the gradle classes dir (test) to the providers jar
            // NOTE! Normally this is the build/classes/main directory!
            .withProviderJarContentModifier( i -> i.importDirectory("build/classes/java/test"))
            // add the gradle resource dir (test) to the providers jar
            // NOTE! Normally this is the build/resources/main directory!
            .withProviderJarContentModifier( i -> i.importDirectory("build/resources/test"))

```

The 'withProviderJarContentModifier' methods adds a `Consumer<ExplodedImporter>` to a list. This consumers are called when
the `providers.jar` is build.

### Add additional configurations to the container

When you want to add your own configuration to the container you can add a `BiConsumer<ExtendableKeycloakContainer<?>, List<String>>` to the XKeycloakTestcontainer. This consumers will be called after the container has finished its internal configuration. The parameters are the container itself and a list of Strings containing the command parts for the docker containers start command.

A typical usecase is adding a debug configuration to the container so you can debug your extension inside the running container.
```java
private static void configureDebug(ExtendableKeycloakContainer<?> ekc, List<String> commandParts) {
    // Set the Environment-Variable "DEBUG_PORT" to the required port
    ekc.withEnv("DEBUG_PORT", "*:8787");
    // The port needs to be exported to a fixed address. This is only possible
    // in a XKeycloakContainer
    ((XKeycloakContainer)ekc).withDebugPort(8787, 8787);
    // Do not suspend the container. Otherwise, the tests will stop if there
    // is no connecting debugger.
    ekc.withEnv("DEBUG_SUSPEND", "n");
    // add the "-debug" option to the container start command
    commandParts.add("--debug");
}
```
You can add this method to your container configuration by:

```java
    // add remote debugging configuration to the container
    testContainer.withConfigurationModifier(XKeycloakContainerTests::configureDebug)
```
### A special debugPort-Method

Talking about debugging: The XKeycloakContainer has a special method to set a debug port. The `withDebugPort`-Method will mapp a given intern port ti a fixed outside port. This is normaly not a good idea because you can not be shure, that the port is free on the machine running the tests. 

But for debugging you need to know which port to connect to. Use this method do define a port which is reachable from the outside for debugging purpose.

