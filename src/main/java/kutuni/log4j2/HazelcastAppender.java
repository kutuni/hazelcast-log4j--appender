package kutuni.log4j2;

import java.io.Serializable;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.net.Severity;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

@Plugin(name = "HAZELCAST", category = "Core", elementType = "appender", printObject = true)
public class HazelcastAppender extends AbstractAppender {
    private static final long serialVersionUID = 0L;

    //private static final Logger LOG = StatusLogger.getLogger();

    private boolean includeSource;
    private boolean includeThreadContext;
    private boolean includeStackTrace;
    private Map<String, String> additionalFields;
    //private String name;
    private boolean newInstance;
    private String instanceName;
    private String queueName;
    HazelcastInstance instance;
    //IQueue queue;
    int connectionAttemptPeriod;
    int connectionAttemptLimit;
    String address;

    protected HazelcastAppender(String name,
                           Layout<? extends Serializable> layout,
                           Filter filter,
                           boolean newInstance,
                           String instanceName,
                           String queueName,
                           boolean includeSource,
                           boolean includeThreadContext,
                           boolean includeStackTrace,
                           int connectionAttemptPeriod,
                           int connectionAttemptLimit,
                           String address,
                           String additionalFields) {
    	
        super(name, filter, layout, false);
        this.includeSource = includeSource;
        this.includeThreadContext = includeThreadContext;
        this.includeStackTrace = includeStackTrace;
        this.newInstance=newInstance;
        this.instanceName=instanceName;
        this.queueName=queueName;
        this.connectionAttemptPeriod=connectionAttemptPeriod;
        this.connectionAttemptLimit=connectionAttemptLimit;
        this.address=address;
        if (null != additionalFields && !additionalFields.isEmpty()) {
            this.additionalFields = new HashMap<String,String>();

            try {
                String[] values = additionalFields.split(",");
                for (String s : values) {
                    String[] nvp = s.split("=");
                    this.additionalFields.put(nvp[0], nvp[1]);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read additional fields.", e);
            }
        } else {
            this.additionalFields = Collections.emptyMap();
        }
    }

    @Override
    public void append(LogEvent event) 
    {
        		Map mp=new HashMap();
        		String message=event.getMessage().getFormattedMessage();
        		mp.put("message",message);
                long time=event.getTimeMillis();
                mp.put("time",time);
                int errCode=Severity.getSeverity(event.getLevel()).getCode();
                mp.put("errCode", errCode);
                String loggerName= event.getLoggerName();
                mp.put("loggerName",loggerName);
                String threadName=event.getThreadName();
                mp.put("threadName",threadName);

        final Marker marker = event.getMarker();
        if (marker != null) {
            String marketName= marker.getName();
            mp.put("marketName", marketName);
        }

        if (includeThreadContext) {
        	mp.putAll(event.getContextMap());
        	
            final List<String> contextStack = event.getContextStack().asList();
            if (contextStack != null && !contextStack.isEmpty()) {
                mp.put("contextStack", contextStack.toString());
            }
        }

        final StackTraceElement source = event.getSource();
        if (includeSource && source != null) 
        {
            mp.put("sourceFileName", source.getFileName());
            mp.put("sourceMethodName", source.getMethodName());
            mp.put("sourceClassName", source.getClassName());
            mp.put("sourceLineNumber", source.getLineNumber()+"");
        }

        @SuppressWarnings("all")
        final Throwable thrown = event.getThrown();
        if (includeStackTrace && thrown != null) {
            final StringBuilder stackTraceBuilder = new StringBuilder();

            for (StackTraceElement stackTraceElement : thrown.getStackTrace()) {
                new Formatter(stackTraceBuilder).format("%s.%s(%s:%d)%n",
                        stackTraceElement.getClassName(),
                        stackTraceElement.getMethodName(),
                        stackTraceElement.getFileName(),
                        stackTraceElement.getLineNumber());
            }

            mp.put("exceptionClass", thrown.getClass().getCanonicalName());
            mp.put("exceptionMessage", thrown.getMessage());
            mp.put("exceptionStackTrace", stackTraceBuilder.toString());

            mp.put("fullmessage",event.getMessage().getFormattedMessage() + "\n\n" + stackTraceBuilder.toString());
        }
        
        if (!additionalFields.isEmpty()) 
        {
        	mp.putAll(additionalFields);
        }

        try 
        {
        	IQueue queue=instance.getQueue(this.queueName);
        	queue.put(mp);
            //put ddata 2 map ,multimap or queue
            
        } catch (Exception e) {
            throw new AppenderLoggingException("failed to write log event to HClog server: " + e.getMessage(), e);
        }
    }

    @Override
    public void start() 
    {
        super.start();
    	if (newInstance) 
    	{
    		LOGGER.debug("new Hazelcast Instance creating...");
    		ClientConfig clientConfig = new ClientConfig();
    		//ClientNetworkConfig network = clientConfig.getNetworkConfig();
    		clientConfig.getNetworkConfig().setConnectionAttemptPeriod(connectionAttemptPeriod);
    		clientConfig.getNetworkConfig().setConnectionAttemptLimit(connectionAttemptLimit);
    		clientConfig.getNetworkConfig().addAddress(address);
    		
    		clientConfig.setInstanceName( "log4j-instance" );
    		instance = HazelcastClient.newHazelcastClient(clientConfig);
    	}
    	else
    	{
    		LOGGER.debug("Hazelcast instance getName:"+instanceName);
    		instance = Hazelcast.getHazelcastInstanceByName(instanceName);
    	}
    }

    @Override
    public void stop() {
        super.stop();
        if (newInstance)
        {
        	try {
				Thread.sleep(5000); //wait for sync
			} catch (InterruptedException e) {
			}
        	instance.shutdown();
        }
    }

    @Override
    public String toString() {
        return HazelcastAppender.class.getSimpleName() + "{"
                + "name=" + getName()
                + ",newInstance=" + newInstance
                + ",instanceName=" + instanceName
                + ",address=" + address
                + ",mapName=" + queueName
                + "}";
    }
    
    @PluginFactory
    public static HazelcastAppender createHazelcastAppender(@PluginElement("Filter") Filter filter,
                                                  @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                  @PluginAttribute(value = "name") String name,
                                                  @PluginAttribute(value = "newInstance", defaultBoolean = true) Boolean newInstance,
                                                  @PluginAttribute(value = "instanceName") String instanceName,
                                                  @PluginAttribute(value = "queueName") String queueName, 
                                                  @PluginAttribute(value = "includeSource", defaultBoolean = true) Boolean includeSource,
                                                  @PluginAttribute(value = "includeThreadContext", defaultBoolean = true) Boolean includeThreadContext,
                                                  @PluginAttribute(value = "includeStackTrace", defaultBoolean = true) Boolean includeStackTrace,
                                                  @PluginAttribute(value = "connectionAttemptPeriod", defaultInt = 5000) int connectionAttemptPeriod,
                                                  @PluginAttribute(value = "connectionAttemptLimit", defaultInt = 9999) int connectionAttemptLimit,
                                                  @PluginAttribute(value = "address", defaultString = "127.0.0.1") String address,
                                                  @PluginAttribute(value = "additionalFields") String additionalField) {
        if (layout == null) 
        {
            layout = PatternLayout.createDefaultLayout();
        }
        return new HazelcastAppender(name, layout, filter, newInstance, instanceName, queueName,includeSource,includeThreadContext,includeStackTrace,connectionAttemptPeriod,connectionAttemptLimit,address, additionalField);
    }
}