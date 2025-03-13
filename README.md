# Leucine Muse

A Java web application built with Tomcat, Servlet, and GSON using Java 17.

## Overview

Leucine Muse is a web application that demonstrates the integration of modern Java technologies including:

- Java 17
- Jakarta Servlet API
- Apache Tomcat
- Google GSON for JSON processing

## Project Structure

```
leucine-muse/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── leucine/
│   │   │           ├── config/
│   │   │           │   └── AIConfig.java
│   │   │           └── utils/
│   │   │               └── ClaudeClient.java
│   │   ├── resources/
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── web.xml
└── pom.xml
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Apache Tomcat 10 (embedded in the project)

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create a WAR file in the `target` directory.

## Running the Application

You can run the application using the embedded Tomcat server:

```bash
mvn tomcat7:run
```

The application will be available at: http://localhost:8080/leucine-muse

## Configuration

The application includes configuration for AI services in the `AIConfig` class.

## License

This project is licensed under the MIT License - see the LICENSE file for details.