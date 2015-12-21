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

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Information about a specific product.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class Product {

	private String code;
	private String name;
	private Optional<String> description;
	private double price;
	private char[] countryCode;

	public Product() { }

	public Product(final String code, final String name, final String description, final double price, final char[] countryCode) {
		this.code = code;
		this.name = name;
		this.description = ofNullable(description);
		this.price = price;
		this.countryCode = countryCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Nullable
	public String getDescription() {
		return description.orElse(null);
	}

	public void setDescription(final @Nullable String description) {
		this.description = ofNullable(description);
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(final double price) {
		this.price = price;
	}

	public char[] getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(final char[] countryCode) {
		this.countryCode = countryCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Product)) {
			return false;
		}
		final Product other = Product.class.cast(obj);
		return Objects.equals(code, other.code)
				&& Objects.equals(name, other.name)
				&& Objects.equals(description.orElse(null), other.description.orElse(null))
				&& Objects.equals(price, other.price)
				&& Arrays.equals(countryCode, other.countryCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, name, description, price, countryCode);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("code", code)
				.add("name", name)
				.add("description", description)
				.add("price", price)
				.add("countryCode", countryCode)
				.toString();
	}

}