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

package org.grycap.opengateway.core.loadbalancer;

import java.util.List;

/**
 * Provides a load balancer that allows discovering service instances from a client.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 * @see <a hre="http://microservices.io/patterns/client-side-discovery.html">Pattern: Client-side service discovery</a>
 */
public interface LoadBalancerClient {

	/**
	 * Queries a service catalog for a list of instances associated to the specified application.
	 * @param serviceRegistry - the service registry to query
	 * @param appId - the application identifier
	 * @return A list of instances associated to the specified application or <tt>null</tt> if no record matches the query.
	 */
	List<String> query(String serviceRegistry, String appId);
	
	/**
	 * Queries the default service catalog for a list of instances associated to the specified application.
	 * @param appId - the application identifier
	 * @return A list of instances associated to the specified application or <tt>null</tt> if no record matches the query.
	 */
	List<String> query(String appId);

	/**
	 * Gets a service instance from the service catalog, allowing the service to apply a load balancing strategy.
	 * @param serviceRegistry - the service registry to query
	 * @param appId - the application identifier
	 * @return A service instance.
	 */
	String getServiceInstance(String serviceRegistry, String appId);

	/**
	 * Gets a service instance from the default service catalog, allowing the service to apply a load balancing strategy.
	 * @param appId - the application identifier
	 * @return A service instance.
	 */
	String getServiceInstance(String appId);
	
}