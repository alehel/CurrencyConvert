echo compiling source and building jar
javac Client/Client.java
jar cfe Client.jar Client.Client Client/Client.class
cls
java -jar Client.jar
pause