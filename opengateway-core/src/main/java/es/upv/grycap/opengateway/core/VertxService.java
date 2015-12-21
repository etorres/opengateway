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

package es.upv.grycap.opengateway.core;

import static com.google.common.base.Preconditions.checkState;
import static es.upv.grycap.opengateway.core.vertx.OgVerticleFactory.OG_VERTICLE_FACTORY_PREFIX;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.util.concurrent.AbstractIdleService;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;

import es.upv.grycap.opengateway.core.loadbalancer.LoadBalancerClient;
import es.upv.grycap.opengateway.core.vertx.OgVerticleFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Vert.x managed service.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class VertxService extends AbstractIdleService {

	private final static Logger LOGGER = getLogger(VertxService.class);

	private final VertxOptions vertxOptions;
	private final DeploymentOptions deploymentOptions;
	private final LoadBalancerClient loadBalancerClient;

	private Vertx vertx;

	private final List<Class<?>> verticles;

	/**
	 * Convenient constructor that creates a managed service where the verticles (microservices in the Vert.x jargon) that will take part of a 
	 * particular deployment will be configured, started and managed. Optionally, the started service can be configured to join a cluster.
	 * @param verticles - services that will take part of the deployment
	 * @param vertxOptions - general configuration properties
	 * @param deploymentOptions - configuration properties affecting the deployment
	 */
	public VertxService(final List<Class<?>> verticles, final @Nullable VertxOptions vertxOptions, final @Nullable DeploymentOptions deploymentOptions,
			final LoadBalancerClient loadBalancerClient) {
		this.verticles = requireNonNull(verticles, "Verticles list expected.");
		this.vertxOptions = ofNullable(vertxOptions).orElse(new VertxOptions());
		this.deploymentOptions = ofNullable(deploymentOptions).orElse(new DeploymentOptions());
		this.loadBalancerClient = requireNonNull(loadBalancerClient, "A valid load balancer expected");		
	}

	@Override
	protected void startUp() throws Exception {
		final AtomicBoolean success = new AtomicBoolean(true);
		final Consumer<Vertx> runner = vertx -> verticles.stream().forEach(ver -> vertx.deployVerticle(verticleName(ver), deploymentOptions, res -> {			
			if (res == null || !res.succeeded()) {
				success.set(false);				
				if (res != null) LOGGER.error(String.format("Failed to deploy verticle [type=%s].", ver.getSimpleName()), res.cause());				
				else LOGGER.error(String.format("Failed to deploy verticle [type=%s]. Unknown cause.", ver.getSimpleName()));
			} else LOGGER.info(String.format("New verticle deployed: [type=%s, id=%s].", ver.getSimpleName(), res.result()));
		}));
		final CompletableFuture<Void> future = new CompletableFuture<>();
		if (vertxOptions.isClustered()) {
			// configure Hazelcast        	
			final Config hazelcastConfig = new Config();
			hazelcastConfig.setInstanceName("opengateway-cluster");
			final NetworkConfig hazelcastNetwork = hazelcastConfig.getNetworkConfig();
			hazelcastNetwork.setPublicAddress(deploymentOptions.getConfig().getString("cluster.public-address"));
			final JoinConfig hazelcastJoin = hazelcastNetwork.getJoin();
			hazelcastJoin.getMulticastConfig().setEnabled(false);
			hazelcastJoin.getTcpIpConfig().addMember(deploymentOptions.getConfig().getString("cluster.network")).setEnabled(true);
			hazelcastNetwork.getInterfaces().addInterface(deploymentOptions.getConfig().getString("cluster.network")).setEnabled(true);
			final GroupConfig hazelcastGroup = hazelcastConfig.getGroupConfig();
			hazelcastGroup.setName(deploymentOptions.getConfig().getString("cluster.name")).setPassword(deploymentOptions.getConfig().getString("cluster.secret"));        	
			final ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);
			vertxOptions.setClusterManager(clusterManager);
			Vertx.clusteredVertx(vertxOptions, res -> {
				if (res.succeeded()) {
					vertx = res.result();
					vertx.registerVerticleFactory(new OgVerticleFactory(loadBalancerClient));
					// TODO
					runner.accept(vertx);
				} else LOGGER.error("Failed to start Vert.x system.", res.cause());
				future.complete(null);
			});
		} else {
			vertx = Vertx.vertx(vertxOptions);
			vertx.registerVerticleFactory(new OgVerticleFactory(loadBalancerClient));
			// TODO
			runner.accept(vertx);
			future.complete(null);
		}
		checkState(success.get(), "Exiting since not all verticles were successfully deployed.");		
		try {
			future.get(deploymentOptions.getConfig().getLong("daemon-service.startup-timeout"), TimeUnit.SECONDS);
			// additional startup operations could be executed here
		} catch (InterruptedException | TimeoutException e) {
			throw new IllegalStateException("Given up to start service daemon due to interruption or timeout.");
		} catch (ExecutionException e) {
			throw (e.getCause() instanceof Exception ? (Exception)e.getCause() : e);
		}
	}

	@Override
	protected void shutDown() throws Exception {
		vertx.close(res -> {
			if (res != null) {
				if (res.succeeded()) LOGGER.info("Shutdown succeeded.");
				else LOGGER.info("Exited with error.", res.cause());
			} else LOGGER.info("Exited with error. Unknown cause.");
		});
	}

	public static String verticleName(final Class<?> clazz) {
		return String.format("%s:%s", OG_VERTICLE_FACTORY_PREFIX, clazz.getCanonicalName());
	}

}