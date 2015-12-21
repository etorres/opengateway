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

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;

/**
 * An HTTP response. Instances of this class are not immutable: the response body is a one-shot value that may be consumed only once. 
 * All other properties are immutable.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class HttpResponse {

	private final Response response;

	public HttpResponse(final Response response) {
		this.response = response;		
	}

	/**
	 * Gets the body of the response entity (if any) as a UTF-8 encoded string.
	 * @return The response entity (if any) as a UTF-8 encoded string.
	 */
	public String readUtf8() {
		try {
			return response.body().source().readUtf8();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to parse body", e);
		}
	}

	/**
	 * Gets the body of the response entity (if any) as an array of bytes.
	 * @return The response entity (if any) as an array of bytes.
	 */
	public byte[] readByteArray() {
		try {
			return response.body().source().readByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read body", e);
		}
	}

	/**
	 * Gets the body of the response entity (if any), transformed to the appropriate type.
	 * @return The body of the response entity (if any), transformed to the appropriate type.
	 * @throws IOException - if the body cannot be processed
	 */
	public <T> T fromString(final Function<String, T> converter) {
		requireNonNull(converter, "A valid converted expected");
		try {
			return response.body() != null ? converter.apply(response.body().source().readUtf8()) : null;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to convert body", e);
		}
	}

	/**
	 * Returns an immutable list of the header values for name.
	 * @param name - header name
	 * @return An immutable list of the header values for name.
	 */
	public List<String> header(final String name) {
		return ofNullable(response.headers()).orElse(Headers.of()).values(name);
	}

}