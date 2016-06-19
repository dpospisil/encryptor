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
package org.marvec.encryptor.api;

import org.marvec.encryptor.util.EncryptionException;
import org.marvec.encryptor.util.EncryptionUtil;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class EncryptionRequest extends KeyRequest<EncryptedMessage> {

   private String message;

   public String getMessage() {
      return message;
   }

   public void setMessage(final String message) {
      this.message = message;
   }

   @Override
   public EncryptedMessage doProcess() throws EncryptionException {
      checkAttribute(message);

      return new EncryptedMessage(encryptionUtil.encrypt(message, (getPrivateKey() != null || getPublicKey() == null) ? EncryptionUtil.KeyType.PRIVATE : EncryptionUtil.KeyType.PUBLIC));
   }
}
