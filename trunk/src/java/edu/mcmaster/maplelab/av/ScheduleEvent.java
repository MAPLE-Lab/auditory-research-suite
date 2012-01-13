/**
 * 
 */
package edu.mcmaster.maplelab.av;

import java.util.concurrent.TimeUnit;

/**
 * @author bguseman
 *
 */
public class ScheduleEvent {
	private final long _eventTime;
	private final long _relativeTime;
	
	public ScheduleEvent(long eventTime, long relativeTime) {
		_eventTime = eventTime;
		_relativeTime = relativeTime;
	}
	
	/**
	 * Get the system time at which this event was created in the given units.
	 */
	public long getEventTime(TimeUnit unit) {
		return unit.convert(_eventTime, TimeUnit.NANOSECONDS);
	}
	
	/**
	 * Get the relative time since the scheduler start time in the given units.
	 */
	public long getRelativeTime(TimeUnit unit) {
		return unit.convert(_relativeTime, TimeUnit.NANOSECONDS);
	}
}
