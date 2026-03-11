# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.9/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.9/maven-plugin/build-image.html)
* [Azure Actuator](https://aka.ms/spring/docs/actuator)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.4.9/reference/actuator/index.html)
* [Azure Cosmos DB](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#spring-data-support)
* [Azure Storage](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#resource-handling)
* [Validation](https://docs.spring.io/spring-boot/3.4.9/reference/io/validation.html)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/3.4.9/reference/web/reactive.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [How to use Spring Boot Starter with Azure Cosmos DB SQL API](https://aka.ms/spring/msdocs/cosmos)
* [How to use the Spring Boot starter for Azure Storage](https://aka.ms/spring/msdocs/storage)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)

### Additional Links

These additional references should also help you:

* [Azure Cosmos DB Sample](https://aka.ms/spring/samples/latest/cosmos)
* [Azure Storage Sample](https://aka.ms/spring/samples/latest/storage)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

