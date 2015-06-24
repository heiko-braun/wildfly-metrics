#!/bin/sh

export MAVEN_OPTS="-agentpath:/Applications/jprofiler7/bin/macos/libjprofilerti.jnilib=port=8849,nowait"
mvn exec:java -Dexec.mainClass="org.wildfly.metrics.server.Server" 

