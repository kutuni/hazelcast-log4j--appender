package kutuni.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;

public class AppTestWithHC {


	
	public static void main(String[] args) {
    	Config cfg = new Config();                  
    	NetworkConfig network = cfg.getNetworkConfig();
    	cfg.setInstanceName("local");
    	network.setPort(5701);
    	network.setReuseAddress( true );
    	JoinConfig join = network.getJoin();
    	join.getMulticastConfig().setEnabled(true);
    	
    	join.getTcpIpConfig().setEnabled(false);
    	network.getInterfaces().setEnabled(true).addInterface("192.168.0.*");
    			
    	
		Hazelcast.newHazelcastInstance(cfg);

		System.out.println("wait...");
		//sleep();
		Logger log = LogManager.getLogger(AppTestWithHC.class.getName());
		
		log.debug("debug test log message with allready hazelcast instance");
		sleep();
		System.exit(0);
	}

	public static void sleep()
	{
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
