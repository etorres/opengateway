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

package org.grycap.opengateway.example;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import io.vertx.core.AbstractVerticle;

/**
 * A WebSockets server that combines two services to provide a unified view of a sales services where products are listed with the options available for shipping.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 * @see <a href="https://github.com/vert-x3/vertx-examples/">Vert.x examples</a>
 */
public class WebSocketsServer extends AbstractVerticle {

	private final static Logger LOGGER = getLogger(WebSocketsServer.class);

	@Override
	public void start() throws Exception {
		// TODO : implementation pending
		LOGGER.trace("New instance created: [id=" + context.deploymentID() + "].");
	}	


}