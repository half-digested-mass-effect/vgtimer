package vgtimer;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import vgtimer.ValeGuardianTimer.Timer;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

public class VGTimerInterface extends JFrame implements HotKeyListener 
{
	/** Title for the GUI window. */
	public static final String WINDOW_TITLE = "Vale Guardian Timer";
	/** The interval between two updates of the GUI and timers. */
	public static final int UPDATE_INTERVAL_IN_MS = 100;

	/** Configuration */
	private Config config;
	
	/** Global hotkey for resetting all timers to their starting values and immediately starting them. */
	private KeyStroke resetAndStartKeyStroke;
	/** Global hotkey for stopping all timers. */
	private KeyStroke stopKeyStroke;
	/** Maps a global hotkey to the timer it resets. */
	private Map<KeyStroke, Timer> individualResetKeys;
	
	public static void main(String[] args)
	{
		VGTimerInterface c = new VGTimerInterface();
		c.setVisible(true);
		try {
			while(true)
			{
				c.update();
				Thread.sleep(UPDATE_INTERVAL_IN_MS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}
	
	// interface elements
	private JPanel background;
	private JLabel greenClock;
	private JLabel greenStrikeClock;
	private JLabel breakClock;
	private JLabel segmentClock;
	private JLabel blueClock;
	
	private ValeGuardianTimer vgTimer;
	
	public VGTimerInterface()
	{
		config = new Config("vgtimer.properties");
		individualResetKeys = new HashMap<>();
		
		// Define keys that apply to all timers.
		resetAndStartKeyStroke = KeyStroke.getKeyStroke(config.getResetKeys());
		stopKeyStroke = KeyStroke.getKeyStroke(config.getStopKeys());

		// Define keys for resetting individual timers and add them to the map.
		individualResetKeys.put(KeyStroke.getKeyStroke(config.getRestartGreenKeys()), Timer.GREEN);
		individualResetKeys.put(KeyStroke.getKeyStroke(config.getRestartBreakKeys()), Timer.BREAK);
		individualResetKeys.put(KeyStroke.getKeyStroke(config.getRestartSegmentKeys()), Timer.SEGMENT);
		individualResetKeys.put(KeyStroke.getKeyStroke(config.getRestartBlueKeys()), Timer.BLUE);
		
		// Register the keys as global hotkeys.
		final Provider provider = Provider.getCurrentProvider(true);
		provider.register(resetAndStartKeyStroke, this);
		provider.register(stopKeyStroke, this);
		for (KeyStroke keyStroke : individualResetKeys.keySet())
		{
			provider.register(keyStroke, this);
		}
		
		// Set default properties for GUI window.
		this.setSize(150, 190);
		this.setTitle(WINDOW_TITLE);
		this.setResizable(false);
		this.setBounds(10, 800, this.getWidth(), this.getHeight());
		this.addWindowListener(new WindowAdapter() 
        {
        	@Override
			public void windowClosing(WindowEvent e) 
        	{
        		// Closing the window should terminate the program. Forget the global hotkeys and kill the JVM.
       			provider.reset();
       			provider.stop();
       			System.exit(0);
        	}
        });
		
		// Initialise GUI elements.
		background = new JPanel();
		background.setVisible(true);

		greenClock = new JLabel("Green circle:");
		greenClock.setBounds(10, 10, 150, 20);

		greenStrikeClock = new JLabel("Strikes in:");
		greenStrikeClock.setBounds(20, 40, 150, 20);

		breakClock = new JLabel("Break:");
		breakClock.setBounds(10, 70, 150, 20);

		segmentClock = new JLabel("Next segment:");
		segmentClock.setBounds(10, 100, 150, 20);

		blueClock = new JLabel("Blue circles:");
		blueClock.setBounds(10, 130, 150, 20);
		
		// Add GUI elements to the window.
		this.add(greenClock);
		this.add(greenStrikeClock);
		this.add(breakClock);
		this.add(segmentClock);
		this.add(blueClock);
		this.add(background);
		
		vgTimer = new ValeGuardianTimer();
	}
	
	/**
	 * Updates all running timers and updates the corresponding GUI labels accordingly.
	 */
	public void update()
	{
		if (!vgTimer.getIsRunning())
		{
			// The timer is not running; there is nothing to do.
			return;
		}
		
		// Update all running timers.
		vgTimer.update();
		
		// Update the GUI labels according to their timers.
		greenClock.setText("Green circle: " + formatLongAsTimeString(Timer.GREEN.read()));
		greenStrikeClock.setText("Strikes in: " + formatLongAsTimeString(Timer.GREEN.readDelay()));
		breakClock.setText("Break: " + formatLongAsTimeString(Timer.BREAK.read()));
		segmentClock.setText("Next segment: " + formatLongAsTimeString(Timer.SEGMENT.read()));
		blueClock.setText("Blue circles: " + formatLongAsTimeString(Timer.BLUE.read()));
	}

	/**
	 * Handles the pressing of global hotkeys.
	 */
	@Override
	public void onHotKey(HotKey hotKey) {
		if (hotKey.keyStroke.equals(resetAndStartKeyStroke))
		{
			System.out.println("Key pressed: reset all");
			vgTimer.resetAllAndStart();
		}
		if (hotKey.keyStroke.equals(stopKeyStroke))
		{
			System.out.println("Key pressed: stop");
			vgTimer.stop();
		}
		
		// Check if they pressed hotkey corresponds to one of the timers.
		Timer timerToReset = individualResetKeys.get(hotKey.keyStroke);
		if (timerToReset != null)
		{
			// The current hotkey is meant to reset a timer.
			System.out.println("Key pressed: reset timer " + timerToReset);
			timerToReset.reset();
		}
	}
	
	/**
	 * Formats a {@code long} value representing a time interval as a {@link String} for display in the GUI.
	 * @param time The time to be displayed.
	 * @return A human-readable {@link String} representing the time.
	 */
	public static String formatLongAsTimeString(long time)
	{
		if (time < 0)
		{
			// Timers are occasionally on a negative value, typically when they're waiting for a delayed strike. Show
			// these timers as 0 so their status is clear.
			time = 0;
		}
		String timeAsString = String.valueOf(time);
		while (timeAsString.length() <= 3)
		{
			// The timer is at less than 1000ms; pad the string with zeroes on the left to make it display nicely.
			timeAsString = "0" + timeAsString;
		}
		int timeAsStringLength = timeAsString.length();
		// Insert a dot as decimal separator to convert ms to s.
		return timeAsString.substring(0, timeAsStringLength - 3) + "."
				+ timeAsString.substring(timeAsStringLength - 3, timeAsStringLength - 2);
	}
}