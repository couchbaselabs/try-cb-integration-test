# Couchbase Travel-Sample Integration Tests

This projects runs integration on Couchbase Travel-Sample application using JUnit, Selenium WebDriver and TestContainers. 

## Prerequisites

The following pieces need to be in place in order to run the application.

1. Have Docker installed and runnable by the user running the tests
2. Maven , nodejs

## Running the test

Build the necessary Docker images (see bellow).

Simply run `mvn test`.

## How to build necessary docker images

### Couchbase image

TestContainers will pull the latest one, so no hassle here.

### Build trycb/front

To build the frontend:

1. checkout the frontend with `git clone https://github.com/couchbaselabs/try-cb-frontend/`
2. Install dependencies with `npm install`
3. Installl angular-cli with `npm install -g angular-cli`
4. Build the frontend with `ng build --environment=test`
5. Build the docker image with `docker build -t trycb/front .`


### Build trycb/java

To build the Java backend

1. checkout the code with `git clone https://github.com/couchbaselabs/try-cb-java/`
2. Build the project and the docker image with `mvn clean package docker:build`