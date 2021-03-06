# Hippo CMS - Konakart integration

The aim of this project is to create a bridge between Hippo CMS and Konakart.

## Before starting a project

### Konakart installation 
Please install the [community](http://www.konakart.com/downloads/community_edition) version or the enterprise version.

Only community features are available.

### Define Environment Variables
In order to process with the next step, you need to define the KONAKART_HOME variable.

```
i.e.: KONAKART_HOME='C:\app\konakart\KonaKart-6.3.0.0\'
```

### Import Konakart libraries within your local .m2 repo
Konakart uses ant to build the entire project. The librairies are not available on any Maven repository. 
The project [Konakart Dependency](https://github.com/jmirc/Hippo-CMS-Konakart/tree/master/konakart-dependency) has been created to import into your local m2 repo the librairies.

The following steps need to be executed:

1. Clone the project
1. cd konakart-dependency
1. run mvn install

## How to start a project

*  Create a new project using the latest version of the artifact. Currently tested with the version 1.05.06

### Global POM.XML
* Add the following conf
```xml
    <dependencyManagement>
     ...
     <dependency>
         <groupId>mysql</groupId>
         <artifactId>mysql-connector-java</artifactId>
         <version>5.1.18</version>
     </dependency>
      ...
    </dependencyManagement>

    <profile>
       <id>cargo.run</id>
       <dependencies>
          <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>5.1.18</version>
          </dependency>
       </dependencies>
       <build>
         <plugins>
            <plugin>
            ...
            <configuration>
               <container>
                 <dependencies>
                      <dependency>
                         <groupId>mysql</groupId>
                         <artifactId>mysql-connector-java</artifactId>
                      </dependency>
                 </dependencies>
               ...
               </container>
    </profile>
```

### CMS Configuration
* Add a copy of the konakart.properties and the konakart_app.properties file under src/main/resources
* Add the following lines into the pom.xml file

```xml
       <dependency>
            <groupId>org.onehippo.forge.konakart</groupId>
            <artifactId>hippo-addon-konakart-cms</artifactId>
            <version>1.00.00-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.onehippo.forge.konakart</groupId>
            <artifactId>hippo-addon-konakart-repository</artifactId>
            <version>1.00.00-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
```

### SITE Configuration
* Add a copy of the konakart.properties and the konakart_app.properties file under src/main/resources
* Add the following lines into the pom.xml file

```xml

        <dependency>
            <groupId>org.onehippo.forge.konakart</groupId>
            <artifactId>hippo-addon-konakart-hstclient</artifactId>
            <version>1.00.00-SNAPSHOT</version>
        </dependency>
       <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
```
* Create a new file named //konakart-hst-configuration.xml// under resources/META-INF/hst-assembly/overrides to add the konakart Valve

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">

    <import resource="classpath:/org/onehippo/forge/konakart/hst/konakart-hst-configuration.xml"/>
</beans>
```
* Add in the web.xml of your site the following value to context parameter hst-beans-annotated-classes (note that the values are comma separated):
```xml
  <context-param>
    <param-name>hst-beans-annotated-classes</param-name>
    <param-value>
        classpath*:org/onehippo/forge/konakart/hst/beans/**/*.class
    </param-value>
  </context-param>

```

### Global Configuration
* Add the following database configuration to the context.xml file. You will add the connection to the Konakart DB previously created.

```xml

    <!-- Hippo Konakart configuration-->
    <Resource
            name="jdbc/konakart" auth="Container" type="javax.sql.DataSource"
            maxActive="20" maxIdle="10" minIdle="2" initialSize="2" maxWait="10000"
            testOnBorrow="true" validationQuery="select 1 from dual"
            poolPreparedStatements="true"
            username="konakart" password="konakart"
            driverClassName="com.mysql.jdbc.Driver"
            url="jdbc:mysql://localhost:3306/konakart?zeroDateTimeBehavior=convertToNull&amp;autoReconnect=true&amp;characterEncoding=utf8" />

```

### Custom Admin Configuration
* You need to update the following node with your needs. "/konakart:konakart/konakart:stores/store1"
    * Update contentroot (i.e. /content/documents/gettingstarted)
    * Update galleryroot (i.e. /content/gallery/gettingstarted)
	
### JAAS Security 
* Update the hst-config.properties file to update the security auth

Replace
```
# HST JAAS login configuration
#java.security.auth.login.config = classpath:/org/hippoecm/hst/security/impl/login.conf
```

By
```
# HST KONAKART login configuration
java.security.auth.login.config = classpath:/org/onehippo/forge/konakart/site/security/login.conf
```	