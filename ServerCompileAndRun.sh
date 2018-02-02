#! /bin/bash

javac -cp "lib/commons-io-2.6.jar" Server/*.java
jar -cvfm Server.jar manifest.txt Server/*.class
java -jar Server.jar

