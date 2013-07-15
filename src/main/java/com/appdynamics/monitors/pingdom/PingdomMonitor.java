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


package com.appdynamics.monitors.pingdom;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.monitors.pingdom.communicator.PingdomCommunicator;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class PingdomMonitor extends AManagedMonitor{

	private Map<String, Integer> metrics;
	private Logger logger;
	private String username = "";
	private String password = "";
	private String appKey = "";
	private String metricPath = "Custom Metrics|Pingdom Monitor|";
	PingdomCommunicator pingdomCommunicator;

	/**
	 * Testing purposes: To see if connection works, run this java file alone with three arguments:
	 * Username, Password, App-Key
	 * 
	 * Prints all metric names and their value from one round of REST calls
	 * @param args
	 */
	
	public static void main(String[] args){

		if(args.length != 3){
			System.err.println("3 arguments needed: username, password, appkey!");
			return;
		}	
		

		String username = args[0];
		String password = args[1];
		String appkey = args[2];
		

		PingdomMonitor pm = new PingdomMonitor();
		pm.logger = Logger.getLogger(PingdomMonitor.class);
		pm.username = username;
		pm.password = password;
		pm.appKey = appkey;

		
		PingdomCommunicator pingdomCommunicator = new PingdomCommunicator(username,password,appkey, Logger.getLogger(PingdomMonitor.class));
		Map<String, Integer> metrics = new HashMap<String, Integer>();
		pingdomCommunicator.populate(metrics);

		
		for(String key : metrics.keySet()){
			System.out.println(key + " ==> " + metrics.get(key));
		}
	}

	@Override
	public TaskOutput execute(Map<String, String> taskArguments,
			TaskExecutionContext taskContext) throws TaskExecutionException {

		logger = Logger.getLogger(PingdomMonitor.class);
		metrics = new HashMap<String, Integer>();



		if(!taskArguments.containsKey("Username") || !taskArguments.containsKey("Password") || !taskArguments.containsKey("App-Key")){
			logger.error("monitor.xml must contain task arguments 'Username', 'Password', and 'App-Key'!" +
					" Terminating Monitor.");
			return null;
		}

		username = taskArguments.get("Username");
		password = taskArguments.get("Password");
		appKey = taskArguments.get("App-Key");
		
		// setting the custom metric path, if there is one in monitor.xml
		if(taskArguments.containsKey("Metric-Path") && taskArguments.get("Metric-Path") != ""){
			metricPath = taskArguments.get("Metric-Path");
			if(!metricPath.endsWith("|")){
				metricPath += "|";
			}
		}
	
		// initializing the communication to the PingDom API
		pingdomCommunicator = new PingdomCommunicator(username, password, appKey, logger);

		while(true){
			(new PrintMetricsClearHashmapThread()).start();
			try{
				Thread.sleep(60000);
			} catch (InterruptedException e){
				logger.error("Pingdom Monitor interrupted. Quitting Pingdom Monitor.");
				return null;
			}
		}

	}



	/**
	 * Returns the metric to the AppDynamics Controller.
	 * @param 	metricValue		Value of the Metric
	 */
	private void printAllMetrics()
	{
		for(String key : metrics.keySet())
		{
			MetricWriter metricWriter = getMetricWriter(metricPath + key, 
					MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
					MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
					MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);

			metricWriter.printMetric(String.valueOf(metrics.get(key)));

		}

	}

	private class PrintMetricsClearHashmapThread extends Thread{
		public void run(){
			pingdomCommunicator.populate(metrics);
			printAllMetrics();		

			metrics.clear();
		}
	}
}
