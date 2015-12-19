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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Configuration for REST services.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class RestServiceConfig {

	private Map<String, ApiConfig> services = newHashMap();
	private Optional<String> frontpage = empty();	

	public RestServiceConfig() { }

	public RestServiceConfig(final Map<String, ApiConfig> services, final @Nullable String frontpage) {		
		setServices(services);
		this.frontpage = ofNullable(frontpage);
	}

	public static RestServiceConfig getRestServiceConfig(final Map<String, ApiConfig> services, final @Nullable String frontpage) {
		return new RestServiceConfig(services, frontpage);
	}

	public Map<String, ApiConfig> getServices() {
		return services;
	}

	public void setServices(final Map<String, ApiConfig> services) {
		this.services = requireNonNull(services, "A valid set of services expected");
		checkArgument(!this.services.isEmpty(), "At least one service expected");
	}

	public Optional<String> getFrontpage() {
		return frontpage;
	}

	public void setFrontpage(final String frontpage) {
		this.frontpage = ofNullable(frontpage);
	}

	/**
	 * Configuration for individual services.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.0.1
	 */
	public static class ApiConfig {

		private String appId; 
		private String path;

		public ApiConfig() { }

		public ApiConfig(final String appId, final String path) {
			this.appId = appId;
			this.path = path;
		}

		/**
		 * Gets the application identifier that can be used to discover new instances of this service in a service catalog.
		 * @return The application identifier.
		 */
		public String getAppId() {
			return appId;
		}

		/**
		 * Sets the application identifier that can be used to discover new instances of this service in a service catalog.
		 * @param appId - the unique identifier that was used to register the instances of this application in a service catalog
		 */
		public void setAppId(final String appId) {
			this.appId = appId;
		}

		/**
		 * Gets the path relative to the server's root path where this service is exposed.
		 * @return The path relative to the server's root path where this service is exposed.
		 */
		public String getPath() {
			return path;
		}

		/**
		 * Sets the path relative to the server's root path where this service is exposed.
		 * @param path - a URL fragment that should be added to the server's root path to 
		 */
		public void setPath(final String path) {
			this.path = path;
		}

	}

}