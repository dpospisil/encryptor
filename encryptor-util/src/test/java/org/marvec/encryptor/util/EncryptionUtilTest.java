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
}