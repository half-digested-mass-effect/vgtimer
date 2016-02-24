package vgtimer;
public class ValeGuardianTimer
{
	/**
	 * Defines all available timers.
	 */
	public enum Timer
	{
		GREEN	(8,		7,	0),
		BREAK	(30,	0,	2),
		SEGMENT	(20,	0,	0),
		BLUE	(9,		0,	0);
		
		/**
		 * Time between the end of one iteration of this event and the start of the next. For example, for the green
		 * circle, this is the time between the lightning striking for one green circle and the next green circle
		 * appearing.
		 */
		private final int intervalInMs;
		/**
		 * Time between the start of the event and the actual activation. For example, for the break bar, this is the
		 * time between the break bar appearing and it disappearing (obviously an estimate, may need tweaking).
		 */
		private final int delayBeforeStrikeInMs;
		/**
		 * Time between the activation of the event and the end. For example, for the green circle, this is the time
		 * between a circle appearing and the lightning striking.
		 */
		private final int delayAfterStrikeInMs;
		/** Represents the time that this event will next activate, as measured based on the system time. */
		public long nextActivation;
		
		private Timer(int intervalInS, int delayBeforeStrikeInS, int delayAfterStrikeInS)
		{
			intervalInMs = intervalInS * 1000;
			delayBeforeStrikeInMs = delayBeforeStrikeInS * 1000;
			delayAfterStrikeInMs = delayAfterStrikeInS * 1000;
		}
		
		/**
		 * @return The remaining time, in ms, before the next start of this event.
		 */
		public long read()
		{
			return nextActivation - System.currentTimeMillis();
		}
		
		/**
		 * @return The remaining time, in ms, before the next actual activation of this event.
		 */
		public long readDelay()
		{
			return nextActivation + delayBeforeStrikeInMs - System.currentTimeMillis();
		}

		/**
		 * Resets the timer to the maximum time before the next occurence of this event. For the green circle, this is
		 * the moment of the previous circle appearing; for the break bar, this is the moment of the break bar
		 * disappearing.
		 */
		public void reset()
		{
			nextActivation = System.currentTimeMillis() - delayAfterStrikeInMs;
		}
		
		/**
		 * Checks whether this event has been activated since the previous update and updates the timer in this case.
		 */
		public void incrementIfNecessary()
		{
			if (System.currentTimeMillis() > nextActivation + delayBeforeStrikeInMs + delayAfterStrikeInMs)
			{
				nextActivation = nextActivation + intervalInMs + delayBeforeStrikeInMs + delayAfterStrikeInMs;
			}
		}
	}
	
	/** Indicates whether the timer is currently running. */
	private boolean isRunning;
	
	/**
	 * Resets all timers and starts them immediately.
	 */
	public void resetAllAndStart()
	{
		for (Timer timer : Timer.values())
		{
			timer.reset();
		}
		isRunning = true;
	}
	
	/**
	 * Stops all timers.
	 */
	public void stop()
	{
		isRunning = false;
	}
	
	public void update()
	{
		if (!isRunning)
		{
			return;
		}
		
		for (Timer timer : Timer.values())
		{
			timer.incrementIfNecessary();
		}
	}
	
	public boolean getIsRunning() {
		return isRunning;
	}
}
