[![Build Status][Travis badge]][Travis build]

[Travis badge]: https://travis-ci.org/marvec/encryptor.svg?branch=master
[Travis build]: https://travis-ci.org/marvec/encryptor

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.marvec/encryptor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.marvec/encryptor/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# encryptor
Encryption web service and utility library

## Using the library

From Maven central:

```xml
   <dependency>
      <groupId>org.marvec</groupId>
      <artifactId>encryptor</artifactId>
      <version>1.0</version>
   </dependency>
```

Or compile yourself:

```
$ mvn install
```

Now you can find the library in `encryptor-util/target/encryptor-util-$VERSION.jar`.

## Usage of the web service

```
$ mvn install
$ cd encryptor
$ mvn exec:exec
```

Most APIs consume private and public keys. When the keys are omitted, the built-in keys from classpath are used.

For encryption and decryption, when you specify just one key (private or public), that key will be used for the operation.
By default encryption works with private key and decryption with public key (when no keys or both are specified).

Signature creation and verification is done with the private key. Setting the public key for this operation has no effect.

The server is now up and running. You can access it at the following URLs:

```
curl -X POST http://localhost:8080/encrypt -d '{"message":"hello", "privateKey":"PEM formatted key", "publicKey":"PEM formatted key"}'
curl -X POST http://localhost:8080/decrypt -d '{"encryptedPayload" : "...", "privateKey":"PEM formatted key", "publicKey":"PEM formatted key"}'
curl -X POST http://localhost:8080/sign -d '{"message":"hello", "privateKey":"PEM formatted key"}'
curl -X POST http://localhost:8080/verify -d '{"message":"hello", "signature":"...", "privateKey":"PEM formatted key"}'
```

## Configuration

The port where the application runs can be configured via the _encryptor.port_ system property. The service always runs at localhost as it
provides its services unsecured. It is supposed to install a public HTTPS enabled proxy in front of it.

The number of threads for processing the requests can be set with the _encryptor.threads_ system property.

## API usage

It is possible to use encryptor just as a library in your own projects.
However, you might suffer from having unnecessary dependencies on your classpath.
In this case, you can definitely exclude _vertx-web_ in your Maven pom file.
Vertx core is needed for JSON en-/de-coding.

Sample usage of the API:

```java
   final EncryptionRequest er = new EncryptionRequest();
   er.setPublicKey(publicKey);
   er.setMessage(message);
   final byte[] encryptedMessage = er.process().getEncryptedPayload();

   final DecryptionRequest dr = new DecryptionRequest();
   dr.setPrivateKey(privateKey);
   dr.setMessage(encryptedMessage);

   final String response = dr.process().getPayload();
```

It does not matter whether you use private or public key for encryption. It is up to your needs. However, always configure
only one type of key for the request. If you need more details see the source code or submit an issue.