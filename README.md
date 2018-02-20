# CurrencyConvert

A simple server and client application for performing currency conversions. Server and client communicate using a TCP connection and the Server is capable of handling multiple clients at once. Currency rates are automatically downloaded from the European Central Bank when launched. Remember to edit the Client.java file to reflect the Server applications IP address. Alternatively, use 127.0.0.1 if both are running on the same machine. 

## To compile and run Server

**To compile:** ```javac -cp "lib/commons-io-2.6.jar" Server/*.java```

**To build jar:** ```jar -cvfm Server.jar Manifest.txt Server/*.class```

**To run jar:** ```java -jar Server.jar```

## To compile and run Client
**To compile:** ```jar cfe Client.jar Client.Client Client/Client.class```

**To run Client:** ```java -jar Client```

## For distribution
You *must* include the lib directory in the same directory as Server.jar file to run the Server application.
