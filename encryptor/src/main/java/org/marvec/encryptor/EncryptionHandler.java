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

import org.marvec.encryptor.api.EncryptedMessage;
import org.marvec.encryptor.api.Signature;
import org.marvec.encryptor.api.SimpleMessage;
import org.marvec.encryptor.api.Verify;
import org.marvec.encryptor.util.EncryptionException;
import org.marvec.encryptor.util.EncryptionUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class EncryptionHandler implements Closeable {

   private final EncryptionUtil encryptionUtil;

   private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Integer.getInteger(EncryptorConst.THREAD_COUNT, 10), new DaemonThreadFactory(EncryptorConst.THREAD_NAME_PREFIX));

   EncryptionHandler() throws EncryptionException {
      encryptionUtil = new EncryptionUtil();
   }

   @Override
   public void close() throws IOException {
      executor.shutdownNow();
   }

   private void encryptMessage(final RoutingContext routingContext, final EncryptionUtil.KeyType keyType) {
      final SimpleMessage message = Json.decodeValue(routingContext.getBodyAsString(), SimpleMessage.class);

      try {
         routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                       .end(Json.encodePrettily(new EncryptedMessage(encryptionUtil.encrypt(message.getPayload(), keyType))));
      } catch (EncryptionException e) {
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to encrypt message: " + e.getMessage()).end();
      }
   }

   void encryptMessageWithPrivate(final RoutingContext routingContext) {
      encryptMessage(routingContext, EncryptionUtil.KeyType.PRIVATE);
   }

   void encryptMessageWithPublic(final RoutingContext routingContext) {
      encryptMessage(routingContext, EncryptionUtil.KeyType.PUBLIC);
   }

   private void decryptMessage(final RoutingContext routingContext, final EncryptionUtil.KeyType keyType) {
      final EncryptedMessage message = Json.decodeValue(routingContext.getBodyAsString(), EncryptedMessage.class);

      try {
         routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                       .end(Json.encodePrettily(new SimpleMessage(encryptionUtil.decrypt(message.getEncryptedPayload(), keyType))));
      } catch (EncryptionException e) {
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to encrypt message: " + e.getMessage()).end();
      }
   }

   void decryptMessageWithPrivate(final RoutingContext routingContext) {
      decryptMessage(routingContext, EncryptionUtil.KeyType.PRIVATE);
   }

   void decryptMessageWithPublic(final RoutingContext routingContext) {
      decryptMessage(routingContext, EncryptionUtil.KeyType.PUBLIC);
   }

   void signMessage(final RoutingContext routingContext) {
      final SimpleMessage message = Json.decodeValue(routingContext.getBodyAsString(), SimpleMessage.class);

      try {
         routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                       .end(Json.encodePrettily(new Signature(encryptionUtil.sign(message.getPayload()))));
      } catch (EncryptionException e) {
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to sign message: " + e.getMessage()).end();
      }
   }

   void verifyMessage(final RoutingContext routingContext) {
      final Verify message = Json.decodeValue(routingContext.getBodyAsString(), Verify.class);

      try {
         routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                       .end(Json.encodePrettily(encryptionUtil.verify(message.getMessage(), message.getSignature())));
      } catch (EncryptionException e) {
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to sign message: " + e.getMessage()).end();
      }
   }

   void createTask(final RoutingContext context, final Consumer<RoutingContext> action) {
      executor.submit(() -> action.accept(context));
   }

   private static class DaemonThreadFactory implements ThreadFactory {

      private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);
      private final ThreadGroup group;
      private final String threadNamePrefix;

      public DaemonThreadFactory(final String threadNamePrefix) {
         this.threadNamePrefix = threadNamePrefix;
         final SecurityManager securityManager = System.getSecurityManager();
         group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
      }

      @Override
      public Thread newThread(final Runnable runnable) {
         final Thread thread = new Thread(group, runnable, threadNamePrefix + THREAD_NUMBER.getAndIncrement(), 0);
         thread.setDaemon(true);

         return thread;
      }
   }
}
