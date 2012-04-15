Evernote SDK for Java version 1.21
==========================================

Overview
--------
This SDK contains wrapper code used to call the Evernote Cloud API from Java applications.

For Android-specific code and samples, see the [Evernote SDK for Android](http://www.github.com/evernote/evernote-sdk-android).

The SDK also contains two samples. The code in sample/client demonstrates the basic use of the SDK for client applications that authenticate using username and password. The code in sample/oauth demonstrates the basic use of the SDK for web  applications that authenticate using OAuth.

Prerequisites
-------------
In order to use the code in this SDK, you need to obtain an API key from http://dev.evernote.com/documentation/cloud. You'll also find full API documentation on that page.

In order to run the sample code, you need a user account on the sandbox service where you will do your development. Sign up for an account at https://sandbox.evernote.com/Registration.action 

Getting Started - Client
------------------------
Single-user client applications such as desktop Java apps may authenticate to the Evernote service using username and password. The code in sample/client/EDAMDemo.java demonstrates the basics.

1. Open sample/client/EDAMDemo.java
2. Scroll down to the top of the EDAMDemo class and fill in your Evernote API consumer key and secret.
3. On the command line, run the following command to compile the class:

    javac -classpath .:../../lib/libthrift.jar:../../lib/log4j-1.2.14.jar:../../lib/evernote-api-*.jar EDAMDemo.java
4. On the command line, run the following command to execute the sample app:

    java -classpath .:../../lib/libthrift.jar:../../lib/log4j-1.2.14.jar:../../lib/evernote-api-*.jar EDAMDemo username password

Replace username and password with the username and password of your Evernote sandbox user account.

Getting Started - OAuth
-----------------------
Web applications must use OAuth to authenticate to the Evernote service. The code in sample/oauth produces a simple WAR that demonstrates the OAuth authentication process.

1. Deploy the webapp to your servlet container (e.g. Tomcat)
2. Open the servlet container's webapps directory and find your exploded webapp (e.g. apache-tomcat-6.0.32/webapps/EDAMWebTest/
3. Open the file index.jsp
4. Fill in your Evernote API consumer key and secret.
5. Load the web application in your browser (e.g. http://localhost:8080/EDAMWebTest) 
