# Open Gateway
An implementation of the Gateway API microservices pattern using Java technologies.

## Master Build Status

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/grycap/opengateway/blob/master/LICENSE)
[![Build Status](https://api.travis-ci.org/grycap/opengateway.svg)](https://travis-ci.org/grycap/opengateway/builds)
[![Coverage Status](https://coveralls.io/repos/grycap/opengateway/badge.svg?branch=master&service=github)](https://coveralls.io/github/grycap/opengateway?branch=master)

## Environment variables

``GRYCAP_TESTS_PRINT_OUTPUT`` set value to ``true`` to print tests output.

## Installation

### Install from source

``$ mvn clean install opengateway``

## Development

### Run all tests logging to the console

``$ mvn clean verify -pl opengateway-core -Dgrycap.tests.print.out=true |& tee /tmp/LOGFILE``

### Run functional and sanity tests logging to the console

``$ mvn clean test -pl opengateway-core -Dgrycap.tests.print.out=true |& tee /tmp/LOGFILE``

## Continuous integration

``$ mvn clean verify opengateway``

## TO-DO list

1. Add more method to the class OgExpectationInitializer (POST, PUT, DELETE).
2. Test conversion in BaseRestService.
3. Complete the classes: SecureRestServer and WebSocketsServer.
4. Include two load-balancing connectors: a) Netflix Eureka-Ribbon; and b) Baker Street (HAProxy-based client side load balancer).