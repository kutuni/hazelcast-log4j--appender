# Hazelcast Appender for Apache Log4j2

This appender for [Apache Log4j2](https://logging.apache.org/log4j/2.x/) logs messages send to a [Graylog2](http://www.graylog2.org) servers by [Hazelcast](http://hazelcast.org) cluster. Plugin designed work together [Graylog2-HCPlugin](https://github.com/kutuni/graylog-plugin-hazelcast).


You can specify the following parameters for the GELF appender in the `log4j2.xml` configuration file:

* `newInstance` false mean, jvm allready have a intialized hazelcast instance. Accessing instance from instaceName.
				 true mean, appender initialized new hazelcastclient instance. Using `address` (ip/hostname) value.
* `instanceName` if using with allready have instance options, need accessing hazelcast instance from `instanceName`.
* `queueName` HC Graylog Plugin configured by default queue name is loggerQ. Dont change.
* `connectionAttemptPeriod` Please refer to hazelcast documantation. [Ref](http://docs.hazelcast.org/docs/3.3/manual/html/javaclient.html#network-configuration-options)
* `connectionAttemptLimit` Please refer to hazelcast documantation. [Ref](http://docs.hazelcast.org/docs/3.3/manual/html/javaclient.html#network-configuration-options)
* `address` if set `newInstance`=true, appender initializing new hazelcastclientInstance connect to `address`.
  
* `additionalFields`
  * Comma-delimited list of key=value pairs to be included in every message

## Log4j2.xml example

    <Configuration status="info" packages="kutuni.log4j2">
	<Appenders>
		<HAZELCAST name="hazelcastAppender" 
			newInstance="true"
			instanceName="local" 
			queueName="loggerQ" 
			connectionAttemptPeriod="1000"
			connectionAttemptLimit="3" 
			address="192.168.0.14"
			additionalFields="appName=unittest,hostname=${hostName},jdk=${java:runtime},context=${web:servletContextName}" />
		<Console name="STDOUT" target="SYSTEM_OUT">
			<PatternLayout
				pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] [%thread] [%c{1.}] - %m%n" />
		</Console>
	</Appenders>
	<Loggers>
		<Logger name="kutuni" 
			level="trace">
		</Logger>
	<Loggers>
		<Logger name="kutuni" 
			level="trace">
		</Logger>
		<Root level="error">
			<AppenderRef ref="hazelcastAppender" />
 			<AppenderRef ref="STDOUT"/>
		</Root>
	</Loggers>
	</Configuration>


## Java code example

    Logger logger = LogManager.getLogger(getClass());
    logger.info("User logon:"+userCredential);

## Using variables in the additionalFields

The `additionalFields` attribute can contain references to variables. 
In order for Log4j 2.x to resolve the variable's value, the variable name must have a certain prefix depending on how the variable is provided.
Internally we're making use of Log4j's [StrSubstitutor](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/StrSubstitutor.html) to resolve the variable's value. 
This in turn is utilizing the following [Log4j Lookups](https://logging.apache.org/log4j/2.x/manual/lookups.html) with the prefixes in the following list:


* `date`        [DateLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/DateLookup.html)                                         
* `sd`          [StructuredDataLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/StructuredDataLookup.html)                     
* `java`        [SystemPropertiesLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/SystemPropertiesLookup.html)                 
* `ctx`         [ContextMapLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/ContextMapLookup.html)                             
* `jndi`        [JndiLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/JndiLookup.html)                                         
* `jvmrunargs`  [JmxRuntimeInputArgumentsLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/JmxRuntimeInputArgumentsLookup.html) 
* `env`         [EnvironmentLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/EnvironmentLookup.html)                           
* `sys`         [SystemPropertiesLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/SystemPropertiesLookup.html)                 
* `map`         [MapLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/MapLookup.html)                                           
* `bundle`      [ResourceBundleLookup](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/lookup/ResourceBundleLookup.html)                     

Please read up on the different variable handling in the linked Javadocs.
### Example configuration with variables

      additionalFields="user=${env:USER},CLIargument=${sys:cliargument},jvm=${java:vm},fileEncoding=${sys:file.encoding}""/>




# Installation

[Download the appender](https://github.com/kutuni/hazelcast-log4j--appender/blob/master/target/log4j2-hcappender-1.0.0-SNAPSHOT.jar)
or create a clone after than compile by mvn package. 

