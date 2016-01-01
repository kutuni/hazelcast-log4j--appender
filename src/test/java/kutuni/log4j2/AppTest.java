package kutuni.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppTest {

	private static Logger log = LogManager.getLogger(AppTest.class.getName());

	
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			log.debug("debug test log message"+i);	
		}
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
