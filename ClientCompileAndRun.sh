#! /bin/bash
javac Client/Client.java
jar cfe Client.jar Client.Client Client/Client.class
java -jar Client.jar $1 $2
