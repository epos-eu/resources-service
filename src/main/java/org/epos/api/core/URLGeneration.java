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

package org.epos.api.core;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(URLGeneration.class);

	private static ArrayList<String> blackList = new ArrayList<>();
	static {
		blackList.add("()");
		blackList.add("(,,,)");
		blackList.add("[]");
		blackList.add("[,,,]");
	}

	private URLGeneration() {}
	/**
	 * Generate URL from a template "http://url.org/{id}{?param}" and a parametermap
	 * 
	 * 
	 * @param template
	 * @param map
	 * @return
	 */
	public static String generateURLFromTemplateAndMap(String template, Map<String, Object> map)
	{

		boolean isSegment = false;
		boolean isThereQuestionMark = false;
		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(template.length());

		for(int i = 0; i < template.length(); i++) {
			char c = template.charAt(i);
			if(c == '?') isThereQuestionMark = true;
			switch(c) {
			case '{': {
				isSegment = true;
				sb.append('{'); break;
			}
			case '}': {
				sb.append('}'); 
				isSegment = false;
				String calcTemplate = calculateTemplate(sb.toString(), map);
				LOGGER.info("infos: "+sb.toString()+" "+map.toString());
				LOGGER.info("calcTemplate: "+calcTemplate);
				if(calcTemplate.isEmpty() && isThereQuestionMark) template = template.replace(sb.toString(), "?");
				if(calcTemplate!=null) {
					template = template.replace(sb.toString(), calcTemplate);
					LOGGER.info("template updated: "+template);
					if(template.contains("{")) {
						template = generateURLFromTemplateAndMap(template, map);
					}
				}
				if(calcTemplate.isEmpty()){
					template = template.replace(sb.toString(), "");
				}
				break;
			}
			default: {
				if(isSegment) sb.append(c);
				break;
			}
			}
		}

		System.out.println("TEMPLATE: "+template);
		if(template.endsWith("?")) template = template.substring(0, template.length() - 1);

		return template;
	}


	private static String calculateTemplate(String segment, Map<String, Object> map) {

		//boolean iHaveQuestionMark = false;
		segment = segment.replaceAll("\\{", "").replaceAll("\\}", "");
		LOGGER.info("segmento: "+segment);
		StringBuilder segmentOutput = new StringBuilder();

		String switchValue = segment.startsWith("%20AND%20")? "%20AND%20" : segment.substring(0, 1);
		switchValue = segment.startsWith("%20and%20")? "%20and%20" : segment.substring(0, 1);

		switch(switchValue) {
		case "?":
			//iHaveQuestionMark = true;
			String[] parameters = segment.replace("?","").split(",");
			segmentOutput.append("");
			LOGGER.info("Pre-output: "+segmentOutput.toString());
			for(int i=0;i<parameters.length;i++) {
				LOGGER.info("Loop-Start: "+segmentOutput.toString());
				if(map.containsKey(parameters[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters[i].trim()+"="+map.get(parameters[i].trim()));
					}
					else {
						segmentOutput.append("&"+parameters[i].trim()+"="+map.get(parameters[i].trim()));
					}
				}
				LOGGER.info("Loop-End: "+segmentOutput.toString());
			}
			//if(iHaveQuestionMark) segmentOutput.insert(0, "?");
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "?");
			break;
		case "&":
			String[] parameters1 = segment.replace("&","").split(",");
			segmentOutput.append("");
			LOGGER.info("Pre-output: "+segmentOutput.toString());
			for(int i=0;i<parameters1.length;i++) { 
				LOGGER.info("Loop-Start: "+segmentOutput.toString());
				if(map.containsKey(parameters1[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters1[i].trim()+"="+map.get(parameters1[i].trim()));
					}
					else {
						segmentOutput.append("&"+parameters1[i].trim()+"="+map.get(parameters1[i].trim()));
					}
				}
				LOGGER.info("Loop-End: "+segmentOutput.toString());
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "&");
			break;
		case "/":
			String[] parameters2 = segment.replace("/","").split(",");
			segmentOutput.append("");
			LOGGER.info("Pre-output: "+segmentOutput.toString());
			for(int i=0;i<parameters2.length;i++) { 
				LOGGER.info("Loop-Start: "+segmentOutput.toString());
				if(map.containsKey(parameters2[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters2[i].trim()+"="+map.get(parameters2[i].trim()));
					}
					else {
						segmentOutput.append("&"+parameters2[i].trim()+"="+map.get(parameters2[i].trim()));
					}
				}
				LOGGER.info("Loop-End: "+segmentOutput.toString());
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "/");;
			break;
		case "%20AND%20":
			String[] parameters3 = segment.replace("%20AND%20","").split(",");
			segmentOutput.append("");
			LOGGER.info("Pre-output: "+segmentOutput.toString());
			for(int i=0;i<parameters3.length;i++) { 
				LOGGER.info("Loop-Start: "+segmentOutput.toString());
				if(map.containsKey(parameters3[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters3[i].trim()+"="+map.get(parameters3[i].trim()));
					}
					else {
						segmentOutput.append("%20AND%20"+parameters3[i].trim()+"="+map.get(parameters3[i].trim()));
					}
				}
				LOGGER.info("Loop-End: "+segmentOutput.toString());
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "%20AND%20");
			break;
		case "%20and%20":
			String[] parameters4 = segment.replace("%20and%20","").split(",");
			segmentOutput.append("");
			LOGGER.info("Pre-output: "+segmentOutput.toString());
			for(int i=0;i<parameters4.length;i++) { 
				LOGGER.info("Loop-Start: "+segmentOutput.toString());
				if(map.containsKey(parameters4[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters4[i].trim()+"="+map.get(parameters4[i].trim()));
					}
					else {
						segmentOutput.append("%20and%20"+parameters4[i].trim()+"="+map.get(parameters4[i].trim()));
					}
				}
				LOGGER.info("Loop-End: "+segmentOutput.toString());
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "%20and%20");
			break;
		default:
			String[] parameters11 = segment.split(",");
			for(int i=0;i<parameters11.length;i++) {
				if(map.containsKey(parameters11[i].trim())) {	
					segmentOutput.append(","+map.get(parameters11[i].trim()));
				}
			}try {
				if(segmentOutput.length()>0) {
					String aux =  segmentOutput.substring(1, segmentOutput.length());
					segmentOutput = new StringBuilder();
					segmentOutput.append(aux);
				}
			}catch(Exception e) {
				LOGGER.error(e.getMessage());
				return null;
			}
			break;
		}

		System.out.println("Segment-output: "+segmentOutput.toString());

		return segmentOutput.toString();
	}

	public static String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
	
	public static String ogcWFSChecker(String url) {
		String[] questionMarkSplit = url.split("\\?", 2);
		String[] parametersSplit = questionMarkSplit[1].split("\\&");

		ArrayList<String> parametersAdjusted = new ArrayList<String>();
		for(int i = 0; i<parametersSplit.length;i++) {
			if(parametersSplit[i].contains("CQL_FILTER")) {
				String CQLContext = parametersSplit[i].replaceAll("CQL_FILTER=", "");
				if(!CQLContext.isBlank()) {
					String[] splits = CQLContext.split("%20AND%20");
					ArrayList<String> updatedSplit = new ArrayList<String>();
					for(int j=0;j<splits.length;j++) {
						if(!splits[j].isBlank()) {
							if(splits[j].contains("bbox")) {
								int splitSize = splits[j].replaceAll("bbox", "").replaceAll("\\(", "").replaceAll("\\)", "").split("\\,").length;
								if(splitSize==5) updatedSplit.add(splits[j]);
							}
							else updatedSplit.add(splits[j]);
						}
					}
					if(updatedSplit.size()>0) {
						parametersAdjusted.add("CQL_FILTER="+String.join("%20AND%20", updatedSplit));
					}
				}
			}
			else parametersAdjusted.add(parametersSplit[i]);
		}

		return questionMarkSplit[0]+"?"+String.join("&", parametersAdjusted);
	}

}

