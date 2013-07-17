# AppDynamics Pingdom Monitoring Extension

##Use Case
Pingdom ([https://www.pingdom.com/](https://www.pingdom.com/)) is a service that tracks website uptime, 
downtime, and performance. The Pingdom extension uses the REST API from Pingdom to retrieve key
metrics for your Pingdom Checks. It also retrieves other important data 
such as remaining Checks and remaining SMS credits.


##Installation

The monitor.xml file is used to execute the Java code that will start the monitoring extension. It contains
tags for your Pingdom credentials, as well as a tag where you can specify your own metric
path. If you specify the metric path, only a single tier will receive metrics from this
monitor.  

1. Run 'ant package' from the pingdom-monitoring-extension directory
2. Download the file PingdomMonitor.zip found in the 'dist' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In monitor.xml in the PingdomMonitor directory:
    1.  set the credentials to your Pingdom account
    2.  configure your own metric path (optional)
5. Restart the machineagent
6. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Pingdom Monitor or your specified path.

##Files

Files/Folders Included:

<table><tbody>
<tr>
<th align = 'left'> Directory/File </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> conf </td>
<td class='confluenceTd'> Contains the monitor.xml </td>
</tr>
<tr>
<td class='confluenceTd'> lib </td>
<td class='confluenceTd'> Contains third-party project references </td>
</tr>
<tr>
<td class='confluenceTd'> src </td>
<td class='confluenceTd'> Contains source code to Pingdom Monitoring Extension </td>
</tr>
<tr>
<td class='confluenceTd'> dist </td>
<td class='confluenceTd'> Only obtained when using ant. Run 'ant build' to get binaries. Run 'ant package' to get the distributable .zip file </td>
</tr>
<tr>
<td class='confluenceTd'> build.xml </td>
<td class='confluenceTd'> Ant build script to package the project (required only if changing Java code) </td>
</tr>
</tbody>
</table>


##monitor.xml
<table>
<th align = 'left'> Parameter </th>
<th align = 'left'> Description </th>
<tr>
<td>Username
</td>
<td>Pingdom username
</td>
</tr>
<tr>
<td>Password
</td>
<td>Pingdom password
</td>
</tr>
<tr>
<td>App-Key
</td>
<td>The Pingdom Application Key, found at https://my.pingdom.com/account/appkeys
</td>
</tr>
<tr>
<td>Metric-Path
</td>
<td>Optional: set your own metric path. The pattern is: Server | Component:id or name | Pingdom Monitor 
</td>
</tr>
</table>

~~~~

<monitor>
        <name>PingdomMonitor</name>
        <type>managed</type>
        <description>Reports key metrics from Pingdom using REST</description>
        <monitor-configuration></monitor-configuration>
        <monitor-run-task>
                <execution-style>continuous</execution-style>
                <name>Pingdom Monitor Run Task</name>
                <display-name>Pingdom Monitor Task</display-name>
                <description>Pingdom Monitor Task</description>
                <type>java</type>
                <java-task>
                        <classpath>PingdomMonitor.jar;lib/json-simple-1.1.1.jar;/lib/httpclient/commons-codec-1.6.jar;lib/httpclient/commons-logging-1.1.1.jar;lib/httpclient/fluent-hc-4.2.5.jar;lib/httpclient/httpclient-4.2.5.jar;lib/httpclient/httpclient-cache-4.2.5.jar; lib/httpclient/httpcore-4.2.4.jar;lib/httpclient/httpmime-4.2.5.jar</classpath>
                        <impl-class>com.appdynamics.monitors.pingdom.PingdomMonitor</impl-class>
                </java-task>
 
                <task-arguments>
                  <!-- CONFIGURE USER CREDENTIALS:
                       App-Key: you generate your application key inside the Pingdom control panel
                       (default value of App-Key is a non-working dummy, to show what the format looks like)
                  -->
                        <argument name="Username" is-required="true" default-value="Username"/>
                        <argument name="Password" is-required="true" default-value="Password"/>
                        <argument name="App-Key" is-required="true" default-value="zoent8w9cbt810rsdkweir23vcxb87zrt5541"/>
 
                        <!-- CONFIGURE METRIC PATH (OPTIONAL):
                       You can configure a metric path, such that only one tier is going to receive
                       metrics from this monitor. The pattern is: Server|Component:<id or name>|Pingdom Monitor
                       Default (if default-value="") is "Custom Metrics|Pingdom Monitor" under 
                       Application Infrastructure Performance in every tier
                  -->
                        <argument name="Metric-Path" is-required="false" default-value=""/>
                </task-arguments>
        </monitor-run-task>
</monitor>

~~~~

##Custom Dashboard example
The custom dashboard below contains a custom heading graphic, three monitors, and an iframe containing an 
external web site (on the right).


![](images/pingdom_01.png)

##Metrics

###Checks

####For each website which Pingdom is tracking:


<table><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> id </td>
<td class='confluenceTd'> The Pingdom Check id </td>
</tr>
<tr>
<td class='confluenceTd'> lastresponsetime </td>
<td class='confluenceTd'> The latest response time Pingdom observed </td>
</tr>
<tr>
<td class='confluenceTd'> lasttesttime </td>
<td class='confluenceTd'> The last time Pingdom made a Check on the website (hour of day) </td>
</tr>
<tr>
<td class='confluenceTd'> resolution </td>
<td class='confluenceTd'> The time resolution of the Check </td>
</tr>
<tr>
<td class='confluenceTd'> status </td>
<td class='confluenceTd'> 0: down, 1: up, 5: unconfirmed_down, 20: unknown, 50: paused </td>
</tr>
</tbody>
</table>


###Credits


<table><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> autofillsms </td>
<td class='confluenceTd'> 0: disabled, 1: enabled </td>
</tr>
<tr>
<td class='confluenceTd'> availablechecks </td>
<td class='confluenceTd'> Free check slots available for new checks </td>
</tr>
<tr>
<td class='confluenceTd'> availablesms </td>
<td class='confluenceTd'> SMS credits remaining on this account </td>
</tr>
<tr>
<td class='confluenceTd'> availablesmstests </td>
<td class='confluenceTd'> SMS provider tests remaining on this account </td>
</tr>
<tr>
<td class='confluenceTd'> checklimit </td>
<td class='confluenceTd'> Total number of check slots on this account </td>
</tr>
</tbody>
</table>



### Limits

The Pingdom API has usage limits to avoid individual rampant applications degrading the overall user experience. There are two layers of limits, the first cover a shorter period of time and the second a longer period. Although we never ran into any troubles with our interval of REST API calls alone, this gives you a view of the total usage of the REST API for your account. If you have other applications calling the Pingdom REST API, you can monitor the remaining calls with these metrics

#### Req-Limit-Short

<table><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Remaining </td>
<td class='confluenceTd'> The number of requests left until the short limit is reached </td>
</tr>
<tr>
<td class='confluenceTd'> Time until reset </td>
<td class='confluenceTd'> Number of seconds until the short limit will be reset </td>
</tr>
</tbody>
</table>

#### Req-Limit-Long

<table><tbody>
<tr>
<th align = 'left'> Metric Name </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> Remaining </td>
<td class='confluenceTd'> The number of requests left until the long limit is reached </td>
</tr>
<tr>
<td class='confluenceTd'> Time until reset </td>
<td class='confluenceTd'> Number of seconds until the long limit will be reset </td>
</tr>
</tbody>
</table>

##Contributing

Always feel free to fork and contribute any changes directly via GitHub.


##Support

For any support questions, please contact ace@appdynamics.com.
