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

package es.upv.grycap.opengateway.examples.test.mockserver;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Information about a specific shipping service.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.0.1
 */
public class Shipping {

	private String code;
	private String provider;
	private int days;
	private double price;
	private Optional<String> observations;
	private char[] countryCode;

	public Shipping() { }

	public Shipping(final String code, final String provider, final int days, final double price, final String observations, final char[] countryCode) {
		this.code = code;
		this.provider = provider;
		this.days = days;
		this.price = price;
		this.observations = ofNullable(observations);
		this.countryCode = countryCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(final String provider) {
		this.provider = provider;
	}

	public int getDays() {
		return days;
	}

	public void setDays(final int days) {
		this.days = days;
	}

	public double getPrice() {
		return price;
	}
	public void setPrice(final double price) {
		this.price = price;
	}

	public String getObservations() {
		return observations.orElse(null);
	}

	public void setObservations(final String observations) {
		this.observations = ofNullable(observations);
	}

	public char[] getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(final char[] countryCode) {
		this.countryCode = countryCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Shipping)) {
			return false;
		}
		final Shipping other = Shipping.class.cast(obj);
		return Objects.equals(code, other.code)
				&& Objects.equals(provider, other.provider)
				&& Objects.equals(days, other.days)
				&& Objects.equals(price, other.price)
				&& Objects.equals(observations.orElse(null), other.observations.orElse(null))
				&& Arrays.equals(countryCode, other.countryCode);		
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, provider, days, price, observations, countryCode);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("code", code)
				.add("provider", provider)
				.add("days", days)
				.add("price", price)
				.add("observations", observations)
				.add("countryCode", countryCode)
				.toString();
	}

}