
# ESC test responder application

This demonstration application provides a simple echo responder for 
ESC messages.

There are two branches:

 - **main** runs under Glassfish 5.x; requires J2EE v7 
 - **tomcat** runs under Tomcat v9.x 
 
 
The context root for this application is "/dev/esc/rs". This is configured in the _web.xml_ file.

Web resources are exposed under the "listen" application path. This is configured in the _ApplicationConfig.java_ class.

There are two REST resource end points:

Ping message listener

  - PUT _/dev/esc/rs/listen/ping_

DpacStatus message listener

  - PUT _/dev/esc/rs/listen/dpac_

REST resources are fully described in the _application.wadl_ file. 


License: Berkeley - as-is, no constraint
