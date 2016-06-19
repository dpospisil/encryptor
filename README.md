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

The server is now up and running. You can access it at the following URLs:

```
curl -X POST http://localhost:8080/encrypt/private -d '{"payload":"hello"}'
curl -X POST http://localhost:8080/encrypt/public -d '{"payload":"hello"}'
curl -X POST http://localhost:8080/sign -d '{"payload":"hello"}'
curl -X POST http://localhost:8080/decrypt/private -d '{"encryptedPayload" : "..."}'
curl -X POST http://localhost:8080/decrypt/public -d '{"encryptedPayload" : "..."}'

```