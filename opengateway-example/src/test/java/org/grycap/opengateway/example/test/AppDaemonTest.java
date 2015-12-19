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

package org.grycap.opengateway.example.test;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.grycap.coreutils.fiber.http.Http2Client.getHttp2Client;
import static org.grycap.coreutils.logging.LogManager.getLogManager;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.http.client.utils.URIBuilder;
import org.grycap.coreutils.test.category.FunctionalTests;
import org.grycap.coreutils.test.rules.TestPrinter;
import org.grycap.coreutils.test.rules.TestWatcher2;
import org.grycap.opengateway.core.OgDaemon;
import org.grycap.opengateway.example.AppDaemon;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.jodah.concurrentunit.Waiter;

/**
 * Tests the {@link AppDaemon}.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
@Category(FunctionalTests.class)
public class AppDaemonTest {

	private int port = 8080;
	private String uri;
	private OgDaemon daemon;

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@BeforeClass
	public static void setup() {
		// install bridges to logging APIs in order to capture Hazelcast and Vert.x messages
		getLogManager().init();
	}

	@Before
	public void setUp() throws Exception {
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
		uri = "http://localhost:" + port + "/";
	}

	@After
	public void cleanUp() throws Exception {
		daemon.stop();
		daemon.destroy();
	}

	@Test
	public void test() throws Exception {
		final Waiter waiter = new Waiter();

		// Thread.sleep(300000l); // TODO
		
		// test index page
		getHtml("", waiter);

		// test get object
		getJson("simple-rest/v1/product/21", waiter);

		// TODO

		waiter.await(30l, TimeUnit.SECONDS, 2);
	}

	private void getHtml(final String path, final Waiter waiter) throws URISyntaxException, IOException {
		get(path, "text/html", waiter);
	}

	private void getJson(final String path, final Waiter waiter) throws URISyntaxException, IOException {
		get(path, "application/json", waiter);		
	}

	private void get(final String path, final String mimeType, final Waiter waiter) throws URISyntaxException, IOException {
		final URIBuilder uriBuilder = new URIBuilder(uri + path);
		getHttp2Client().asyncGet(uriBuilder.build().toURL().toString(), newArrayList(mimeType), false, new Callback() {			
			@Override
			public void onResponse(final Response response) throws IOException {
				
				// TODO
				System.err.println("\n\n >> RESPONSE: " + response + "\n");
				// TODO
				
				waiter.assertTrue(response.isSuccessful());				
				// assert response headers				
				final Headers headers = response.headers();
				waiter.assertNotNull(headers);
				waiter.assertNotNull(headers.names());
				waiter.assertThat(headers.names().size(), greaterThan(0));
				// assert response body
				waiter.assertThat(response.body().contentLength(), greaterThan(0l));
				final String payload = response.body().source().readUtf8();
				waiter.assertThat(payload, allOf(notNullValue(), not(equalTo(""))));
				pw.println(" >> Response: " + abbreviate(payload, 64));
				waiter.resume();			
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);
			}
		});
	}

}