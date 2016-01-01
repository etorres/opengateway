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

package es.upv.grycap.opengateway.core.http;

import static com.google.common.collect.Lists.newArrayList;
import static es.upv.grycap.coreutils.fiber.http.Http2Clients.http2Client;
import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * A Vert.x client used to interact with {@link es.upv.grycap.coreutils.fiber.Http2Client Http2Client}.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class VertxHttp2Client {

	private final Vertx vertx;

	public VertxHttp2Client(final Vertx vertx) {
		this.vertx = vertx;
	}

	/**
	 * Retrieve information from a server via a HTTP GET request.
	 * @param url - URL target of this request
	 * @param nocache - don't accept an invalidated cached response, and don't store the server's response in any cache
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncGet(final String url, final boolean nocache, final Handler<AsyncResult<HttpResponse>> resultHandler) {		
		asyncGet(url, null, nocache, resultHandler);
	}

	/**
	 * Retrieve information from a server via a HTTP GET request.
	 * @param url - URL target of this request
	 * @param nocache - don't accept an invalidated cached response, and don't store the server's response in any cache
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncGetJson(final String url, final boolean nocache, final Handler<AsyncResult<HttpResponse>> resultHandler) {		
		asyncGet(url, newArrayList("application/json"), nocache, resultHandler);
	}

	/**
	 * Retrieve information from a server via a HTTP GET request.
	 * @param url - URL target of this request
	 * @param acceptableMediaTypes - Content-Types that are acceptable for this request
	 * @param nocache - don't accept an invalidated cached response, and don't store the server's response in any cache
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncGet(final String url, final @Nullable List<String> acceptableMediaTypes, final boolean nocache, final Handler<AsyncResult<HttpResponse>> resultHandler) {
		http2Client().asyncGet(url, acceptableMediaTypes, nocache, wrapCallback(resultHandler));
	}

	/**
	 * Posts data to a server via a HTTP POST request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncPost(final String url, final String mediaType, final Supplier<String> supplier, final Handler<AsyncResult<HttpResponse>> resultHandler) {
		http2Client().asyncPost(url, mediaType, supplier, wrapCallback(resultHandler));
	}

	/**
	 * Posts the content of a buffer of bytes to a server via a HTTP POST request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncPostBytes(final String url, final String mediaType, final Supplier<byte[]> supplier, final Handler<AsyncResult<HttpResponse>> resultHandler) {
		http2Client().asyncPostBytes(url, mediaType, supplier, wrapCallback(resultHandler));
	}

	/**
	 * Puts data to a server via a HTTP PUT request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncPut(final String url, final String mediaType, final Supplier<String> supplier, final Handler<AsyncResult<HttpResponse>> resultHandler) {
		http2Client().asyncPut(url, mediaType, supplier, wrapCallback(resultHandler));
	}

	/**
	 * Puts the content of a buffer of bytes to a server via a HTTP PUT request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncPutBytes(final String url, final String mediaType, final Supplier<byte[]> supplier, final Handler<AsyncResult<HttpResponse>> resultHandler) {
		http2Client().asyncPutBytes(url, mediaType, supplier, wrapCallback(resultHandler));
	}

	/**
	 * Delete HTTP method.
	 * @param supplier - supplies the content of this request
	 * @param resultHandler - is called back when the response is readable
	 */
	public void asyncDelete(final String url, final Handler<AsyncResult<HttpResponse>> resultHandler) {
		http2Client().asyncDelete(url, wrapCallback(resultHandler));
	}

	private Callback wrapCallback(final Handler<AsyncResult<HttpResponse>> resultHandler) {
		final Context context = vertx.getOrCreateContext();		
		return new Callback() {
			@Override
			public void onResponse(final Response response) throws IOException {
				context.runOnContext(v -> {
					if (!response.isSuccessful()) resultHandler.handle(failedFuture(new IOException(String.format("Unexpected code: %s", response))));
					else resultHandler.handle(succeededFuture(new HttpResponse(response)));
				});
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				context.runOnContext(v -> {
					resultHandler.handle(failedFuture(new IllegalStateException(String.format("Failed request: %s", request), throwable)));
				});
			}			
		};
	}

}