package org.epos.api;

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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.epos.api.core.DetailsItemGenerationJPA;
import org.epos.api.core.MonitoringGeneration;
import org.epos.api.core.SearchGenerationJPA;
import org.epos.api.utility.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

abstract class ApiController<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
	protected final HttpServletRequest request;

	protected ApiController(HttpServletRequest request) {
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	protected ResponseEntity<T> standardRequest(String service, Map<String, Object> requestParams) {
		String cacheId = StringUtils.join(requestParams);
		
		String responseCode = "OK";
		
		String response = null;
		
		switch(service) {
		case "SEARCH":
			response = Utils.gson.toJson(SearchGenerationJPA.generate(requestParams));
			break;
		case "DETAILS":
			response = Utils.gson.toJson(DetailsItemGenerationJPA.generate(requestParams));
			break;
		case "MONITORING":
			response = Utils.gson.toJson(MonitoringGeneration.generate());
			break;
		default:
			break;
		}
		
		if(response!=null) {
			LOGGER.debug("Payload text received => "+response);

			if(Utils.gson.fromJson(response, JsonElement.class).isJsonObject()) {
				if(!Utils.gson.fromJson(response, JsonElement.class).getAsJsonObject().entrySet().isEmpty()) {
					LOGGER.debug("Payload Array received => "+response);;
					return hasContent(response)? (ResponseEntity<T>) ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response) : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
				}
			}
			else if(Utils.gson.fromJson(response, JsonElement.class).isJsonArray()) {
				if(Utils.gson.fromJson(response, JsonElement.class).getAsJsonArray().size()>0) {
					LOGGER.debug("Payload Array received => "+response);
					return hasContent(response)? (ResponseEntity<T>) ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response) : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
				}
			}
			else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}	
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}


	private static boolean hasContent(String payload) throws JsonSyntaxException {
		LOGGER.debug("Has Content Payload => "+payload);
		JsonElement jsonElement = Utils.gson.fromJson(payload, JsonElement.class);
		LOGGER.debug("Has Content Json Element => "+jsonElement.toString());
		return ((jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size()<1) 
				|| (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().entrySet().isEmpty()))? 
						false : true;
	}
}