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

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Implements a load balancer for cases where only one instance of the service is deployed.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class SingleNodeLoadBalancer implements LoadBalancerClient {

	/**
	 * The name that should be used to query this load balancer.
	 */
	public final String SINGLE_NODE_REGISTRY = "SingleNodeServiceRegistry";

	private final ListMultimap<String, String> registry = ArrayListMultimap.create();

	@Override @Nullable
	public List<String> query(final String serviceRegistry, final String appId) {
		final String appId2 = requireNonNull(trimToNull(appId));
		return SINGLE_NODE_REGISTRY.equals(serviceRegistry) ? registry.get(appId2) : null;		
	}

	@Override @Nullable
	public List<String> query(final String appId) {
		return query(SINGLE_NODE_REGISTRY, appId);
	}

	@Override @Nullable
	public String getServiceInstance(final String serviceRegistry, final String appId) {
		final String appId2 = requireNonNull(trimToNull(appId));
		return SINGLE_NODE_REGISTRY.equals(serviceRegistry) ? ofNullable(registry.get(appId2)).orElse(emptyList()).stream().findAny().orElse(null) : null;
	}

	@Override @Nullable
	public String getServiceInstance(final String appId) {
		return getServiceInstance(SINGLE_NODE_REGISTRY, appId);
	}

}