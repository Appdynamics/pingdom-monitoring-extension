/**
 * Copyright 2013 AppDynamics
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


package com.appdynamics.monitors.pingdom.communicator;


import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class PingdomCommunicator {

	private String username, password, appkey;
	private final String baseAddress = "https://api.pingdom.com";
	Logger logger;

	public PingdomCommunicator(String username, String password, String appkey, Logger logger){
		this.username = username;
		this.password = password;
		this.appkey = appkey;
		this.logger = logger;
	}


	public void populate(Map<String, Integer> metrics) {

		getCredits(metrics);

		// getChecks also has getLimits, to save time and requests it
		// takes the necessary information from its given header
		getChecks(metrics);

	}

	@SuppressWarnings("rawtypes")
	private void getCredits(Map<String, Integer> metrics){

		try {
			HttpClient httpclient = new DefaultHttpClient();

			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username,password);			
			HttpGet httpget = new HttpGet(baseAddress + "/api/2.0/credits");
			httpget.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
			httpget.addHeader("App-Key", appkey);

			HttpResponse response;
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();


			// reading in the JSON response
			String result = "";
			if(entity != null) {
				InputStream instream = entity.getContent();
				int b;
				try{
					while((b = instream.read()) != -1){
						result += Character.toString((char) b);
					}
				} finally {
					instream.close();
				}
			}

			// parsing the JSON response
			try{

				JSONParser parser = new JSONParser();

				ContainerFactory containerFactory = new ContainerFactory(){
					public List creatArrayContainer() {
						return new LinkedList();
					}					
					public Map createObjectContainer() {
						return new LinkedHashMap();
					}
				};

				// retrieving the metrics and populating HashMap
				JSONObject obj = (JSONObject) parser.parse(result);
				if(obj.get("credits") == null){
					logger.error("Error retrieving data. " + obj);
					return;
				}
				Map json = (Map) parser.parse(obj.get("credits").toString(), containerFactory);

				if(json.containsKey("autofillsms")){
					if(json.get("autofillsms").toString().equals("false")){
						metrics.put("Credits|autofillsms", 0);
					} else if(json.get("autofillsms").toString().equals("true")) {
						metrics.put("Credits|autofillsms", 1);
					} else {
						logger.error("can't determine whether Credits|autofillsms is true or false!");
					}
				}

				if(json.containsKey("availablechecks")){
					try{
						metrics.put("Credits|availablechecks", Integer.parseInt(json.get("availablechecks").toString()));
					} catch(NumberFormatException e){
						logger.error("Error parsing metric value for Credits|availablechecks!");
					}
				}

				if(json.containsKey("availablesms")){
					try{
						metrics.put("Credits|availablesms", Integer.parseInt(json.get("availablesms").toString()));
					} catch(NumberFormatException e){
						logger.error("Error parsing metric value for Credits|availablesms!");
					}
				}

				if(json.containsKey("availablesmstests")){
					try{
						metrics.put("Credits|availablesmstests", Integer.parseInt(json.get("availablesmstests").toString()));
					} catch(NumberFormatException e){
						logger.error("Error parsing metric value for Credits|availablesmstests!");
					}
				}

				if(json.containsKey("checklimit")){
					try{
						metrics.put("Credits|checklimit", Integer.parseInt(json.get("checklimit").toString()));
					} catch(NumberFormatException e){
						logger.error("Error parsing metric value for Credits|checklimit!");
					}
				}

			} catch(ParseException e) {
				logger.error("JSON Parsing error: " + e.getMessage());
			} catch(Throwable e) {
				logger.error(e.getMessage());
			}

		} catch (IOException e1) {
			logger.error(e1.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
		}

	}


	@SuppressWarnings("rawtypes")
	private void getChecks(Map<String, Integer> metrics){

		try {
			HttpClient httpclient = new DefaultHttpClient();

			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username,password);			
			HttpGet httpget = new HttpGet(baseAddress + "/api/2.0/checks");
			httpget.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
			httpget.addHeader("App-Key", appkey);

			HttpResponse response;
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();




			// reading in the JSON response
			String result = "";
			if(entity != null) {
				InputStream instream = entity.getContent();
				int b;
				try{
					while((b = instream.read()) != -1){
						result += Character.toString((char) b);
					}
				} finally {
					instream.close();
				}
			}



			// parsing the JSON response
			try{

				JSONParser parser = new JSONParser();

				ContainerFactory containerFactory = new ContainerFactory(){
					
					public List creatArrayContainer() {
						return new LinkedList();
					}

					public Map createObjectContainer() {
						return new LinkedHashMap();
					}

				};

				// retrieving the metrics and populating HashMap
				JSONObject obj = (JSONObject) parser.parse(result);
				if(obj.get("checks") == null){
					logger.error("Error retrieving data. " + obj);
					return;
				}
				JSONArray array = (JSONArray) parser.parse(obj.get("checks").toString());
				for(Object checkObj : array){
					JSONObject check = (JSONObject) checkObj;

					Map json = (Map) parser.parse(check.toJSONString(), containerFactory);

					String metricName = "";

					if(json.containsKey("name")){
						metricName = "Checks|" + json.get("name") + "|";						
					} else {
						logger.error("Encountered error while parsing metrics for a check: no name found!");
						continue;
					}

					if(json.containsKey("id")){
						try{
							metrics.put(metricName + "id", Integer.parseInt(json.get("id").toString()));
						} catch(NumberFormatException e){
							logger.error("Error parsing metric value for " + metricName + "id");
						}
					}

					if(json.containsKey("lastresponsetime")){
						try{
							metrics.put(metricName + "lastresponsetime", Integer.parseInt(json.get("lastresponsetime").toString()));
						} catch(NumberFormatException e){
							logger.error("Error parsing metric value for " + metricName + "lastresponsetime");
						}
					}

					if(json.containsKey("lasttesttime")){
						try{
							int testTime = Integer.parseInt(json.get("lasttesttime").toString());
							java.util.Date date = new java.util.Date(testTime);
							Calendar cal = GregorianCalendar.getInstance();
							cal.setTime(date);

							metrics.put(metricName + "lasttesttime", cal.get(Calendar.HOUR_OF_DAY));
						} catch(NumberFormatException e){
							logger.error("Error parsing metric value for " + metricName + "lasttesttime");
						} catch(Throwable t) {
							logger.error("Error parsing metric value for " + metricName + "lasttesttime: can't get hour of day");
						}
					}

					if(json.containsKey("resolution")){
						try{
							metrics.put(metricName + "resolution", Integer.parseInt(json.get("resolution").toString()));
						} catch(NumberFormatException e){
							logger.error("Error parsing metric value for " + metricName + "resolution");
						}
					}

					if(json.containsKey("status")){
						String status = json.get("status").toString();
						if(status != null){
							if(status.equals("down")){
								metrics.put(metricName + "status", 0);
							} else if(status.equals("up")){
								metrics.put(metricName + "status", 1);
							} else if(status.equals("unconfirmed_down")){
								metrics.put(metricName + "status", 5);
							} else if(status.equals("unknown")){
								metrics.put(metricName + "status", 20);
							} else if(status.equals("paused")){
								metrics.put(metricName + "status", 50);
							} else {
								logger.error("Error parsing metric value for " + metricName + "status: Unknown status '" + status + "'");
							}
						} else {
							logger.error("Error parsing metric value for " + metricName + "status");
						}
					}
				}


			} catch(ParseException e) {
				logger.error("JSON Parsing error: " + e.getMessage());
			} catch(Throwable e) {
				logger.error(e.getMessage());
			}


			// parse header in the end to get the Req-Limits
			Header[] responseHeaders = response.getAllHeaders();
			getLimits(metrics, responseHeaders);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
		}


	}


	/**
	 * Parse the headers of the HTTP request for the remaining allowed requests in the remaining time frame
	 * @param metrics
	 * @param responseHeaders
	 */
	private void getLimits(Map<String, Integer> metrics, Header[] responseHeaders){

		for(Header header : responseHeaders){
			if(header.getName().equals("Req-Limit-Short")){
				String[] value = header.getValue().split("\\s+");
				try{
					metrics.put("Limits|Req-Limit-Short|Remaining", Integer.parseInt(value[1]));
					metrics.put("Limits|Req-Limit-Short|Time until reset", Integer.parseInt(value[5]));
				} catch(NumberFormatException e){
					logger.error("Error parsing metric value for Limits|Req-Limit-Short" + e.getMessage());
				}
			} else if(header.getName().equals("Req-Limit-Long")){
				String[] value = header.getValue().split("\\s+");
				try{
					metrics.put("Limits|Req-Limit-Long|Remaining", Integer.parseInt(value[1]));
					metrics.put("Limits|Req-Limit-Long|Time until reset", Integer.parseInt(value[5]));
				} catch(NumberFormatException e){
					logger.error("Error parsing metric value for Limits|Req-Limit-Long" + e.getMessage());
				}
			}
		}
	}
}

