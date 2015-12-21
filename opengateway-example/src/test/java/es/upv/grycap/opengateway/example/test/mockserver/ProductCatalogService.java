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

package es.upv.grycap.opengateway.example.test.mockserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides information about the products available for sale.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public final class ProductCatalogService {
	
	private static Map<String, Product> dataset = new HashMap<>();
	static {
		dataset.put("P001", new Product("P001", "Smartwatch", "Fancy and useless clock", 180.0, new char[]{ 'E', 'S' }));
		dataset.put("P002", new Product("P002", "Smartphone", "Expensive and enslaving gadget", 420.0, new char[]{ 'E', 'S' }));
		dataset.put("P003", new Product("P003", "Tablet", "Substitute for TV", 210.0, new char[]{ 'U', 'S' }));
		dataset.put("P004", new Product("P004", "Laptop", "A developer's best friend!", 890.0, new char[]{ 'E', 'S' }));		
		dataset.put("P005", new Product("P005", "Gaming PC", "Technology that's worth owning", 2100.0, new char[]{ 'E', 'S' }));
	}

	public static Map<String, Product> getProducts() {
		return dataset;
	}
	
}