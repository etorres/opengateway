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

package org.grycap.opengateway.core.vertx;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.lang.reflect.Method;

import org.grycap.opengateway.core.loadbalancer.LoadBalancerClient;

import io.vertx.core.Verticle;
import io.vertx.core.impl.JavaVerticleFactory;
import io.vertx.core.spi.VerticleFactory;

/**
 * Decorates the verticle Java factory with an additional functionality to inject the load balancer in the services that require it.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 * @see <a href="https://github.com/eclipse/vert.x/blob/master/src/main/java/io/vertx/core/impl/JavaVerticleFactory.java">JavaVerticleFactory</a>
 */
public class OgVerticleFactory implements VerticleFactory {

	public static final String OG_VERTICLE_FACTORY_PREFIX = "opengateway-java";

	private final JavaVerticleFactory javaVerticleFactory = new JavaVerticleFactory();
	private final LoadBalancerClient loadBalancerClient;

	/**
	 * Convenient constructor that setup the load balancer.
	 * @param loadBalancerClient - load balancer client
	 */
	public OgVerticleFactory(final LoadBalancerClient loadBalancerClient) {
		this.loadBalancerClient = loadBalancerClient;
	}

	@Override
	public String prefix() {
		return OG_VERTICLE_FACTORY_PREFIX;
	}

	@Override
	public Verticle createVerticle(final String verticleName, final ClassLoader classLoader) throws Exception {
		final String checkedVerticleName = requireNonNull(trimToNull(verticleName), "A non-empty name expected");
		final Verticle verticle = javaVerticleFactory.createVerticle(checkedVerticleName.replaceFirst(String.format("^%s:", prefix()), 
				String.format("%s:", javaVerticleFactory.prefix())), classLoader);		
		try {
			final Method method = verticle.getClass().getMethod("setLoadBalancer", new Class<?>[]{ LoadBalancerClient.class });
			if (method != null) method.invoke(verticle, loadBalancerClient);
		} catch (NoSuchMethodException ignore) { }
		return verticle;
	}

}