# My-Wallet-HD-jar

Encapsulation of basic Blockchain HD Wallet utilities. A work in progress.

## Build Process

Install Maven 3.2 or higher.

### Build:

mvn clean

mvn package

Two .jar files will be created in the directory ./target :

MyWalletHD.jar : Can be included in any Java project 'as is' but requires inclusion of dependencies. Main.java harness not included.

MyWalletHD-jar-with-dependencies.jar : includes all dependencies and can be run from the command line using the Main.java harness (see command line switches below).
