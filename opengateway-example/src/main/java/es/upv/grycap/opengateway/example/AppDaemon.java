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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

import com.google.common.util.concurrent.ServiceManager;

import es.upv.grycap.opengateway.core.OgDaemon;
import es.upv.grycap.opengateway.core.VertxService;
import es.upv.grycap.opengateway.core.loadbalancer.LoadBalancerClient;
import es.upv.grycap.opengateway.core.loadbalancer.SingleNodeLoadBalancer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;

/**
 * Example daemon.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class AppDaemon extends OgDaemon {

	/**
	 * Class constructor.
	 */
	public AppDaemon() {
		super(AppDaemon.class);
	}

	/**
	 * Main entry point to the application.
	 * @param args - arguments passed to the application
	 * @throws Exception - if the application fails to start the required services
	 */
	public static void main(final String[] args) throws Exception {
		final AppDaemon daemon = new AppDaemon();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					daemon.stop();
				} catch (Exception e) {
					System.err.println(new StringBuffer("Failed to stop application: ").append(e.getMessage()).toString());
				}
				try {
					daemon.destroy();
				} catch (Exception e) {
					System.err.println(new StringBuffer("Failed to stop application: ").append(e.getMessage()).toString());
				}
			}
		});
		daemon.init(new DaemonContext() {
			@Override
			public DaemonController getController() {
				return null;
			}
			@Override
			public String[] getArguments() {
				return args;
			}
		});
		daemon.start();
	}

	@Override
	public void init(final DaemonContext daemonContext) throws Exception {
		// parse application arguments
		CommandLine cmd = null;
		try {
			cmd = parseParameters(daemonContext.getArguments(), new Options());
		} catch (Exception e) {
			logger.error("Parsing options failed.", e);
			System.exit(1);
		}
		// load configuration properties
		loadConfigOptions(cmd);
		// create load balancer (a real implementation of the load balancer should be used in production)
		final LoadBalancerClient loadBalancerClient = new SingleNodeLoadBalancer()
				.addService("opengateway-example.product.v1", "http://localhost:9080/products")
				.addService("opengateway-example.shipping.v1", "http://localhost:9080/shipping");
		// create service options from configuration
		final VertxOptions vertxOptions = createVertxOptions(true);		
		final DeploymentOptions deploymentOptions = createDeploymentOptions();
		// configure and start the service manager
		serviceManager = new ServiceManager(newHashSet(new VertxService(newArrayList(SimpleRestServer.class, SecureRestServer.class, WebSocketsServer.class), 
				vertxOptions, deploymentOptions, loadBalancerClient)));
		super.init(daemonContext);
	}

}