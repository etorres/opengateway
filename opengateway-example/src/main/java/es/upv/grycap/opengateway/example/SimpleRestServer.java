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

package es.upv.grycap.opengateway.example;

import static es.upv.grycap.opengateway.core.http.RestServiceConfig.getRestServiceConfig;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.ImmutableMap;

import es.upv.grycap.opengateway.core.http.BaseRestService;
import es.upv.grycap.opengateway.core.http.RestServiceConfig.ApiConfig;

/**
 * A REST server that combines two services to provide a unified view of a sales services where products are listed with the options available for shipping.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 * @see <a href="https://github.com/vert-x3/vertx-examples/">Vert.x examples</a>
 */
public class SimpleRestServer extends BaseRestService {

	/**
	 * Convenient constructor to add the service configuration to this server. In this example, two different resources are combined in the
	 * same API: product and shipping. The endpoint where the resources are served will be discovered from the service registry via the load
	 * balancer. This class only needs to know about the identifier of the services in the catalog (e.g <tt>opengateway-example.product.v1</tt>)
	 * and the local path where the API will be served in this application (e.g <tt>/simple-rest/v1/product</tt>).
	 */
	public SimpleRestServer() {
		logger = getLogger(SimpleRestServer.class);
		serviceConfig = getRestServiceConfig(new ImmutableMap.Builder<String, ApiConfig>()
				.put("opengateway-example.product.v1", new ApiConfig("opengateway-example.product.v1", "/simple-rest/v1/products"))
				.put("opengateway-example.shipping.v1", new ApiConfig("opengateway-example.shipping.v1", "/simple-rest/v1/shipping"))
				.build(), null);		
	}

}