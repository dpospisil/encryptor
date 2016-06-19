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
import org.marvec.encryptor.api.DecryptionRequest;
import org.marvec.encryptor.api.EncryptionRequest;
import org.marvec.encryptor.api.SignRequest;
import org.marvec.encryptor.api.VerifyRequest;
import org.marvec.encryptor.util.EncryptionException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Provides {@link org.marvec.encryptor.util.EncryptionUtil} as an HTTP service.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class Boot {

   private static final Logger log = LogManager.getLogger(Boot.class);

   private final EncryptionHandler encryptionHandler;

   private HttpServer server;

   public Boot() throws EncryptionException {
      this.encryptionHandler = new EncryptionHandler();
   }

   public void startServer() {
      log.info("Starting encryption service...");

      Vertx vertx = Vertx.vertx();
      server = vertx.createHttpServer();

      Router router = Router.router(vertx);

      router.route("/*").handler(BodyHandler.create());
      router.post("/encrypt").handler((context) -> encryptionHandler.submitTask(context, EncryptionRequest.class));
      router.post("/decrypt").handler((context) -> encryptionHandler.submitTask(context, DecryptionRequest.class));
      router.post("/sign").handler((context) -> encryptionHandler.submitTask(context, SignRequest.class));
      router.get("/verify").handler((context) -> encryptionHandler.submitTask(context, VerifyRequest.class));

      log.info("Service started, hit <Ctrl>-C to exit...");

      server.requestHandler(router::accept).listen(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080));
   }

   public void stop() {
      server.close();
   }

   public static void main(String[] args) throws EncryptionException {
      final Boot boot = new Boot();

      boot.startServer();
   }
}
