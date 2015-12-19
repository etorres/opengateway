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

package org.grycap.opengateway.core.test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.grycap.coreutils.logging.LogManager.getLogManager;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.grycap.coreutils.test.category.FunctionalTests;
import org.grycap.coreutils.test.rules.TestPrinter;
import org.grycap.coreutils.test.rules.TestWatcher2;
import org.grycap.opengateway.core.OgDaemon;
import org.grycap.opengateway.core.VertxService;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.util.concurrent.ServiceManager;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;

/**
 * Tests boot process.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
@RunWith(Parameterized.class)
@Category(FunctionalTests.class)
public class BootTest {

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@BeforeClass
	public static void setup() {
		// install bridges to logging APIs in order to capture Hazelcast and Vert.x messages
		getLogManager().init();
	}

	/**
	 * Provides different input datasets.
	 * @return Parameters for the different test scenarios.
	 */
	@Parameters(name = "{index}: clustered={0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ true },
			{ false }
		});
	}

	@Parameter(value = 0) public boolean clustered;

	@Test
	public void testBoot() throws Exception {
		// create new instance
		final TestDaemon daemon = new TestDaemon(clustered);
		assertThat("Test daemon was created", daemon, notNullValue());
		daemon.init(new DaemonContext() {
			@Override
			public DaemonController getController() {
				return null;
			}
			@Override
			public String[] getArguments() {
				return null;
			}
		});
		daemon.start();

		// await until the services are started or fail with timeout
		daemon.awaitHealthy(30l, TimeUnit.SECONDS);

		// await until the services are stopped or fail with timeout
		daemon.awaitStopped(30l, TimeUnit.SECONDS);
	}

	public static class TestDaemon extends OgDaemon {

		private final boolean clustered;

		public TestDaemon(final boolean clustered) {
			super(TestDaemon.class);
			this.clustered = clustered;
		}

		@Override
		public void init(final DaemonContext daemonContext) throws Exception {			
			// load default configuration properties
			loadConfigFile(null);
			// create service options from configuration
			final VertxOptions vertxOptions = createVertxOptions(clustered);		
			final DeploymentOptions deploymentOptions = createDeploymentOptions();
			// configure and start the service manager
			serviceManager = new ServiceManager(newHashSet(new VertxService(newArrayList(), vertxOptions, deploymentOptions)));		
			super.init(daemonContext);
		}

	}

}