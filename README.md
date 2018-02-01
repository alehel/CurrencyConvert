# CurrencyConvert

Currency converter using Java sockets. Client sends request to server, and server responds. Allows multiple connections.

## To compile and run Server

**To compile:** ```javac -cp "lib/commons-io-2.6.jar" Server/*.java```

**To build jar:** ```jar -cvfm Server.jar Manifest.txt Server/*.class```

**To run jar:** ```java -jar Server.jar```

## To compile and run Client
**To compile:** ```jar cfe Client.jar Client.Client Client/Client.class```

**To run Client:** ```java -jar Client```

## For distribution
You *must* include the lib directory in the same directory as Server.jar file to run the Server application.