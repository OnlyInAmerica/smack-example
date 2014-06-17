# Smack Example

This is a test commandline application demonstrating use of [Flowdalic's Smack](https://github.com/Flowdalic/Smack) as a gradle submodule project dependency.

The xep-0174 branch is concerned with testing serverless XMPP functionality rebased from [ChatSecureAndroid](https://github.com/guardianproject/ChatSecureAndroid).

## Build & Run

Without modification this application will broadcast an XEP-0174 presence on the local WiFi. The application will print other presences as they are discovered and send them XMPP message stanzas. 

```
./gradlew run
```

## Open with IntelliJ

To edit the project, simply "Import Project" and point to the root project directory.

## Debugging mDNS

A source of client incompatibility is mDNS presence broadcasting and resolution. Below are techniques I've used to diagnose these conditions:

To browse (and automatically attempt to resolve, if supported) all mDNS XEP-0174 clients on your local network:

### JmDNS (all platforms)

JmDNS is the mDNS library used by this project. An executable JmDNS 3.4.1 .jar is included in `./tools` which is useful for debugging XEP-0174 client compatibility:

To browse and automatically attempt to resolve all XEP-0174 clients on your local network:

```
$ java -jar jmdns-3.4.1.jar -bs _presence._tcp local.
```

### dns-sd (Mac OS X)

To browse all services:

```
$ dns-sd -B _presence._tcp local
```

### avi-hi (Ubuntu)

To browse and automatically attempt to resolve all XEP-0174 clients on your local network:

```
$ avahi-browse _presence._tcp -r
```

## SECRETS.java

This file contains XMPP account data that is kept out of source control. It is required only to test traditional XMPP client to server connections over TCP.

From the project root directory, create SECRETS.java:

```
$ touch ./smack-example/src/main/java/pro/dbro/smack/SECRETS.java
$ open ./smack-example/src/main/java/pro/dbro/smack/SECRETS.java
```

And then copy your XMPP credentials in the following form:

```java
package pro.dbro.smack;

public class SECRETS {

    public static final String XMPP_SERVER      = "talk.google.com";
    public static final int XMPP_PORT           = 5222;
    public static final String XMPP_SERVICE     = "gmail.com";
    public static final String XMPP_USER        = "username@gmail.com";
    public static final String XMPP_PASS        = "password";

    public static final String XMPP_RECIPIENT   = "afriend@somewhere.com";
}
```
