## Demographics service

Source: https://github.com/hapifhir/hapi-fhir-jpaserver-starter

#### Running locally using Spring Boot
```bash
mvn clean package spring-boot:repackage -Pboot && java -jar target/hapi-fhir-jpaserver.war
```
Server will then be accessible at http://localhost:8080/ and eg. http://localhost:8080/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8080/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```

#### Configuration via environment variables

You can customize HAPI directly from the `run` command using environment variables. For example:

`docker run -p 8080:8080 -e hapi.fhir.default_encoding=xml hapiproject/hapi:tagname`

HAPI looks in the environment variables for properties in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file for defaults.

#### Configuration via overridden application.yaml file

You can customize HAPI by telling HAPI to look for the configuration file in a different location, eg.:

`docker run -p 8090:8080 -e "--spring.config.location=classpath:/another.application.yaml" hapiproject/hapi:tagname`

#### Example docker-compose.yml

```
version: '3.7'
services:
  web:
    image: "hapiproject/hapi:tagname"
    ports:
      - "8090:8080"
    configs:
      - source: hapi
        target: /data/hapi/application.yaml
    volumes:
      - hapi-data:/data/hapi
    environment:
      JAVA_OPTS: '-Dhapi.properties=/data/hapi/application.yaml'
configs:
  hapi:
     external: true
volumes:
    hapi-data:
        external: true
```


#### Configurations

Much of this HAPI starter project can be configured using the yaml file in _src/main/resources/application.yaml_. By default, this starter project is configured to use H2 as the database.

#### PostgreSQL configuration

To configure the starter app to use PostgreSQL, instead of the default H2, update the application.yaml file to have the following:

```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://localhost:5432/hapi_dstu3'
    username: admin
    password: admin
    driverClassName: org.postgresql.Driver
```

Because the integration tests within the project rely on the default H2 database configuration, it is important to either explicity skip the integration tests during the build process, i.e., `mvn install -DskipTests`, or delete the tests altogether. Failure to skip or delete the tests once you've configured PostgreSQL for the datasource.driver, datasource.url, and hibernate.dialect as outlined above will result in build errors and compilation failure.

#### Customizing The Web Testpage UI

The UI that comes with this server is an exact clone of the server available at [http://hapi.fhir.org](http://hapi.fhir.org). You may skin this UI if you'd like. For example, you might change the introductory text or replace the logo with your own.

The UI is customized using [Thymeleaf](https://www.thymeleaf.org/) template files. You might want to learn more about Thymeleaf, but you don't necessarily need to: they are quite easy to figure out.

Several template files that can be customized are found in the following directory: [https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates)

#### Deploying to an Application Server

Using the Maven-Embedded Jetty method above is convenient, but it is not a good solution if you want to leave the server running in the background.

Most people who are using HAPI FHIR JPA as a server that is accessible to other people (whether internally on your network or publically hosted) will do so using an Application Server, such as [Apache Tomcat](http://tomcat.apache.org/) or [Jetty](https://www.eclipse.org/jetty/). Note that any Servlet 3.0+ compatible Web Container will work (e.g Wildfly, Websphere, etc.).

Tomcat is very popular, so it is a good choice simply because you will be able to find many tutorials online. Jetty is a great alternative due to its fast startup time and good overall performance.

To deploy to a container, you should first build the project:

```bash
mvn clean install
```

This will create a file called `hapi-fhir-jpaserver.war` in your `target` directory. This should be installed in your Web Container according to the instructions for your particular container. For example, if you are using Tomcat, you will want to copy this file to the `webapps/` directory.

Again, browse to the following link to use the server (note that the port 8080 may not be correct depending on how your server is configured).

[http://localhost:8080/hapi-fhir-jpaserver/](http://localhost:8080/hapi-fhir-jpaserver/)

If you would like it to be hosted at the root, eg. http://localhost:8080/ - then rename the WAR file to ```ROOT.war```.

#### Deploy with docker compose

Docker compose is a simple option to build and deploy container. To deploy with docker compose, you should build the project
with `mvn clean install` and then bring up the containers with `docker-compose up -d --build`. The server can be
reached at http://localhost:8080/hapi-fhir-jpaserver/.

In order to use another port, change the `ports` parameter
inside `docker-compose.yml` to `8888:8080`, where 8888 is a port of your choice.

The docker compose set also includes my MySQL database, if you choose to use MySQL instead of H2, change the following
properties in application.yaml:

```yaml
spring:
  datasource:
    url: 'jdbc:mysql://hapi-fhir-mysql:3306/hapi'
    username: admin
    password: admin
    driverClassName: com.mysql.jdbc.Driver
```

#### Running hapi-fhir-jpaserver direclty from IntelliJ as Spring Boot
Make sure you run with the maven profile called ```boot``` and NOT also ```jetty```. Then you are ready to press debug the project directly without any extra Application Servers.

#### Running hapi-fhir-jpaserver-example in Tomcat from IntelliJ

Install Tomcat.

Make sure you have Tomcat set up in IntelliJ.

- File->Settings->Build, Execution, Deployment->Application Servers
- Click +
- Select "Tomcat Server"
- Enter the path to your tomcat deployment for both Tomcat Home (IntelliJ will fill in base directory for you)

Add a Run Configuration for running hapi-fhir-jpaserver-example under Tomcat

- Run->Edit Configurations
- Click the green +
- Select Tomcat Server, Local
- Change the name to whatever you wish
- Uncheck the "After launch" checkbox
- On the "Deployment" tab, click the green +
- Select "Artifact"
- Select "hapi-fhir-jpaserver-example:war"
- In "Application context" type /hapi

Run the configuration.

- You should now have an "Application Servers" in the list of windows at the bottom.
- Click it.
- Select your server, and click the green triangle (or the bug if you want to debug)
- Wait for the console output to stop

Point your browser (or fiddler, or what have you) to `http://localhost:8080/hapi/baseDstu3/Patient`

It is important to use MySQL5Dialect when using MySQL version 5+.

#### Enabling Subscriptions

The server may be configured with subscription support by enabling properties in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file:

- `hapi.fhir.subscription.resthook.enabled` - Enables REST Hook subscriptions, where the server will make an outgoing connection to a remote REST server

- `hapi.fhir.subscription.email.*` - Enables email subscriptions. Note that you must also provide the connection details for a usable SMTP server.

- `hapi.fhir.subscription.websocket.enabled` - Enables websocket subscriptions. With this enabled, your server will accept incoming websocket connections on the following URL (this example uses the default context path and port, you may need to tweak depending on your deployment environment): [ws://localhost:8080/hapi-fhir-jpaserver/websocket](ws://localhost:8080/hapi-fhir-jpaserver/websocket)

#### Enabling EMPI

Set `hapi.fhir.empi_enabled=true` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to enable EMPI on this server.  The EMPI matching rules are configured in [empi-rules.json](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/empi-rules.json).  The rules in this example file should be replaced with actual matching rules appropriate to your data. Note that EMPI relies on subscriptions, so for EMPI to work, subscriptions must be enabled. 

#### Using Elasticsearch

By default, the server will use embedded lucene indexes for terminology and fulltext indexing purposes. You can switch this to using lucene by editing the properties in [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml)

For example:

```properties
elasticsearch.enabled=true
elasticsearch.rest_url=http://localhost:9200
elasticsearch.username=SomeUsername
elasticsearch.password=SomePassword
elasticsearch.required_index_status=YELLOW
elasticsearch.schema_management_strategy=CREATE
```
