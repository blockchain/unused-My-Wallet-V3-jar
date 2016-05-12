#!/bin/sh

openssl sha1 target/MyWalletHD-deterministic.jar > sha1_$1_jar.txt
mkdir target/MyWalletHD-deterministic
cd target/MyWalletHD-deterministic
jar xf ../MyWalletHD-deterministic.jar
cd ../..
find ./target/MyWalletHD-deterministic -type f -print0 | xargs -0 openssl sha1 > sha1_$1_all.txt