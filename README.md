Komuso is a very simple MBean server monitoring tool, developed by Yusuke Yamamoto,
originally distributed at http://yusuke.homeip.net/komuso/en/.

This is a modified version by atWare, Inc.

## Introduction

Komuso is a very simple MBean server monitoring tool.

With Komuso, you can monitor any MBean server including WebLogic Server, Tomcat, JBoss, Oracle Application Server, WebSphere and just JVM locally or remotely. Komuso is unique for its simplicity. Komuso consists of only 150 lines of code.

This makes Komuso easier to understand and customize.


## Requirements

* OS: Windows or any flavor of Unix that supports Java.
* JVM: Java5 or later
* Application Server: Any application server that supports JMX Remote API(JSR160).

JBoss AS 4.0.2 or later, and WebLogic Server 9.0 or later support JMX Remote API.

You can use Komuso 1.0 for WebLogic 8.1 or prior versions which don't support JMX Remote API.

## Changes by atWare, Inc.

TODO: Add changes

## Usage

1. Configuring Komuso

   Edit `setEnv.sh` or `setEnv.cmd` to point your `JAVA_HOME` propertly.
2. Configuring your application

   * Configuring your application

     You can get your application JMX Remote ready by just adding following two system properties:

     ```none
     -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.authenticate=false
     ```
   * Configuring JBoss

     Follow the example below to make JBoss MBeans monitorable.

     * e.g. Windows

       ```bat
       @echo off
       rem -------------------------------------------------------------------------
       rem JBoss Bootstrap Script for Win32
       rem -------------------------------------------------------------------------
       set JAVA_OPTS="%JAVA_OPTS% -Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl"
       set JAVA_OPTS="%JAVA_OPTS% -Djboss.platform.mbeanserver"
       set JAVA_OPTS="%JAVA_OPTS% -Dcom.sun.management.jmxremote"
       set JAVA_OPTS="%JAVA_OPTS% -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.authenticate=false"
       ```
     * e.g. Unix

       ```bash
       #!/bin/sh
       ### ====================================================================== ###
       ##                                                                          ##
       ##  JBoss Bootstrap Script                                                  ##
       ##                                                                          ##
       ### ====================================================================== ###
       JAVA_OPTS="$JAVA_OPTS -Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl"
       JAVA_OPTS="$JAVA_OPTS -Djboss.platform.mbeanserver"
       JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
       JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.authenticate=false"
       ```
   * Configuring WebLogic Server

     ```none
     There's nothing you need to configure on the server side. But you need to copy weblogic.jar to lib/ directory to make Komuso t3 protocol aware.
     ```
   * Configuring Tomcat

     Add one single line in your startup.bat/sh as following:

     * e.g. Windows

       ```bat
       @echo off
       if "%OS%" == "Windows_NT" setlocal
       rem ---------------------------------------------------------------------------
       rem Start script for the CATALINA Server
       rem
       rem $Id: startup.bat 302918 2004-05-27 18:25:11Z yoavs $
       rem ---------------------------------------------------------------------------
       set CATALINA_OPTS="-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.authenticate=false"
       ```
     * e.g. Unix

       ```bash
       #!/bin/sh
       # -----------------------------------------------------------------------------
       # Start Script for the CATALINA Server
       #
       # $Id: startup.sh 385888 2006-03-14 21:04:40Z keith $
       # -----------------------------------------------------------------------------
       export CATALINA_OPTS="-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.authenticate=false"
       ```
3. Preparing a properties file

   Prepare a text file whose name ends with ".properties" as below.

   Each property should be written without any line break.

   Komuso is shipped with sample properties files named "komuso.properties" for Tomcat / standalone JVM, "komuso-wls.properties" for WebLogic.

   ```properties
   jmx.remote.protocol.provider.pkgs=JMX Remote protocol provier package name
   JMXServiceURL=JMX Service URL
   java.naming.security.principal=User ID for the connection
   java.naming.security.credentials=Password for the connection
   ```

   * e.g. A standalone JVM is running with an MBean Server listening on 8999 and authentication is disabled.

     ```properties
     jmx.remote.protocol.provider.pkgs=
     JMXServiceURL=service:jmx:rmi:///jndi/rmi://localhost:8999/jmxrmi
     java.naming.security.principal=
     java.naming.security.credentials=
     ```

   * e.g. WebLogic Server is listening on port 7001 and admin userID/password combination is weblogic/gumby1234

     ```properties
     jmx.remote.protocol.provider.pkgs=weblogic.management.remote
     JMXServiceURL=service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
     java.naming.security.principal=weblogic
     java.naming.security.credentials=gumby1234
     ```
4. Generating MBeanDoc

   MBeanDoc is a JavaDoc like HTML document which describes all MBean instances / attributes deployed on the JMX Server.

   You can generate MBeanDoc by issuing `mbeandoc.sh` or `mbeandoc.cmd`.

   MBeanDoc will be generated under the `KOMUSO_HOME/mbeandump/` directory and automatically opened with the default web browser(Win/Mac only).

   You can find [a sample MBeanDoc here](http://yusuke.homeip.net/komuso/en/mbeandocen/index.html).

   * Usage

     ```bash
     mbeandoc.sh <YOUR_KOMUSO.properties>
     ```

     or

     ```bash
     mbeandoc.cmd <YOUR_KOMUSO.properties>
     ```
5. Selecting MBean attributes

   MBeanDoc has two tabs. On the "MBeanDoc" tab, you choose MBean attributes you want to monitor.

   On the "Settings" tab, you confirm the chosen ones.

   You can also set the CSV header of each attribute on the tab.

   A complete setting is generated in real time right below the "Generated Settings" label.

   Unfortunately MBeanDoc doesn't automagically store the setting into the properties file.

   You need to manually override the existing properties file with the generated setting.
6. Monitoring

   It's all set! You can monitor the JMX Service by just running `komuso.sh` or `komuso.cmd`.

   * Running

     ```bash
     komuso.sh <YOUR_KOMUSO.properties>
     ```

     or

     ```bash
     komuso.cmd <YOUR_KOMUSO.properties>
     ```

   * About monitoring

     Sampled MBean attribute values will be written into standard output and `komuso.csv` in CSV format by default.

     Komuso will also report the status of the connection, caught exceptions to `komuso_status.log` separately.

     Log files will be rolled over every midnight and the latest 10 files will be kept.

     Komuso uses [Logback](http://logback.qos.ch/) to write messages.

     You can edit `logback.xml` to configure logging policy.

     For more information, please visit the [Logback Documentation page](http://logback.qos.ch/manual/appenders.html#TimeBasedRollingPolicy).

## License

Komuso 2.0 is released under the BSD license.

Komuso 2.0 bundles Apache Velocity which is released under the Apache Software License .

Komuso 2.0 bundles Logback which is released under GNU Lesser General Public License.

## Contribution

1. [Fork it](https://github.com/atware/komuso/fork)
2. Create your feature branch ( `git checkout -b my-new-feature` )
3. Commit your changes ( `git commit -am 'Add some feature'` )
4. Push to the branch ( `git push origin my-new-feature` )
5. Create new Pull Request
