/*
 * -----------------------------------------------------------------------\
 * Copyright (C) 2014 - 2016 the original author or authors.
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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class EncryptionUtilTest {

   private static final String ORIGINAL_MESSAGE = "Hello World!";

   private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
         + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDBqPcPVprF3kG5\n"
         + "0XxavWU170lFioEizJa8E4TnNl3iAtBh/my5rGV5P5I/6UrF6sxKfA5dJMAOFayK\n"
         + "Z4KBdDPIWvOF5B0jMS2ZRSo3lfIekky/IVewRSqJfupr7Oo530wJSN6Y3FUcvVkr\n"
         + "/jdSQPVd1CzXOaHeewb5sVeeCjsxfN6VIX4SUDeCWKQ6zkL1ALVG3A5RYe6KUq6X\n"
         + "NfDxOQ9VuEvMAayk1kBCZS0J5xg3eNPYxawBi6tVHeoSCh6e+2lgY/0T6zBBZvpK\n"
         + "R/DDwiHoYSEatWK4whvoS3wOw3EIXgjgjJ6LAWQ6QUCGeRUkXsti4a08Kn8Hv5PR\n"
         + "gHjlJwYhAgMBAAECggEAM8YpbvdXX4kBB12jIls6IMY9T5Ms3RHXRqbtRCc7yD26\n"
         + "Wkh75tPZOSYuwllrfSg9v7gU2wAFPH685y+vK07WvTzbmDMZOxxdtXlRRRUYauXw\n"
         + "ELUoTV7gdWvx3jKzmA9Ds8PAhxt4wbs5iNJpFdt5/cDI0C1/QgygG8c2xnCzzJG/\n"
         + "ky2RmvnsHadsIHFn4BVMofyHaS5MKHfE0qZO9eGqOqclmeKB2i2lOlZB2D+C9ADE\n"
         + "5bBIr/7qjn+YdL3YUUyjGiBoecFWcfjFU1bgENByCWXs2sp2B6cUXv6g7QiBuIQR\n"
         + "oeMzrGhvm43htNON3QRuX0Q0UmmYhxTJh7mS+FXP1QKBgQDkp/g8XrHC4k4ZxdPV\n"
         + "Oa2jrSNcwvqxxKs/472Vdy4YKxlQoIESaOIhAXdDZ/aq4xXFowbmprFgsvgxWeNv\n"
         + "VZiC3qYrFkrDks2d8bv+BMRPZNTIEdXt1cNXFSJVagYtFehIswyWqyFouHM1EBrc\n"
         + "gjSBgMxdxhUafORsWyPFnHdRawKBgQDY0aHBzaIuFtmrjYvVQicQgB7c3hxZ0n+K\n"
         + "QFkNM98Z/ptpjkz00Ad5zGnxbW++pawVOy2IHa6cbz2CTYtpj4jaoai+C1WkbugJ\n"
         + "bTfmTU9l3lPBa4le9+5juANw5tkS5cQbhs4iF6iEyuHTBjH/hnGZcdU0A0GxvLvK\n"
         + "OMon9FNNowKBgQDdzqsZNOrPl+QXUDZbgyYTvqKtG19BDvzoby27Te9i9nwKIIjU\n"
         + "qPWi+t8Mfk9kGgFGxQyutkke+r2UchQULj5RxVYAOhGwgjn/2z13/nvIYJGgutyB\n"
         + "aHmjghbaMI1pMvUvgLDXf45bHYJ0mPjVq2Dvt1eIJ9zU4w5aplafHVcJnQKBgG8I\n"
         + "3Ae94SItPYenU0cBO7QtiKCfdnFhZUNNtM0YFVbgloI2B58PseRkFJU48BL4EOpG\n"
         + "hgjA+pzOMve58n06rmEYjKvWbiNpUJcTQZ9FVDXc55OUGKNz4LdckxaMgfo1hHp8\n"
         + "TLAU4Y4vv/oroAs1tJNaQk8Co+/7sUfcUVqoush1AoGBALzY4QsRxwVl+/3CWgJJ\n"
         + "HqNjnpG5azh+A1+1RUvkFJEUzad4cQYpMZkMBoTw1R+/4ItvYHiJA2YddmFJWGNc\n"
         + "V5/CZZujzga+jqYFa2aev4bh0wxNiK8kOVFUtoqcDL4I4rNQvsCKw6Oe3qntsR/O\n"
         + "lrUhB9c5GWHBnsv3wRf7J6SU\n"
         + "-----END PRIVATE KEY-----\n\n";

   private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
         + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwaj3D1aaxd5BudF8Wr1l\n"
         + "Ne9JRYqBIsyWvBOE5zZd4gLQYf5suaxleT+SP+lKxerMSnwOXSTADhWsimeCgXQz\n"
         + "yFrzheQdIzEtmUUqN5XyHpJMvyFXsEUqiX7qa+zqOd9MCUjemNxVHL1ZK/43UkD1\n"
         + "XdQs1zmh3nsG+bFXngo7MXzelSF+ElA3glikOs5C9QC1RtwOUWHuilKulzXw8TkP\n"
         + "VbhLzAGspNZAQmUtCecYN3jT2MWsAYurVR3qEgoenvtpYGP9E+swQWb6Skfww8Ih\n"
         + "6GEhGrViuMIb6Et8DsNxCF4I4IyeiwFkOkFAhnkVJF7LYuGtPCp/B7+T0YB45ScG\n"
         + "IQIDAQAB\n"
         + "-----END PUBLIC KEY-----\n\n";

   @DataProvider(name = "keyTypes")
   public Object[][] keyTypesProvider() {
      return new Object[][] {
            { EncryptionUtil.KeyType.PRIVATE, EncryptionUtil.KeyType.PUBLIC },
            { EncryptionUtil.KeyType.PUBLIC, EncryptionUtil.KeyType.PRIVATE },
      };
   }

   @Test(dataProvider = "keyTypes")
   public void encryptTest(final EncryptionUtil.KeyType encryptKeyType, final EncryptionUtil.KeyType decryptKeyType) throws EncryptionException {
      final EncryptionUtil u = new EncryptionUtil();

      Assert.assertEquals(u.decrypt(u.encrypt(ORIGINAL_MESSAGE, encryptKeyType), decryptKeyType), ORIGINAL_MESSAGE);
   }

   @Test
   public void signatureTest() throws EncryptionException {
      final EncryptionUtil u = new EncryptionUtil();

      Assert.assertTrue(u.verify(ORIGINAL_MESSAGE, u.sign(ORIGINAL_MESSAGE)));
      Assert.assertFalse(u.verify(ORIGINAL_MESSAGE, u.sign(ORIGINAL_MESSAGE + "modified")));
      Assert.assertFalse(u.verify(ORIGINAL_MESSAGE + "modified", u.sign(ORIGINAL_MESSAGE)));
   }

   @Test
   public void testStringPrivateKey() throws EncryptionException {
      final EncryptionUtil u1 = new EncryptionUtil(null, PRIVATE_KEY);
      final byte[] secure = u1.encrypt("Hello world", EncryptionUtil.KeyType.PRIVATE);

      final EncryptionUtil u2 = new EncryptionUtil(PUBLIC_KEY, null);
      final String response = u2.decrypt(secure, EncryptionUtil.KeyType.PUBLIC);

      Assert.assertEquals(response, "Hello world");
   }
}