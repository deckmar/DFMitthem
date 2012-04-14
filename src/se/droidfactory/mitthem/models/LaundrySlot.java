package se.droidfactory.mitthem.models;

import java.util.Date;

/**
 * LaundrySlot describes date and time of a laundry slot as well as whether this
 * slot is free, booked by other or booked by user.
 * 
 * @author Johan Deckmar
 * 
 */
public class LaundrySlot {
	
	public static final int STATUS_SLOT_FREE = 1;
	public static final int STATUS_SLOT_BOOKED_OTHER = 2;
	public static final int STATUS_SLOT_BOOKED_ME = 3;

	// Room-number
	private int laundryRoom;

	// Date and time-interval of this laundry-slot
	private Date laundryDate;
	private int laundrySlotIndex;
	
	// Booking status
	private int slotState;
	
	// Action URL (to book or unbook)
	String actionUrl;

	public LaundrySlot(int laundryRoom, Date laundryDate, int laundrySlotIndex, int slotState, String actionUrl) {
		this.laundryRoom = laundryRoom;
		this.laundryDate = laundryDate;
		this.laundrySlotIndex = laundrySlotIndex;
		this.slotState = slotState;
		this.actionUrl = actionUrl;
	}
	
	public int getHourOfDayStart() {
		return LaundrySlot.getTimeSlotHour(this.laundrySlotIndex);
	}

	/*
	 * Getters and setters
	 */

	public int getLaundryRoom() {
		return laundryRoom;
	}

	public Date getLaundryDate() {
		return laundryDate;
	}

	public int getLaundryTimeInterval() {
		return laundrySlotIndex;
	}

	public int getSlotState() {
		return slotState;
	}

	public void setSlotState(int slotState) {
		this.slotState = slotState;
	}
	
	public String getActionUrl() {
		return actionUrl;
	}

	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}


	/*
	 * Helpers for conversion between daily slot-index and actual hour of day
	 */
	public static final int[] slotIntervalStartTimes = { 5, 8, 11, 14, 17, 20, 23, 2 };
	public static final String[] slotIntervalStrings = { "5:00 - 8:00", "8:00 - 11:00", "11:00 - 14:00",
			"14:00 - 17:00", "17:00 - 20:00", "20:00 - 23:00", "23:00 - 02:00" };

	/**
	 * The hour of the day at which a specific slot-index starts
	 * 
	 * @param slotIndex
	 *            Slot-index in question
	 * @return Hour of the day
	 */
	public static int getTimeSlotHour(int slotIndex) {
		return slotIntervalStartTimes[slotIndex];
	}

	/**
	 * String representation of laundry interval start-and-stop time
	 * 
	 * @param slotIndex
	 *            Daily slot-index
	 * @return Start and stop time as text
	 */
	public static String getTimeIntervalString(int slotIndex) {
		return slotIntervalStrings[slotIndex];
	}
}
