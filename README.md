# CurrencyConvert

Currency converter using Java sockets. Client sends request to server, and server responds. Allows multiple connections.

To compile Server
javac -cp "lib/commons-io-2.6.jar" Server/*.java
jar -cvfm Server.jar Manifest.txt Server/*.class

To run Server
java -jar Server.jar

To compile Client
javac Client.java

To run Client
java Client

For distribution
You must include the lib directory in the same directory as Server.jar file.