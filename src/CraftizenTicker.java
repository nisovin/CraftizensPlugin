
import java.util.logging.*;

public class CraftizenTicker implements Runnable {
	static final Logger log = Logger.getLogger("Minecraft");

	private int interval;
	private boolean running;

	public CraftizenTicker(int interval) {
		this.interval = interval;
		running = false;
	}
	
	public void run() {
		try {
			running = true;
			Thread.sleep(10000);
			while (running) {
				for (Craftizen npc : Craftizens.npcs) {
					npc.tick(interval);
				}
			
				Thread.sleep(interval);
			}			
		} catch (InterruptedException ex) {
		
		}
	}
	
	public void stop() {
		running = false;
	}

}
