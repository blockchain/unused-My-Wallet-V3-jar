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

Command line switches:

-a create/restore with this number of accounts (default = 2 accounts)
-c (no arguments) create new wallet (default = 12 words)
-f restore wallet from JSON using this password
-l use this Locale for BIP 39 nword list
-p use this passphrase (BIP 39)
-r restore wallet from hex, mnemonic, or ':' separated XPUBs (write-only wallet)
-t save wallet to JSON using this password
-w create wallet using this number of words for mnemonic
        
