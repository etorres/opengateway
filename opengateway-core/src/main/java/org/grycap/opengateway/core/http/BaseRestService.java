/*
 * Open Gateway - Core Components.
 * Copyright 2015-2016 GRyCAP (Universitat Politecnica de Valencia)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * 
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package org.grycap.opengateway.core.http;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Objects;

import org.grycap.opengateway.core.loadbalancer.LoadBalancerClient;
import org.grycap.opengateway.core.loadbalancer.SingleNodeLoadBalancer;
import org.slf4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Implements a generic REST API that allows clients to list items, retrieve details for a particular item and to add new items. The API can 
 * be extended to meet the specific needs of an application.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public abstract class BaseRestService extends AbstractVerticle {

	protected Logger logger;
	protected RestServiceConfig serviceConfig;
	protected LoadBalancerClient loadBalancerClient;

	private final static long MAX_BODY_SIZE_MIB = 8; // 8 MiB

	@Override
	public void start() throws Exception {
		// setup load balancer
		switch (config().getString("load-balancer.strategy")) {
		case "single-node":			
		default:
			loadBalancerClient = new SingleNodeLoadBalancer();
			break;
		}
		final Router router = Router.router(vertx);
		// set body limit
		final long maxBodySize = context.config().getLong("http-server.max-body-size", MAX_BODY_SIZE_MIB) * 1024l * 1024l;		
		router.route().handler(BodyHandler.create().setBodyLimit(maxBodySize));
		// enable CORS
		router.route().handler(CorsHandler.create("*")
				.allowedMethod(HttpMethod.GET)
				.allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.PUT)
				.allowedMethod(HttpMethod.DELETE)
				.allowedMethod(HttpMethod.OPTIONS)
				.allowedHeader("Content-Type")
				.allowedHeader("Authorization"));
		// configure index page
		router.route(serviceConfig.getFrontpage().orElse("/")).handler(StaticHandler.create());
		// serve resources
		serviceConfig.getServices().values().stream().forEach(s -> {
			final String path = requireNonNull(s.getPath(), "A valid path required");
			router.get(String.format("%s/:id", path)).produces("application/json").handler(this::handleGet);
			router.get(path).produces("application/json").handler(this::handleList);
			router.post(path).handler(this::handleCreate);
			router.put(String.format("%s/:id", path)).consumes("application/json").handler(this::handleModify);
			router.delete(String.format("%s/:id", path)).handler(this::handleDelete);
		});
		// start HTTP server
		final int port = context.config().getInteger("http.port", 8080);		
		vertx.createHttpServer().requestHandler(router::accept).listen(port);
		logger.trace("New instance created: [id=" + context.deploymentID() + "].");
	}

	private void handleGet(final RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		final HttpServerResponse response = routingContext.response();
		if (id == null) {
			sendError(400, response);
		} else {
			final String service = loadBalancerClient.getServiceInstance(serviceConfig.getAppId());
			if (isBlank(service)) {
				sendError(503, response);
			} else {
				new VertxHttp2Client(vertx).asyncGet(String.format("%s/%s", service, id), false, resp -> {
					if (!resp.succeeded()) {
						sendError(504, response);
					} else {
						final JsonObject jsonObj = resp.result().fromString(r -> new JsonObject(r));
						if (jsonObj == null) {
							sendError(404, response);
						} else {							
							response.putHeader("content-type", "application/json").end(jsonObj.encode());
						}
					}
				});
			}
		}
	}

	private void handleList(final RoutingContext routingContext) {
		final HttpServerResponse response = routingContext.response();		
		final String service = loadBalancerClient.getServiceInstance(serviceConfig.getAppId());
		if (isBlank(service)) {
			sendError(503, response);
		} else {
			new VertxHttp2Client(vertx).asyncGet(service, false, resp -> {
				if (!resp.succeeded()) {
					sendError(504, response);
				} else {
					final JsonObject jsonObj = resp.result().fromString(r -> new JsonObject(r));
					if (jsonObj == null) {
						sendError(404, response);
					} else {
						response.putHeader("content-type", "application/json").end(jsonObj.encode());
					}
				}
			});
		}
	}

	private void handleCreate(final RoutingContext routingContext) {
		final Buffer buffer = routingContext.getBody();		
		final HttpServerResponse response = routingContext.response();
		if (buffer == null) {
			sendError(400, response);
		} else {
			final String service = loadBalancerClient.getServiceInstance(serviceConfig.getAppId());
			if (isBlank(service)) {
				sendError(503, response);
			} else {
				new VertxHttp2Client(vertx).asyncPostBytes(service, "application/json", () -> buffer.getBytes(), resp -> {
					if (!resp.succeeded()) {
						sendError(504, response);
					} else {
						ofNullable(resp.result().header("Location")).orElse(emptyList()).stream().filter(Objects::nonNull)
						.forEach(location -> response.putHeader("Location", location));
						response.setStatusCode(201).end();
					}					
				});			
			}
		}		
	}

	private void handleModify(final RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		final Buffer buffer = routingContext.getBody();
		final HttpServerResponse response = routingContext.response();
		if (id == null || buffer == null) {
			sendError(400, response);
		} else {
			final String service = loadBalancerClient.getServiceInstance(serviceConfig.getAppId());
			if (isBlank(service)) {
				sendError(503, response);
			} else {
				new VertxHttp2Client(vertx).asyncPutBytes(String.format("%s/%s", service, id), "application/json", () -> buffer.getBytes(), resp -> {
					if (!resp.succeeded()) {
						sendError(504, response);
					} else {
						response.setStatusCode(204).end();
					}
				});				
			}
		}
	}

	private void handleDelete(final RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		final HttpServerResponse response = routingContext.response();
		if (id == null) {
			sendError(400, response);
		} else {
			final String service = loadBalancerClient.getServiceInstance(serviceConfig.getAppId());
			if (isBlank(service)) {
				sendError(503, response);
			} else {
				new VertxHttp2Client(vertx).asyncDelete(String.format("%s/%s", service, id), resp -> {
					if (!resp.succeeded()) {
						sendError(504, response);
					} else {
						response.setStatusCode(204).end();
					}
				});				
			}
		}
	}

	private void sendError(final int statusCode, final HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}

}