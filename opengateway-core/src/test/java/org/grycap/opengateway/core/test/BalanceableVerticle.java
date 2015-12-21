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

package org.grycap.opengateway.core.test;

import static java.util.Objects.requireNonNull;

import org.grycap.opengateway.core.loadbalancer.BalanceableService;
import org.grycap.opengateway.core.loadbalancer.LoadBalancerClient;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * A verticle that implements the balanceable interface and offers a handler to test the operation of the load balancer.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class BalanceableVerticle extends AbstractVerticle implements BalanceableService {

	public static final String BALANCEABLE_NAME = "TestBalanceableVerticle";
	public static final int BALANCED_PORT = 9081;

	private LoadBalancerClient loadBalancerClient;

	@Override
	public LoadBalancerClient getLoadBalancer() {
		return loadBalancerClient;
	}

	@Override
	public void setLoadBalancer(final LoadBalancerClient loadBalancerClient) {
		this.loadBalancerClient = loadBalancerClient;
	}

	@Override
	public void start() throws Exception {
		requireNonNull(loadBalancerClient, "A valid load balancer client expected");
		final Router router = Router.router(vertx);
		router.get("/").produces("text/plain").handler(this::handleGet);
		vertx.createHttpServer().requestHandler(router::accept).listen(BALANCED_PORT);
	}

	private void handleGet(final RoutingContext routingContext) {
		final HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "text/plain").end(loadBalancerClient.getServiceInstance(BALANCEABLE_NAME));
	}

}