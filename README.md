# Features

* Immediate search access to local and distributed geospatial catalogues
* Up- and downloading of data, graphics, documents, pdf files and any other content type
* An interactive Web Map Viewer to combine Web Map Services from distributed servers around the world
* Online editing of metadata with a powerful template system
* Scheduled harvesting and synchronization of metadata between distributed catalogs
* Support for OGC-CSW 2.0.2 ISO Profile, OAI-PMH, Z39.50 protocols
* Fine-grained access control with group and user management
* Multi-lingual user interface
*


# ECAS setup

Installation documentation: https://webgate.ec.europa.eu/CITnet/confluence/display/IAM/ECAS+Tomcat+Client

To compile the ECAS-refapp solution, we have to add the refapp and the ecas-tomcat dependencies to the local maven repository.

Download ```ecas-tomcat-7.0-3.11.1.jar``` from https://webgate.ec.europa.eu/CITnet/confluence/display/IAM/Downloads-Tomcat

Download ```refapp-support-security-ecas-file-1.2.0-SNAPSHOT.jar``` from https://webgate.ec.europa.eu/CITnet/nexus/service/local/artifact/maven/redirect?r=refapp-snapshots&g=eu.europa.ec.digit&a=refapp-support-security-ecas-file&v=1.2.0-SNAPSHOT&e=jar

```
$ mvn install:install-file -Dfile=refapp-support-security-ecas-file-1.2.0-SNAPSHOT.jar -DgroupId=eu.europa.ec.digit -Dpackaging=jar -DartifactId=refapp-support-security-ecas-file -Dversion=1.2.0-SNAPSHOT -Dsources=/home/ec2-user/refapp-support-security-ecas-file-1.2.0-SNAPSHOT-sources.jar 

$ mvn install:install-file -Dfile=ecas-tomcat-7.0-3.11.1.jar -DgroupId=eu.europa.ec.digit -DartifactId=ecas-tomcat -Dversion=7.0-3.11.1 -Dpackaging=jar
```

Once the Tomcat installation is configured for this ecas client, we still have to add a system variable to the startup script on tomcat. This is because the ECAS-tomcat client requires that the log4j library is installed on the lib folder of tomcat and not on the lib folder of the app. So, we have to remove it from the app (done on the ```pom.xml``` files of geoNetwork) and add the following line to Tomcat: ```-Dlog4j.ignoreTCL=true```


## Certificates

Download from https://webgate.ec.europa.eu/CITnet/confluence/display/IAM/Downloads-Certificates

Installation:

```
$keytool -import -v -keystore cacerts -storepass changeit -alias EuropeanCommissionRootCA -file EuropeanCommissionRoot.cer

$keytool -import -v -keystore cacerts -storepass changeit -alias CommisSignClassA -file CommisSign.cer
```

## Tomcat 7.0

### Installation

Copy ```ecas-tomcat-7.0-3.11.1.jar``` and ```log4j-1.2.14.jar``` (https://webgate.ec.europa.eu/CITnet/nexus/service/local/artifact/maven/redirect?r=atlassian&g=log4j&a=log4j&v=1.2.12&e=jar) to ```$TOMCAT_PATH/lib```

### Add the ECAS Authenticator for Tomcat

Download the file ```ecas-tomcat-7.0-3.11.1-config.zip``` from https://webgate.ec.europa.eu/CITnet/confluence/display/IAM/Downloads-Tomcat

Extract to ```$TOMCAT_PATH/lib```

### Realm

Edit ```$TOMCAT_PATH/conf/server.xml``` and add in this Realm ```allRolesMode="authOnly"```.

```
<Realm className="org.apache.catalina.realm.LockOutRealm" allRolesMode="authOnly">
  <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
         resourceName="UserDatabase" />
</Realm>
```
