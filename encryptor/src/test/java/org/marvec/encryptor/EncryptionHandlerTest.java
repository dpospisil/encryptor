/*
 * -----------------------------------------------------------------------\
 * Copyright (C) 2016 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.marvec.encryptor;

import static org.testng.Assert.*;

import org.marvec.encryptor.api.DecryptionRequest;
import org.marvec.encryptor.api.EncryptedMessage;
import org.marvec.encryptor.api.EncryptionRequest;
import org.marvec.encryptor.api.SimpleMessage;
import org.marvec.encryptor.util.EncryptionException;
import org.marvec.encryptor.util.EncryptionUtil;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class EncryptionHandlerTest {

   private static final String MESSAGE = "Hello world!";

   private static final String myPrivateKey = "-----BEGIN PRIVATE KEY-----\n"
         + "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAM9vBFncVx6mHRn4\n"
         + "5c2MVMVhggBPLeP+MvWwjYr+Ot9mXBCnEVdZXrWtMZ8KVgOE5IR/ywhldrM/jU1r\n"
         + "HiXnHEuB9VBzyMJZfW9Pt2PKKPSYGCQt+NNRRf7HyIA0Mrj7x1YwI29l80bZxDSr\n"
         + "tsI2NYJ0h02c57IwspmZutpuwCTrAgMBAAECgYBNhHRAzIm2B2e6q/vcy4NmW6EE\n"
         + "UpKYsAcFL5xFg+omUA85neVqGhi9leKER42LNR9csJEEKz6nj6lTdCNJEMvOc1sQ\n"
         + "SujCvu+jeBljojq6o+pQut43oOkAQypXN8dfDiEznmOCITGTkvQRWev8A7sSibSI\n"
         + "nxfxnu01k0zeK+pO6QJBAPOrOvXFQxmMuqIgyxyS9EDRvr8ytpp1ncSQ1c/iwdzk\n"
         + "y9Zs9xrAUQHjt+tiE5v+7FHBthsOlnjPRp/it7WYdi8CQQDZ7lqLObgt0k4DYvD8\n"
         + "yyptfNlCLBhanRBzcWyCiSeIafWXFySBzsMULtalSSyVLzw+cJCvd/5+W4FhkswS\n"
         + "WAoFAkEAqYYYiAKMc0sYUVGVXbTToAEMvwKuTfnEIIxPZMky7NZ1BiJbLE5eTX07\n"
         + "aNScJeyORzcI1fwpLbWvQe8+tmVy2wJBAKdTkiOZ0qcj9RA2lI+UMmRUWr+q+ZwB\n"
         + "QlNRAB6NzlO3/3/dngMozLGfcOEBeQCmftv2M7D2Mem0unc/bf6bqD0CQBi3jRxV\n"
         + "xrzBq3sADxZMdJeEMzqk+MOgvTTsXbycgcLqdiOwyUE7r58DeSPN3oxuho6CSTc3\n"
         + "w6cb6rpWZqnyzhU=\n"
         + "-----END PRIVATE KEY-----\n";

   private static final String myPublicKey = "-----BEGIN PUBLIC KEY-----\n"
         + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDPbwRZ3Fceph0Z+OXNjFTFYYIA\n"
         + "Ty3j/jL1sI2K/jrfZlwQpxFXWV61rTGfClYDhOSEf8sIZXazP41Nax4l5xxLgfVQ\n"
         + "c8jCWX1vT7djyij0mBgkLfjTUUX+x8iANDK4+8dWMCNvZfNG2cQ0q7bCNjWCdIdN\n"
         + "nOeyMLKZmbrabsAk6wIDAQAB\n"
         + "-----END PUBLIC KEY-----\n";

   private static int port = 8090;

   @BeforeMethod
   public void waitAfterTest() throws InterruptedException {
      System.setProperty(EncryptorConst.PORT_NUMBER, "" + (port++));
   }

   @Test
   public void testDefaultKey() throws EncryptionException, InterruptedException {
      final Boot boot = new Boot();
      new Thread(boot::startServer).start();

      final Vertx vertx = Vertx.vertx();
      HttpClient client = vertx.createHttpClient();

      // First, encrypt the message
      EncryptionRequest req = new EncryptionRequest();
      req.setMessage(MESSAGE);

      final Semaphore s = new Semaphore(0);
      final StringBuffer resp1 = new StringBuffer();

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/encrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp1.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(req));

      s.acquire();

      // Second, decrypt the response
      final StringBuffer resp2 = new StringBuffer();
      DecryptionRequest dec = new DecryptionRequest();
      dec.setMessage(Json.decodeValue(resp1.toString(), EncryptedMessage.class).getEncryptedPayload());

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/decrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp2.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(dec));

      s.acquire();

      Assert.assertEquals(Json.decodeValue(resp2.toString(), SimpleMessage.class).getPayload(), MESSAGE);

      boot.stop();
   }

   @Test
   public void testCustomKey() throws EncryptionException, InterruptedException {
      final Boot boot = new Boot();
      new Thread(boot::startServer).start();

      final Vertx vertx = Vertx.vertx();
      HttpClient client = vertx.createHttpClient();

      // First, encrypt the message
      EncryptionRequest req = new EncryptionRequest();
      req.setMessage(MESSAGE);
      req.setPrivateKey(myPrivateKey);

      final Semaphore s = new Semaphore(0);
      final StringBuffer resp1 = new StringBuffer();

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/encrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp1.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(req));

      s.acquire();

      // Second, decrypt the response but without the right public key
      final StringBuffer resp2 = new StringBuffer();
      DecryptionRequest dec = new DecryptionRequest();
      dec.setMessage(Json.decodeValue(resp1.toString(), EncryptedMessage.class).getEncryptedPayload());

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/decrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp2.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(dec));

      s.acquire();

      Assert.assertEquals(resp2.toString(), ""); // no message was decrypted

      // Third, decrypt the response with the right key
      final StringBuffer resp3 = new StringBuffer();
      dec = new DecryptionRequest();
      dec.setMessage(Json.decodeValue(resp1.toString(), EncryptedMessage.class).getEncryptedPayload());
      dec.setPublicKey(myPublicKey);

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/decrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp3.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(dec));

      s.acquire();

      Assert.assertEquals(Json.decodeValue(resp3.toString(), SimpleMessage.class).getPayload(), MESSAGE);

      boot.stop();
   }

   @Test
   public void testCustomKeyPubFirst() throws EncryptionException, InterruptedException {
      final Boot boot = new Boot();
      new Thread(boot::startServer).start();

      final Vertx vertx = Vertx.vertx();
      HttpClient client = vertx.createHttpClient();

      // First, encrypt the message
      EncryptionRequest req = new EncryptionRequest();
      req.setMessage(MESSAGE);
      req.setPublicKey(myPublicKey);

      final Semaphore s = new Semaphore(0);
      final StringBuffer resp1 = new StringBuffer();

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/encrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp1.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(req));

      s.acquire();

      // Second, decrypt the response but without the right public key
      final StringBuffer resp2 = new StringBuffer();
      DecryptionRequest dec = new DecryptionRequest();
      dec.setMessage(Json.decodeValue(resp1.toString(), EncryptedMessage.class).getEncryptedPayload());

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/decrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp2.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(dec));

      s.acquire();

      Assert.assertEquals(resp2.toString(), ""); // no message was decrypted

      // Third, decrypt the response with the right key
      final StringBuffer resp3 = new StringBuffer();
      dec = new DecryptionRequest();
      dec.setMessage(Json.decodeValue(resp1.toString(), EncryptedMessage.class).getEncryptedPayload());
      dec.setPrivateKey(myPrivateKey);

      client.post(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080), "127.0.0.1", "/decrypt", responseHandler -> {
         responseHandler.bodyHandler(bodyHandler -> {
            resp3.append(new String(bodyHandler.getBytes()));
            s.release();
         });
      }).end(Json.encodePrettily(dec));

      s.acquire();

      Assert.assertEquals(Json.decodeValue(resp3.toString(), SimpleMessage.class).getPayload(), MESSAGE);

      boot.stop();
   }

}