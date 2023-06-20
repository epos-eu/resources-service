/*******************************************************************************
 * Copyright 2021 EPOS ERIC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.epos.api.utility;

import java.util.Arrays;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class SystemResponse {

	private String consumerTag;
	private Envelope envelope;
	private AMQP.BasicProperties properties;
	private byte[] body;
	
	
	public SystemResponse(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
		super();
		this.consumerTag = consumerTag;
		this.envelope = envelope;
		this.properties = properties;
		this.body = body;
	}


	/**
	 * @return the consumerTag
	 */
	public String getConsumerTag() {
		return consumerTag;
	}


	/**
	 * @param consumerTag the consumerTag to set
	 */
	public void setConsumerTag(String consumerTag) {
		this.consumerTag = consumerTag;
	}


	/**
	 * @return the envelope
	 */
	public Envelope getEnvelope() {
		return envelope;
	}


	/**
	 * @param envelope the envelope to set
	 */
	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}


	/**
	 * @return the properties
	 */
	public AMQP.BasicProperties getProperties() {
		return properties;
	}


	/**
	 * @param properties the properties to set
	 */
	public void setProperties(AMQP.BasicProperties properties) {
		this.properties = properties;
	}


	/**
	 * @return the body
	 */
	public byte[] getBody() {
		return body;
	}


	/**
	 * @param body the body to set
	 */
	public void setBody(byte[] body) {
		this.body = body;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SystemResponse [consumerTag=" + consumerTag + ", envelope=" + envelope + ", properties=" + properties
				+ ", body=" + Arrays.toString(body) + "]";
	}

	
	
}
