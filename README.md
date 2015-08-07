# My-Wallet-HD-jar

BIP44 extension of Bitcoinj. Includes full support for BIP39 including non-English word lists.

## Build Process

Install Maven.

Build:

mvn clean
mvn package

Two .jar files will be created in the directory target/ :

BitcoinjBIP44-jar-with-dependencies.jar : includes all dependencies and can be run from the command line using the Main.java harness.

BitcoinjBIP44.jar : Can be included in any Java project as is but requires inclusion of dependencies.

Run using Main.java harness:

java -jar target/BitcoinjBIP44-jar-with-dependencies.jar
