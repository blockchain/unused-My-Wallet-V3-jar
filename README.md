# My-Wallet-HD-jar

[![Build Status](https://travis-ci.org/blockchain/My-Wallet-V3-jar.svg?branch=master)](https://travis-ci.org/blockchain/My-Wallet-V3-jar)

Encapsulation of basic Blockchain HD Wallet utilities.

## Build Process

Install Maven 3.2 or higher.

### Build:

mvn clean

mvn package

The build process will create MyWalletHD.jar in ./target : Can be included in any Java project 'as is' but requires inclusion of dependencies.

## Classes

### crypto : standard AES support

### multiaddr : multiaddr API support

### payload : JSON payload classes

### send : creating, signing, and pushing transactions to network

### util : support classes
