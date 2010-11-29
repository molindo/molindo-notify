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

public class PushChannelPreferences extends ChannelPreferences implements IPushChannelPreferences {

	public enum Frequency {
		INSTANT, DAILY, WEEKLY, MONTHLY;
	}

	private Long _id;
	private Frequency _frequency = Frequency.INSTANT;

	public Long getId() {
		return _id;
	}

	public void setId(Long id) {
		_id = id;
	}

	/* (non-Javadoc)
	 * @see at.molindo.notify.model.IPushChannelPreferences#setFrequency(at.molindo.notify.model.PushChannelPreferences.Frequency)
	 */
	@Override
	public void setFrequency(Frequency frequency) {
		if (frequency == null) {
			throw new NullPointerException("frequency");
		}
		_frequency = frequency;
	}

	/* (non-Javadoc)
	 * @see at.molindo.notify.model.IPushChannelPreferences#getFrequency()
	 */
	@Override
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
		result = prime * result + (getFrequency() == null ? 0 : getFrequency().hashCode());
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
		IPushChannelPreferences other = (IPushChannelPreferences) obj;
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
