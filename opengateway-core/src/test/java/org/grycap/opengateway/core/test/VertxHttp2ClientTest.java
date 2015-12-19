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

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.grycap.coreutils.test.category.FunctionalTests;
import org.grycap.coreutils.test.rules.TestPrinter;
import org.grycap.coreutils.test.rules.TestWatcher2;
import org.grycap.opengateway.core.http.VertxHttp2Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;

/**
 * Tests the {@link VertxHttp2Client}.
 * @author Erik Torres <etserrano@gmail.com>
 * @since @since 0.0.1
 */
@RunWith(Parameterized.class)
@UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
@Category(FunctionalTests.class)
public class VertxHttp2ClientTest {

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	/**
	 * Provides an input dataset with different data formats (JSON, XML) and different access methods (URL fragment, 
	 * query parameter). Some tests will surpass the concurrency level.
	 * @return Parameters for the different test scenarios.
	 */
	@Parameters(name = "{index}: method={0}, path={1}, objectId={2}, contentType={3}, parseResp={4}, nocache={5}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			/* 0*/ { "GET",    "/test/json/1",   "1", of("application/json"), false, true },
			/* 1*/ { "GET",    "/test/json/2",   "2", of("application/json"), true,  true },
			/* 2*/ { "GET",    "/test/xml/1",    "1", of("application/xml"),  false, true },
			/* 3*/ { "GET",    "/test/xml/2",    "2", of("application/xml"),  false, true },
			/* 4*/ { "GET",    "/test/json?q=1", "1", of("application/json"), true,  true },
			/* 5*/ { "GET",    "/test/json?q=2", "2", of("application/json"), false, true },
			/* 6*/ { "GET",    "/test/xml?q=1",  "1", of("application/xml"),  false, true },
			/* 7*/ { "GET",    "/test/xml?q=2",  "2", of("application/xml"),  false, true },
			/* 8*/ { "GET",    "/test/json/1",   "1", null,                   true,  true },
			/* 9*/ { "GET",    "/test/xml/1",    "1", null,                   false, true },
			/*10*/ { "GET",    "/test/json/1",   "1", null,                   false, false },
			/*11*/ { "GET",    "/test/xml/1",    "1", null,                   false, false },
			/*12*/ { "POST",   "/test/json",     "X", of("application/json"), false, true  },
			/*13*/ { "POST",   "/test/xml",      "Y", of("application/xml"),  false, true  },
			/*14*/ { "PUT",    "/test/json/1",   "1", of("application/json"), false, true  },
			/*15*/ { "PUT",    "/test/xml/2",    "2", of("application/xml"),  false, true  },
			/*16*/ { "DELETE", "/test/json/2",   "2", of("application/json"), false, true  },
			/*17*/ { "DELETE", "/test/xml/1",    "1", of("application/xml"),  false, true  }
		});
	}

	@Parameter(value = 0) public String method;
	@Parameter(value = 1) public String path;
	@Parameter(value = 2) public String objectId;
	@Parameter(value = 3) public List<String> contentType;
	@Parameter(value = 4) public boolean parseResp;
	@Parameter(value = 5) public boolean nocache;

	private Vertx vertx;

	@Before
	public void before() {
		vertx = Vertx.vertx();
	}

	@Test
	public void test(final TestContext context) throws Exception {
		final String respContentType = ofNullable(contentType).orElse(newArrayList("application/json")).get(0);
		String body;
		if (path.contains("xml")) {
			body = String.format("<object>%s</object>", objectId);
		} else if (path.contains("json")) {
			body = String.format("{ \"object\" : \"%s\" }", objectId);
		} else body = "plain text";
		final HttpServer server = vertx.createHttpServer().requestHandler(req -> {
			context.assertEquals(9080, req.localAddress().port());
			req.response().putHeader("content-type", respContentType).end(body);
		});
		server.listen(9080, "localhost", context.asyncAssertSuccess(s -> {
			final VertxHttp2Client client = new VertxHttp2Client(vertx);
			final Async async = context.async();
			// submit request
			switch (method) {
			case "POST":
				submitPost(client, context, body, async);
				break;
			case "PUT":
				submitPut(client, context, body, async);
				break;
			case "DELETE":
				submitDelete(client, context, body, async);
				break;
			case "GET":
			default:
				submitGet(client, context, body, async, parseResp);				
				break;
			}						
		}));
	}

	private void submitGet(final VertxHttp2Client client, final TestContext context, final String body, final Async async, final boolean parseResp) {
		client.asyncGet("http://localhost:9080" + path, contentType, nocache, resp -> {
			context.assertTrue(resp.succeeded(), "Request succeeded");
			if (parseResp && path.contains("json")) {
				final JsonObject jsonObj = resp.result().fromString(r -> new JsonObject(r));
				context.assertNotNull(jsonObj, "JSON response is not empty");
				context.assertTrue(jsonObj.containsKey("object"), "JSON response contains the expected fields");
				final String objectId2 = jsonObj.getString("object");
				context.assertNotNull(objectId2, "Object id is not empty");
				context.assertEquals(objectId, objectId2, "Object id coincides with expected");
			} else {
				context.assertEquals(body, resp.result().string(), "Body coincides with expected");
			}
			async.complete();
		});
	}

	private void submitPost(final VertxHttp2Client client, final TestContext context, final String body, final Async async) {
		final String requestContentType = ofNullable(contentType).orElse(newArrayList("application/json")).get(0);
		Supplier<String> supplier = null;
		if (path.contains("xml")) {
			supplier = () -> "<object>Y</object>";
		} else if (path.contains("json")) {
			supplier = () -> "{ \"object\" : \"X\" }";
		} else supplier = () -> "plain text";
		client.asyncPost("http://localhost:9080" + path, requestContentType, supplier, resp -> {
			context.assertTrue(resp.succeeded(), "Request succeeded");
			context.assertEquals(body, resp.result().string(), "Body coincides with expected");
			async.complete();
		});
	}

	private void submitPut(final VertxHttp2Client client, final TestContext context, final String body, final Async async) {
		final String requestContentType = ofNullable(contentType).orElse(newArrayList("application/json")).get(0);
		Supplier<String> supplier = null;
		if (path.contains("xml")) {
			supplier = () -> "<object>Y</object>";
		} else if (path.contains("json")) {
			supplier = () -> "{ \"object\" : \"X\" }";
		} else supplier = () -> "plain text";
		client.asyncPut("http://localhost:9080" + path, requestContentType, supplier, resp -> {
			context.assertTrue(resp.succeeded(), "Request succeeded");
			async.complete();
		});
	}

	private void submitDelete(final VertxHttp2Client client, final TestContext context, final String body, final Async async) {
		client.asyncDelete("http://localhost:9080" + path, resp -> {
			context.assertTrue(resp.succeeded(), "Request succeeded");
			async.complete();
		});
	}

	@After
	public void after(final TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

}