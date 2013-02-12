Evernote SDK for Java
==========================================

Evernote API version 1.23


Overview
--------
This SDK contains wrapper code used to call the Evernote Cloud API from Java applications.

For Android-specific code and samples, see the [Evernote SDK for Android](http://www.github.com/evernote/evernote-sdk-android).

The SDK also contains two samples. The code in `sample/oauth` demonstrates the basic use of the SDK. The code in `sample/client` also demonstrates the basic use of API, but uses developer tokens instead of OAuth for authentication.

JavaDocs for the SDK are available at http://dev.evernote.com/documentation/reference/javadoc/.

Changes in version 1.23
-----------------------
We have completely reorganized the SDK as part of the release of API version 1.23. If you were using a previous version of the SDK, you will need to make a few changes to your project as part of moving to version 1.23.

* We have moved to a [Maven](http://maven.apache.org) build process. Compiled JAR files are no longer included in this repository. See [Including the SDK in your project](#including-the-sdk-in-your-project) below.

* We have removed our dependency on `libthrift.jar`. All of the required Thrift runtime classes are now included directly in the SDK JAR file. If you had previously included `libthrift.jar` in your project, you must remove it.

* We use a customized version of the [Apache Thrift](http://thrift.apache.org) runtime. To avoid namespace collisions with other projects that use Thrift, we have repackaged the runtime components that we use into the `com.evernote.thrift package`. You will need to change references to Thrift components such as `TBinaryProtocol`, `THttpClient` and `TTransportException` from `org.apache.thrift` to `com.evernote.thrift`. For example:

    ```java
    import org.apache.thrift.protocol.TBinaryProtocol;
    import org.apache.thrift.transport.THttpClient;
    import org.apache.thrift.transport.TTransportException;
    ```

    becomes

    ```java
    import com.evernote.thrift.protocol.TBinaryProtocol;
    import com.evernote.thrift.transport.THttpClient;
    import com.evernote.thrift.transport.TTransportException;
    ```

Prerequisites
-------------
In order to use the Cloud API, you need to obtain an API key from http://dev.evernote.com/documentation/cloud. You'll also find full API documentation on that page.

In order to run the sample code, you need a user account on the sandbox service where you will do your development. Sign up for an account at https://sandbox.evernote.com/Registration.action

In order to run the client client sample code, you need a developer token. Developer tokens make it easy to learn your way around the API without needing to worry about OAuth. Get one at https://sandbox.evernote.com/api/DeveloperToken.action

Including the SDK in your project
---------------------------------

The easiest way to incorporate the SDK into your Java project is to use Maven. If you're using Maven already, simply add a new dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.evernote</groupId>
    <artifactId>evernote-api</artifactId>
    <version>1.23</version>
</dependency>
```

If you'd prefer to build the SDK yourself, it's as simple as running

```bash
$ mvn package
```

You'll find `evernote-sdk-1.23.jar` in the target directory after the build completes. This single JAR contains everything needed to use the API.

Sample Code - Client
------------------------
The code in `sample/client/EDAMDemo.java` demonstrates the basics of using the Evernote API, using developer tokens instead of OAuth to simplify the authentication process while you're learning. Real applications that support multiple users need to use OAuth.

1. Build the SDK library

    ```bash
    $ mvn package
    ```
1. Open `sample/client/EDAMDemo.java`
1. Scroll down to the top of the `EDAMDemo` class and fill in your Evernote developer token.
1. On the command line, run the following command to compile the class:

    ```bash
    $ javac -classpath ../../target/evernote-api-1.23.jar EDAMDemo.java
    ```

1. On the command line, run the following command to execute the sample app:

    ```bash
    $ java -classpath .:../../target/evernote-api-1.23.jar EDAMDemo
    ````

Sample Code - OAuth
-----------------------
Real applications use OAuth to authenticate to the Evernote service. At the end of the OAuth flow you'll have an authentication token that you can use to access the Cloud API in the same way that the developer token is used in the client sample code. The code in `sample/oauth` demonstrate the OAuth authentication process.

1. Open the file `sample/oauth/src/main/webapp/index.jsp`
1. Fill in your Evernote API consumer key and secret
1. Build the sample project:

    ```bash
    $ cd sample/oauth
    $ mvn package
    ```

1. Deploy `sample/oauth/target/EDAMWebTest.war` to your servlet container (e.g. Tomcat)
1. Load the web application in your browser (e.g. `http://localhost:8080/EDAMWebTest`)
