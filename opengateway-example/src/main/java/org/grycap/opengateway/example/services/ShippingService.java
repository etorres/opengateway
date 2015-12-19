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

package org.grycap.opengateway.example.services;

import java.util.HashMap;
import java.util.Map;

import org.grycap.opengateway.example.models.Shipping;

/**
 * Provides information about shipping options.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class ShippingService {

	private static Map<String, Shipping> dataset = new HashMap<>();
	static {
		dataset.put("Post Service", new Shipping("Post Service", 7, 0.50, "Unsafe and unreliable", new char[]{ 'E', 'S' }));
		dataset.put("Package Delivery Company", new Shipping("Package Delivery Company", 3, 23.0, "Fast but expensive", new char[]{ 'E', 'S' }));
		dataset.put("International Shipping Agency", new Shipping("International Shipping Agency", 14, 51.0, "The only choice", new char[]{ 'U', 'S' }));
	}

	// TODO

}