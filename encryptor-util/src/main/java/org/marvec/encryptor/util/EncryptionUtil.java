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
package org.marvec.encryptor.util;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Provides basic assymetric cryptography methods to encrypt, decrypt, sign and verify message with a RSA key pair.
 *
 * @author <a href="mailto:martin@vecerovi.com">Martin Večeřa</a>
 */
public class EncryptionUtil {

   /**
    * String to hold name of the encryption algorithm.
    */
   private static final String ALGORITHM = "RSA";

   /**
    * Default charset used to input/output messages.
    */
   private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

   /**
    * String to hold the name of the private key file.
    */
   private static final String PRIVATE_KEY_RESOURCE = "/org/marvec/encryptor/private_key.pem";

   /**
    * String to hold name of the public key file.
    */
   private static final String PUBLIC_KEY_RESOURCE = "/org/marvec/encryptor/public_key.pem";

   /**
    * Public RSA key.
    */
   private final PublicKey publicKey;

   /**
    * Private RSA key.
    */
   private final PrivateKey privateKey;

   /**
    * Type of key to be used for encryption/decryption.
    */
   public enum KeyType {
      PRIVATE, PUBLIC;
   }

   /**
    * Gets the encryption utility initialized with default key pair from the resources.
    *
    * @throws EncryptionException
    *       When it was not possible to initialize the keys.
    */
   public EncryptionUtil() throws EncryptionException {
      try {
         publicKey = getPemPublicKey(readResource(PUBLIC_KEY_RESOURCE));
         privateKey = getPemPrivateKey(readResource(PRIVATE_KEY_RESOURCE));
      } catch (IOException e) {
         throw new EncryptionException("Unable to initialize encryptor: ", e);
      }
   }

   /**
    * Gets the encryption utility initialized with the provided key pair.
    *
    * @param publicKey
    *       Public key as a string.
    * @param privateKey
    *       Private key as a string.
    * @throws EncryptionException
    *       When it was not possible to initialize the keys.
    */
   public EncryptionUtil(final String publicKey, final String privateKey) throws EncryptionException {
      this.publicKey = publicKey != null ? getPemPublicKey(publicKey) : null;
      this.privateKey = privateKey != null ? getPemPrivateKey(privateKey) : null;
   }

   /**
    * Reads the given resource as a string.
    *
    * @param resourceName
    *       The name of the resource to read.
    * @return The content of the resource as a string.
    * @throws IOException
    *       When it was not possible to read the resource.
    */
   private String readResource(final String resourceName) throws IOException {
      return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resourceName), DEFAULT_CHARSET)).lines().collect(Collectors.joining("\n"));
   }

   /**
    * Initializes a private key from a string.
    *
    * @param keyString
    *       String representation of the key.
    * @return An initialized private key.
    * @throws EncryptionException
    *       When it was not possible to initialize the key.
    */
   private PrivateKey getPemPrivateKey(final String keyString) throws EncryptionException {
      try {
         final String privKeyPEM = keyString.replace("-----BEGIN PRIVATE KEY-----\n", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\n", "");

         final Base64 b64 = new Base64();
         final byte[] decoded = b64.decode(privKeyPEM);

         final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
         final KeyFactory kf = KeyFactory.getInstance(ALGORITHM);

         return kf.generatePrivate(spec);
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
         throw new EncryptionException("Unable to obtain private key: ", e);
      }
   }

   /**
    * Initializes a public key from a string.
    *
    * @param keyString
    *       String representation of the key.
    * @return An initialized public key.
    * @throws EncryptionException
    *       When it was not possible to initialize the key.
    */
   private PublicKey getPemPublicKey(final String keyString) throws EncryptionException {
      try {
         final String publicKeyPEM = keyString.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "");

         final Base64 b64 = new Base64();
         final byte[] decoded = b64.decode(publicKeyPEM);

         final X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
         final KeyFactory kf = KeyFactory.getInstance(ALGORITHM);

         return kf.generatePublic(spec);
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
         throw new EncryptionException("Unable to obtain public key: ", e);
      }
   }

   /**
    * Encrypts the given message with the specified key type (either public or private).
    *
    * @param text
    *       The message to encrypt.
    * @param keyType
    *       THe key type to be used.
    * @return The encrypted message.
    * @throws EncryptionException
    *       When it was not possible to encrypt the message.
    */
   public byte[] encrypt(final String text, final KeyType keyType) throws EncryptionException {
      final Key key = keyType == KeyType.PRIVATE ? privateKey : publicKey;

      try {
         // get an RSA cipher object and print the provider
         final Cipher cipher = Cipher.getInstance(ALGORITHM);
         // encrypt the plain text using the public key
         cipher.init(Cipher.ENCRYPT_MODE, key);
         final byte[] cipherText = cipher.doFinal(text.getBytes(DEFAULT_CHARSET));

         return cipherText;
      } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
         throw new EncryptionException("Unable to encrypt message: ", e);
      }
   }

   /**
    * Decrypts the given encrypted message with the specified key type.
    *
    * @param text
    *       The encrypted message to be decrypted.
    * @param keyType
    *       The key type to be used.
    * @return The decrypted message.
    * @throws EncryptionException
    *       When it was not possible to decrypt the message.
    */
   public String decrypt(final byte[] text, final KeyType keyType) throws EncryptionException {
      final Key key = keyType == KeyType.PRIVATE ? privateKey : publicKey;

      try {
         // get an RSA cipher object and print the provider
         final Cipher cipher = Cipher.getInstance(ALGORITHM);
         // decrypt the text using the private key
         cipher.init(Cipher.DECRYPT_MODE, key);
         byte[] dectyptedText = cipher.doFinal(text);

         return new String(dectyptedText, DEFAULT_CHARSET);
      } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
         throw new EncryptionException("Unable to decrypt message: ", e);
      }
   }

   /**
    * Signs the message with the private key.
    *
    * @param message
    *       The message to be signed.
    * @return The signature.
    * @throws EncryptionException
    *       When it was not possible to sign the message.
    */
   public String sign(final String message) throws EncryptionException {
      try {
         Signature sign = Signature.getInstance("SHA1withRSA");
         sign.initSign(privateKey);
         sign.update(message.getBytes(DEFAULT_CHARSET));

         return new String(Base64.encodeBase64(sign.sign()), DEFAULT_CHARSET);
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
         throw new EncryptionException("Unable to sign message: ", e);
      }
   }

   /**
    * Verifies whether the signature matches the given message.
    *
    * @param message
    *       The original message.
    * @param signature
    *       The signature of the message.
    * @return True if and only if the signature matches the given message.
    * @throws EncryptionException
    *       When it was not possible to verify the message signature.
    */
   public boolean verify(final String message, final String signature) throws EncryptionException {
      try {
         Signature sign = Signature.getInstance("SHA1withRSA");
         sign.initVerify(publicKey);
         sign.update(message.getBytes(DEFAULT_CHARSET));
         return sign.verify(Base64.decodeBase64(signature.getBytes(DEFAULT_CHARSET)));
      } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
         throw new EncryptionException("Unable to verify message: ", e);
      }
   }
}