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

   private final EncryptionHandler encryptionHandler;

   private Boot() throws EncryptionException {
      this.encryptionHandler = new EncryptionHandler();
   }

   private void startServer() {
      System.out.println("Starting encryption service...");

      Vertx vertx = Vertx.vertx();
      HttpServer server = vertx.createHttpServer();

      Router router = Router.router(vertx);

      router.route("/*").handler(BodyHandler.create());
      router.post("/encrypt/private").handler((context) -> encryptionHandler.createTask(context, encryptionHandler::encryptMessageWithPrivate));
      router.post("/encrypt/public").handler((context) -> encryptionHandler.createTask(context, encryptionHandler::encryptMessageWithPublic));
      router.post("/decrypt/private").handler((context) -> encryptionHandler.createTask(context, encryptionHandler::decryptMessageWithPrivate));
      router.post("/decrypt/public").handler((context) -> encryptionHandler.createTask(context, encryptionHandler::decryptMessageWithPublic));
      router.post("/sign").handler((context) -> encryptionHandler.createTask(context, encryptionHandler::signMessage));
      router.get("/verify").handler((context) -> encryptionHandler.createTask(context, encryptionHandler::verifyMessage));

      System.out.println("Service started, hit <Ctrl>-C to exit...");

      server.requestHandler(router::accept).listen(Integer.getInteger(EncryptorConst.PORT_NUMBER, 8080));
   }

   public static void main(String[] args) throws EncryptionException {
      final Boot boot = new Boot();

      boot.startServer();
   }
}
