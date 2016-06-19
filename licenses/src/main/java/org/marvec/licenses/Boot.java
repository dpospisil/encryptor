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
package org.marvec.licenses;

import org.marvec.licenses.util.EncryptionException;
import org.marvec.licenses.util.EncryptionUtil;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Provides {@link org.marvec.licenses.util.EncryptionUtil} as an HTTP service.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class Boot {

   private final EncryptionUtil encryptionUtil;

   private Boot() throws EncryptionException {
      encryptionUtil = new EncryptionUtil();
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

   private void encryptMessageWithPrivate(final RoutingContext routingContext) {
      encryptMessage(routingContext, EncryptionUtil.KeyType.PRIVATE);
   }

   private void encryptMessageWithPublic(final RoutingContext routingContext) {
      encryptMessage(routingContext, EncryptionUtil.KeyType.PUBLIC);
   }

   private void signMessage(final RoutingContext routingContext) {
      final SimpleMessage message = Json.decodeValue(routingContext.getBodyAsString(), SimpleMessage.class);

      try {
         routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                       .end(Json.encodePrettily(new Signature(encryptionUtil.sign(message.getPayload()))));
      } catch (EncryptionException e) {
         routingContext.response().setStatusCode(500).setStatusMessage("Unable to sign message: " + e.getMessage()).end();
      }
   }

   private void startServer() {
      System.out.println("Starting encryption service.");

      Vertx vertx = Vertx.vertx();
      HttpServer server = vertx.createHttpServer();

      Router router = Router.router(vertx);

      router.route("/*").handler(BodyHandler.create());
      router.post("/encrypt/private").blockingHandler(this::encryptMessageWithPrivate);
      router.post("/encrypt/public").blockingHandler(this::encryptMessageWithPublic);
      router.post("/sign").blockingHandler(this::signMessage);

      server.requestHandler(router::accept).listen(8080);

   }

   public static void main(String[] args) throws EncryptionException {
      final Boot boot = new Boot();

      boot.startServer();
   }
}
