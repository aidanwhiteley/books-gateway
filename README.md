# gateway
A Spring Cloud Gateway project to provide an API gateway to the Books project APIs.

Provides throttling and timeouts on calls to to the microservices.

Integrates with a Eureka service registry to know which microservice instances are active to 
know where to route calls to.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.aidan.books%3Agateway&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.aidan.books%3Agateway)
[![codecov](https://codecov.io/gh/aidanwhiteley/books-gateway/branch/master/graph/badge.svg)](https://codecov.io/gh/aidanwhiteley/books-gateway/branch/master)

The checked in Spring profile is "test" to allow tests to run stand alone. To use the project with a Euerka 
service registry set the spring.profiles.active to default.