Evernote SDK for Java 
==========================================

Evernote API version 1.21


Overview
--------
This SDK contains wrapper code used to call the Evernote Cloud API from Java applications.

For Android-specific code and samples, see the [Evernote SDK for Android](http://www.github.com/evernote/evernote-sdk-android).

The SDK also contains two samples. The code in sample/oauth demonstrates the basic use of the SDK. The code in sample/client also demonstrates the basic use of API, but uses developer tokens instead of OAuth for authentication.

Prerequisites
-------------
In order to use the code in this SDK, you need to obtain an API key from http://dev.evernote.com/documentation/cloud. You'll also find full API documentation on that page.

In order to run the sample code, you need a user account on the sandbox service where you will do your development. Sign up for an account at https://sandbox.evernote.com/Registration.action 

In order to run the client client sample code, you need a developer token. Get one at https://sandbox.evernote.com/api/DeveloperToken.action

Getting Started - Client
------------------------
The code in sample/client/EDAMDemo.java demonstrates the basics of using the Evernote API, using developer tokens instead of OAuth to simplify the authentication process while you're learning. Real applications that support multiple users need to use OAuth.

1. Open sample/client/EDAMDemo.java
2. Scroll down to the top of the EDAMDemo class and fill in your Evernote developer token.
3. On the command line, run the following command to compile the class:

    javac -classpath .:../../lib/libthrift.jar:../../lib/evernote-api-*.jar EDAMDemo.java
4. On the command line, run the following command to execute the sample app:

    java -classpath .:../../lib/libthrift.jar:../../lib/evernote-api-*.jar EDAMDemo

Getting Started - OAuth
-----------------------
Applications use OAuth to authenticate to the Evernote service. The code in sample/oauth demonstrate the OAuth authentication process.

1. Open the file sample/oauth/src/main/webapp/index.jsp
1. Fill in your Evernote API consumer key and secret
1. Build the sample project:

    cd sample/oauth
    
    mvn package
1. Deploy sample/oauth/target/EDAMWebTest.war to your servlet container (e.g. Tomcat)
1. Load the web application in your browser (e.g. http://localhost:8080/EDAMWebTest) 

If you want to modify and build the WAR yourself, 