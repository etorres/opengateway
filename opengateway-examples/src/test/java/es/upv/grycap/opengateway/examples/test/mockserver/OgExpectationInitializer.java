/*
 * Open Gateway - Usage Examples.
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

package es.upv.grycap.opengateway.examples.test.mockserver;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.model.Header;

import com.google.gson.Gson;

/**
 * Sets Mock Server expectations for tests that requires a valid HTTP/Proxy server to interact with.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class OgExpectationInitializer implements ExpectationInitializer {

	public static final String MOCK_SERVER_BASE_URL = "http://localhost:9080";

	@Override
	public void initializeExpectations(final MockServerClient client) {
		final Gson gson = new Gson();
		// add products
		ProductCatalogService.getProducts().entrySet().stream().forEach(e -> {
			client.when(request().withMethod("GET").withPath(String.format("/products/%s", e.getKey())))
			.respond(response().withStatusCode(200)
					.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
							new Header("Cache-Control", "public, max-age=86400"))
					.withBody(gson.toJson(e.getValue())));
		});
		client.when(request().withMethod("GET").withPath("/products"))
		.respond(response().withStatusCode(200)
				.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
						new Header("Cache-Control", "public, max-age=86400"))
				.withBody(gson.toJson(ProductCatalogService.getProducts().values())));
		// add shipping options
		ShippingService.getShipping().entrySet().stream().forEach(e -> {
			client.when(request().withMethod("GET").withPath(String.format("/shipping/%s", e.getKey())))
			.respond(response().withStatusCode(200)
					.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
							new Header("Cache-Control", "public, max-age=86400"))
					.withBody(gson.toJson(e.getValue())));
		});
		client.when(request().withMethod("GET").withPath("/shipping"))
		.respond(response().withStatusCode(200)
				.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
						new Header("Cache-Control", "public, max-age=86400"))
				.withBody(gson.toJson(ShippingService.getShipping().values())));
		// TODO : complete this method with the needed POST, PUT and DELETE mocks to test the services
	}

}