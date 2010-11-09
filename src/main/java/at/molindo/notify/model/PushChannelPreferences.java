/**
 * Copyright 2010 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.molindo.notify.model;

public class PushChannelPreferences extends ChannelPreferences {

	public enum Frequency {
		INSTANT, DAILY, WEEKLY, MONTHLY;
	}

	private Frequency _frequency = Frequency.INSTANT;

	public PushChannelPreferences() {
	}

	public PushChannelPreferences(Params defaults) {
		super(defaults);
	}

	public void setFrequency(Frequency frequency) {
		_frequency = frequency;
	}

	public Frequency getFrequency() {
		return _frequency;
	}

	@Override
	public PushChannelPreferences clone() {
		return (PushChannelPreferences) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getFrequency() == null) ? 0 : getFrequency().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof PushChannelPreferences)) {
			return false;
		}
		PushChannelPreferences other = (PushChannelPreferences) obj;
		if (getFrequency() != other.getFrequency()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PushChannelPreferences [frequency=" + getFrequency() + ", params()=" + getParams() + "]";
	}

}
