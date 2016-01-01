package kutuni.log4j2;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class HazelcastAppenderTest {
	static HazelcastInstance hz=null;
	
	@BeforeClass
	public static void init() throws Exception {
    	com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();                  
    	NetworkConfig network = cfg.getNetworkConfig();
    	cfg.setInstanceName("local");
    	network.setPort(5701);
    	network.setReuseAddress( true );
    	JoinConfig join = network.getJoin();
    	join.getMulticastConfig().setEnabled(true);
    	join.getTcpIpConfig().setEnabled(false);
    	network.getInterfaces().setEnabled(true).addInterface("192.168.0.*");
    	
		hz = Hazelcast.newHazelcastInstance(cfg);
		final IQueue<Map> queue = hz.getQueue("loggerQ");
	}
	

	@Test
	public void testLog() {
		final Logger logger = LogManager.getLogger(getClass());
		logger.debug("Hello World");
	}

	@Test
	public void testMarker() {
		final Logger logger = LogManager.getLogger("test");
		final Marker parent = MarkerManager.getMarker("PARENT");
		final Marker marker = MarkerManager.getMarker("TEST").addParents(parent);
		logger.debug(marker, "Marker test");
	}

	
	public void testException() {
		final Logger logger = LogManager.getLogger("test");

		try {
			throw new Exception("Test");
		} catch (Exception e) {
			e.fillInStackTrace();
			logger.error("Error:", e);
		}
	}

	@Test
	public void testThreadContext() {
		final Logger logger = LogManager.getLogger(getClass());

		ThreadContext.push("add key value variables from thread");
		ThreadContext.push("int", 1);
		ThreadContext.push("int-long-string", 1, 2l, "3");
		ThreadContext.put("key", "value");

		logger.debug("Hello World");

		ThreadContext.clearAll();
	}

	@AfterClass
	public static void shutdown() throws InterruptedException {
		hz.shutdown();
		Thread.sleep(500);
	}
}
