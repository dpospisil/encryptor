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

import io.vertx.core.json.Json;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public abstract class KeyRequest<T> {

   protected EncryptionUtil encryptionUtil;

   private String privateKey;

   private String publicKey;

   public String getPrivateKey() {
      return privateKey;
   }

   public void setPrivateKey(final String privateKey) {
      this.privateKey = privateKey;
   }

   public String getPublicKey() {
      return publicKey;
   }

   public void setPublicKey(final String publicKey) {
      this.publicKey = publicKey;
   }

   public T process() throws EncryptionException {
      if (privateKey == null && publicKey == null) {
         encryptionUtil = new EncryptionUtil();
      } else {
         encryptionUtil = new EncryptionUtil(publicKey, privateKey);
      }

      return doProcess();
   }

   public abstract T doProcess() throws EncryptionException;

   protected void checkAttribute(final Object t) throws EncryptionException {
      if (t == null || "".equals(t)) {
         throw new EncryptionException("Incomplete request.");
      }
   }

   public String toString() {
      return Json.encodePrettily(this);
   }
}
