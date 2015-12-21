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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.grycap.opengateway.core.VertxService.verticleName;
import static org.grycap.opengateway.core.test.BalanceableVerticle.BALANCEABLE_NAME;
import static org.grycap.opengateway.core.test.BalanceableVerticle.BALANCED_PORT;

import java.util.Set;

import org.grycap.coreutils.test.category.FunctionalTests;
import org.grycap.opengateway.core.loadbalancer.LoadBalancerClient;
import org.grycap.opengateway.core.loadbalancer.SingleNodeLoadBalancer;
import org.grycap.opengateway.core.vertx.OgVerticleFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests {@link OgVerticleFactory}.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
@RunWith(VertxUnitRunner.class)
@Category(FunctionalTests.class)
public class VerticleFactoryTest {

	private Vertx vertx;
	private LoadBalancerClient loadBalancerClient;

	@Before
	public void before(final TestContext context) {
		vertx = Vertx.vertx();
		loadBalancerClient = new SingleNodeLoadBalancer().addService(BALANCEABLE_NAME, String.format("http://localhost:%d", BALANCED_PORT));
		// create and register the new verticle factory		
		final VerticleFactory factory = new OgVerticleFactory(loadBalancerClient);
		vertx.registerVerticleFactory(factory);
		final Set<VerticleFactory> factories = vertx.verticleFactories();
		context.assertNotNull(factories, "Verticles factories exist");
		context.assertTrue(factories.contains(factory), "Created verticle factory is available");
	}

	@After
	public void after(final TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testBalanceableVerticle(final TestContext context) {
		final Async async = context.async();		
		vertx.deployVerticle(verticleName(BalanceableVerticle.class), resp -> {
			context.assertTrue(resp.succeeded(), "Deployment of balanceable opengateway verticle succeeded");
			final String deploymentId = trimToNull(resp.result());
			context.assertFalse(isBlank(deploymentId), "A deployment id was created and returned to the caller");			
			// check the client behavior
			final HttpClient client = vertx.createHttpClient();
			client.getNow(BALANCED_PORT, "localhost", "/", resp2 -> {
				resp2.bodyHandler(body -> {
					context.assertEquals(String.format("http://localhost:%d", BALANCED_PORT), body.toString());
					client.close();
					async.complete();
				});
			});
		});
	}

	@Test
	public void testCommonVerticle(final TestContext context) {
		final Async async = context.async();		
		vertx.deployVerticle(verticleName(CommonVerticle.class), resp -> {
			context.assertTrue(resp.succeeded(), "Deployment of common Java verticle succeeded");
			async.complete();
		});
	}

}