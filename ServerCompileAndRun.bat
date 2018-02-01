echo Compiling server
javac -cp "lib/commons-io-2.6.jar" Server/*.java
echo Building jar
jar -cvfm Server.jar Manifest.txt Server/*.class
cls
java -jar Server.jar
