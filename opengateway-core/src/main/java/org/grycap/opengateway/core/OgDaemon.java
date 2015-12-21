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

package org.grycap.opengateway.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static com.typesafe.config.ConfigRenderOptions.concise;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.grycap.coreutils.logging.LogManager.getLogManager;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.grycap.coreutils.common.Configurer;
import org.slf4j.Logger;

import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;
import com.typesafe.config.Config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * Tools for starting applications as stand-alone processes.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public abstract class OgDaemon implements Daemon {

	public static final String[] ARGS_CONFIG_OPT  = { "c", "configuration" };
	public static final String ARGS_DIR_PROP = "directory";

	protected final Logger logger;

	protected Thread daemonThread;
	protected boolean stopped = false;

	protected Config config;
	protected ServiceManager serviceManager;

	/**
	 * Convenient constructor that initializes a logger for the daemon.
	 * @param clazz - the class extending this daemon
	 */
	public OgDaemon(final Class<?> clazz) {
		getLogManager().init();
		logger = getLogger(clazz);
	}

	@Override
	public void init(final DaemonContext daemonContext) throws Exception {
		// start daemon thread
		daemonThread = new Thread() {	
			@Override
			public synchronized void start() {
				OgDaemon.this.stopped = false;
				super.start();
			}
			@Override
			public void run() {             
				if (!stopped) {					
					super.run();					
				}
			}			
		};
	}

	@Override
	public void start() throws Exception {
		daemonThread.start();
		serviceManager.addListener(new Listener() {
			@Override
			public void healthy() {
				final double startupTime = serviceManager.startupTimes().entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
				logger.info(String.format("Services started in: %d seconds.", (long)startupTime/1000l));
			}
		});
		serviceManager.startAsync();
	}

	@Override
	public void stop() throws Exception {
		stopped = true;
		try {
			serviceManager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
			logger.info("Service manager was stopped.");
		} catch (TimeoutException timeout) {
			logger.info("Stopping timed out.");
		}
		try {
			daemonThread.join(1000l);
		} catch (InterruptedException e) {
			logger.warn("Stopping error.", e);
			throw e;
		}		
	}

	@Override
	public void destroy() {
		daemonThread = null;
		serviceManager = null;
		try {
			logger.info("Closing log manager: all messages will be lost beyond this point.");
			getLogManager().reset();
		} catch (Exception ignore) { }
	}

	/**
	 * Description copied from the method {@link ServiceManager#awaitHealthy(long, TimeUnit)}: Waits for this instance to become 
	 * {@link ServiceManager#isHealthy() healthy} for no more than the given time.
	 * @param timeout - the maximum time to wait
	 * @param unit - the time unit of the timeout argument
	 * @throws TimeoutException if not all of the services have finished starting within the deadline
	 */
	public void awaitHealthy(final long timeout, final TimeUnit unit) throws TimeoutException {
		serviceManager.awaitHealthy(timeout, unit);
	}

	/**
	 * Description copied from the method {@link ServiceManager#awaitStopped(long, TimeUnit)}: Waits for the all the services to 
	 * reach a terminal state for no more than the given time.
	 * @param timeout - the maximum time to wait
	 * @param unit - the time unit of the timeout argument
	 * @throws TimeoutException if not all of the services have stopped within the deadline
	 */
	public void awaitStopped(final long timeout, final TimeUnit unit) throws TimeoutException {
		serviceManager.stopAsync().awaitStopped(timeout, unit);
	}

	/**
	 * Parses command-line arguments against the application's options.
	 * @param args - arguments passed to the application
	 * @param options - configuration options
	 * @return A list of arguments parsed against the application's options.
	 * @throws ParseException - if the arguments cannot be parsed
	 */
	protected CommandLine parseParameters(final String[] args, final Options options) throws ParseException {
		final Option configOption = Option.builder(ARGS_CONFIG_OPT[0])
				.longOpt(ARGS_CONFIG_OPT[1])
				.hasArg()
				.argName(ARGS_DIR_PROP)
				.desc("load configuration from the specified directory")
				.build();
		options.addOption(configOption);
		final CommandLineParser parser = new DefaultParser();
		return parser.parse(options, args);
	}

	/**
	 * Loads configuration properties, discovering the options from the application's command line arguments.
	 * @param cmd - a list of arguments parsed against the application's options
	 */
	protected void loadConfigOptions(final CommandLine cmd) {
		String confname = null;
		if (cmd.hasOption(ARGS_CONFIG_OPT[0])) {
			try {
				confname = cmd.getOptionValue(ARGS_CONFIG_OPT[0]);
				checkArgument(isNotBlank(confname), String.format("Parameter %s is expected.", ARGS_DIR_PROP));				
			} catch (Exception e) {
				logger.error("Configuration load failed.", e);
				System.exit(1);
			}
		}
		loadConfigFile(confname);
	}

	/**
	 * Loads the configuration properties from a configuration file.
	 * @param confname - optional configuration filename
	 */
	protected void loadConfigFile(final @Nullable String confname) {
		// load and merge application configuration with default properties
		config = new Configurer().loadConfig(confname, "opengateway");
		if (logger.isTraceEnabled()) {
			logger.trace(config.root().render());
		} else {
			logger.info("Configuration: " + config.getObject("opengateway").render(concise()));
		}
	}

	/**
	 * Reads configuration properties and creates the options for the Vert.x server.
	 * @param clustered - set to <tt>true</tt> to create a clustered server
	 * @return Configuration options for the Vert.x server.
	 */
	protected VertxOptions createVertxOptions(final boolean clustered) {
		requireNonNull(config);
		final VertxOptions vertxOptions = new VertxOptions().setClustered(clustered);
		if (clustered) {
			vertxOptions.setClusterHost(config.getString("opengateway.cluster.network"));
		}
		return vertxOptions;
	}

	/**
	 * Reads configuration properties and creates the options for the deployment of the services.
	 * @return Configuration options for service deployment.
	 */
	protected DeploymentOptions createDeploymentOptions() {
		requireNonNull(config);
		// create service options from configuration
		final Map<String, Object> verticleConfig = newHashMap();
		verticleConfig.put("daemon-service.startup-timeout", config.getLong("opengateway.daemon-service.startup-timeout"));
		verticleConfig.put("http-server.port", config.getInt("opengateway.http-server.port"));		
		verticleConfig.put("cluster.name", config.getString("opengateway.cluster.name"));
		verticleConfig.put("cluster.secret", config.getString("opengateway.cluster.secret"));
		verticleConfig.put("cluster.network", config.getString("opengateway.cluster.network"));
		verticleConfig.put("cluster.public-address", config.getString("opengateway.cluster.public-address"));
		// create deployment options
		return new DeploymentOptions()
				.setInstances(config.getInt("opengateway.http-server.instances"))
				.setConfig(new JsonObject(verticleConfig));
	}

}