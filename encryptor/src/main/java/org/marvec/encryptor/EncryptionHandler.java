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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marvec.encryptor.api.KeyRequest;
import org.marvec.encryptor.util.EncryptionException;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class EncryptionHandler implements Closeable {

   private static final Logger log = LogManager.getLogger(EncryptionHandler.class);

   // extensive usage, cache the value
   private static final boolean debugEnabled = log.isDebugEnabled();

   private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Integer.getInteger(EncryptorConst.THREAD_COUNT, 10), new DaemonThreadFactory(EncryptorConst.THREAD_NAME_PREFIX));

   EncryptionHandler() {
   }

   @Override
   public void close() throws IOException {
      executor.shutdownNow();
   }

   private <T extends KeyRequest> void processRequest(final RoutingContext routingContext, final Class<T> expectedRequestType) {
      try {
         final T message = Json.decodeValue(routingContext.getBodyAsString(), expectedRequestType);

         if (debugEnabled) {
            log.debug("Processing message: {}", message.toString());
         }

         routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                       .end(Json.encodePrettily(message.process()));
      } catch (EncryptionException e) {
         log.error("Unable to process request: ", e);
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to process request.").end();
      } catch (DecodeException de) {
         log.error("Unable to decode request: ", de);
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to process request.").end();
      }
   }

   void submitTask(final RoutingContext context, final Class expectedRequestType) {
      executor.submit(() -> processRequest(context, expectedRequestType));
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
