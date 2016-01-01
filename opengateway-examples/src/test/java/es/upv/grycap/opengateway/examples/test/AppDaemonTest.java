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

package es.upv.grycap.opengateway.examples.test;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static es.upv.grycap.coreutils.fiber.http.Http2Clients.http2Client;
import static es.upv.grycap.coreutils.fiber.http.Http2Clients.isolatedHttp2Client;
import static es.upv.grycap.coreutils.logging.LogManager.getLogManager;
import static es.upv.grycap.opengateway.examples.test.mockserver.ProductCatalogService.getProducts;
import static es.upv.grycap.opengateway.examples.test.mockserver.ShippingService.getShipping;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import es.upv.grycap.coreutils.fiber.http.Http2Client;
import es.upv.grycap.coreutils.test.category.IntegrationTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;
import es.upv.grycap.opengateway.core.OgDaemon;
import es.upv.grycap.opengateway.examples.AppDaemon;
import es.upv.grycap.opengateway.examples.test.mockserver.Product;
import es.upv.grycap.opengateway.examples.test.mockserver.Shipping;
import net.jodah.concurrentunit.Waiter;

/**
 * Tests the {@link AppDaemon}.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
@RunWith(Parameterized.class)
@Category(IntegrationTests.class)
public class AppDaemonTest {

	private static final int port = 8080;
	private static String uri;
	private static OgDaemon daemon;

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	/**
	 * Provides an input dataset to test the different scenarios.
	 * @return Parameters for the different test scenarios.
	 */
	@Parameters(name = "{index}: method={0}, path={1}, code={2}, ids={3}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			/* 0*/ { "GET", "simple-rest/v1/products", 200, of("P001", "P002", "P003", "P004", "P005") }, 
			/* 1*/ { "GET", "simple-rest/v1/shipping", 200, of("S001", "S002", "S003") }, 
			/* 2*/ { "GET", "simple-rest/v1/products", 200, null },
			/* 3*/ { "GET", "simple-rest/v1/shipping", 200, null }
		});
	}

	@Parameter(value = 0) public String method;
	@Parameter(value = 1) public String path;
	@Parameter(value = 2) public int code;
	@Parameter(value = 3) public List<String> ids;

	@BeforeClass
	public static void setUp() throws Exception {
		// install bridges to logging APIs in order to capture Hazelcast and Vert.x messages
		getLogManager().init();
		// start the daemon
		daemon = new AppDaemon();
		daemon.init(new DaemonContext() {			
			@Override
			public DaemonController getController() {
				return null;
			}
			@Override
			public String[] getArguments() {
				return null;
			}
		});
		daemon.start();
		daemon.awaitHealthy(30l, TimeUnit.SECONDS);
		uri = "http://localhost:" + port;
		// test that the server started
		final Waiter waiter = new Waiter();
		http2Client().asyncGet(uri, newArrayList("text/html"), false, new Callback() {			
			@Override
			public void onResponse(final Response response) throws IOException {
				waiter.assertTrue(response.isSuccessful());	
				waiter.assertThat(response.body().contentLength(), greaterThan(0l));
				final String payload = response.body().source().readUtf8();
				waiter.assertThat(payload, allOf(notNullValue(), not(equalTo(""))));
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);
			}
		});
		waiter.await(30l, TimeUnit.SECONDS);
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		daemon.stop();
		daemon.destroy();
	}

	@Test
	public void testResources() throws Exception {
		final Waiter waiter = new Waiter();
		int count = 1;
		if ("GET".equals(method)) {
			if (ids != null && !ids.isEmpty()) {
				count = ids.size();
				for (final String id : ids) {
					testGetObject(id, waiter);
				}				
			} else {
				testGetList(waiter);
			}
		}
		// TODO : add more tests here
		waiter.await(30l, TimeUnit.SECONDS, count);
	}

	private void testGetObject(final String id, final Waiter waiter) throws URISyntaxException, MalformedURLException {
		final Http2Client client = isolatedHttp2Client();
		client.asyncGetJson(String.format("%s/%s/%s", uri, path, id), false, new Callback() {
			@Override
			public void onResponse(final Response response) throws IOException {
				waiter.assertTrue(response.isSuccessful());
				// check response headers
				final Headers headers = response.headers();
				waiter.assertNotNull(headers);
				waiter.assertNotNull(headers.names());
				waiter.assertThat(headers.names().size(), greaterThan(0));
				// check response body
				waiter.assertThat(response.body().contentLength(), greaterThan(0l));
				final String payload = response.body().source().readUtf8();
				waiter.assertThat(payload, allOf(notNullValue(), not(equalTo(""))));
				pw.println(" >> Abbreviated response: " + abbreviate(payload, 32));
				// parse and check
				final Gson gson = new Gson();
				if (path.contains("products")) {
					final Product product = gson.fromJson(payload, Product.class);
					waiter.assertNotNull(product);
					waiter.assertThat(product, allOf(notNullValue(), equalTo(getProducts().get(id))));
					pw.println(" >> Object response: " + product);
				} else if (path.contains("shipping")) {
					final Shipping shipping = gson.fromJson(payload, Shipping.class);
					waiter.assertNotNull(shipping);
					waiter.assertThat(shipping, allOf(notNullValue(), equalTo(getShipping().get(id))));
					pw.println(" >> Object response: " + shipping);
				}
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);
			}
		});		
	}

	private void testGetList(final Waiter waiter) {
		final Http2Client client = isolatedHttp2Client();
		client.asyncGetJson(String.format("%s/%s", uri, path), false, new Callback() {			
			@Override
			public void onResponse(final Response response) throws IOException {
				waiter.assertTrue(response.isSuccessful());
				// check response headers
				final Headers headers = response.headers();
				waiter.assertNotNull(headers);
				waiter.assertNotNull(headers.names());
				waiter.assertThat(headers.names().size(), greaterThan(0));
				// check response body
				waiter.assertThat(response.body().contentLength(), greaterThan(0l));
				final String payload = response.body().source().readUtf8();
				waiter.assertThat(payload, allOf(notNullValue(), not(equalTo(""))));
				pw.println(" >> Abbreviated response: " + abbreviate(payload, 32));
				// parse and check
				final Gson gson = new Gson();
				if (path.contains("products")) {				
					final Type collectionType = new TypeToken<List<Product>>(){}.getType();
					final List<Product> products = gson.fromJson(payload, collectionType);					
					waiter.assertNotNull(products);
					waiter.assertThat(products, allOf(notNullValue(), containsInAnyOrder(getProducts().values().toArray())));
					pw.println(" >> Object response: " + products);
				} else if (path.contains("shipping")) {
					final Type collectionType = new TypeToken<List<Shipping>>(){}.getType();
					final List<Shipping> shipping = gson.fromJson(payload, collectionType);					
					waiter.assertNotNull(shipping);
					waiter.assertThat(shipping, allOf(notNullValue(), containsInAnyOrder(getShipping().values().toArray())));
					pw.println(" >> Object response: " + shipping);
				}
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);
			}
		});
	}

}