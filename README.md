# encryptor
Encryption web service and utility library

## Using the library

```
mvn install
```

Now you can find the library in encryptor-util/target/encryptor-util-$VERSION.jar.

## Usage of the web service

```
mvn install
cd encryptor
mvn exec:exec
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