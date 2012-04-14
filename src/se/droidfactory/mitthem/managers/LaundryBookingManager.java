package se.droidfactory.mitthem.managers;

import java.util.ArrayList;
import java.util.Date;

import se.droidfactory.mitthem.models.LaundrySlot;

public class LaundryBookingManager {

	private ArrayList<LaundrySlot> knownLaundrySlots;

	public LaundryBookingManager() {
		this.knownLaundrySlots = new ArrayList<LaundrySlot>();
	}

	public void addLaundrySlot(LaundrySlot slot) {
		this.knownLaundrySlots.add(slot);
	}

	public ArrayList<LaundrySlot> getAllKnownSlots() {
		return this.knownLaundrySlots;
	}

	public ArrayList<LaundrySlot> getAllSlotsBetweenDays(Date minDate, Date maxDate) {
		ArrayList<LaundrySlot> slots = new ArrayList<LaundrySlot>();
		for (LaundrySlot slot : this.knownLaundrySlots) {
			long laundTime = slot.getLaundryDate().getTime();
			if (laundTime >= minDate.getTime() && laundTime <= maxDate.getTime()) {
				slots.add(slot);
			}
		}
		return slots;
	}

	public ArrayList<LaundrySlot> getOnlyFreeSlots() {
		ArrayList<LaundrySlot> slots = new ArrayList<LaundrySlot>();
		for (LaundrySlot slot : this.knownLaundrySlots) {
			if (slot.getSlotState() == LaundrySlot.STATUS_SLOT_FREE) {
				slots.add(slot);
			}
		}
		return slots;
	}

	public ArrayList<LaundrySlot> getOnlyOthersSlots() {
		ArrayList<LaundrySlot> slots = new ArrayList<LaundrySlot>();
		for (LaundrySlot slot : this.knownLaundrySlots) {
			if (slot.getSlotState() == LaundrySlot.STATUS_SLOT_BOOKED_OTHER) {
				slots.add(slot);
			}
		}
		return slots;
	}

	public ArrayList<LaundrySlot> getOnlyMySlots() {
		ArrayList<LaundrySlot> slots = new ArrayList<LaundrySlot>();
		for (LaundrySlot slot : this.knownLaundrySlots) {
			if (slot.getSlotState() == LaundrySlot.STATUS_SLOT_BOOKED_ME) {
				slots.add(slot);
			}
		}
		return slots;
	}

	public void clearSlots() {
		this.knownLaundrySlots.clear();
	}

	/*
	 * Manager Singleton
	 */
	private static volatile LaundryBookingManager managerSingleton;

	public static LaundryBookingManager getInstance() {
		if (managerSingleton == null) {
			managerSingleton = new LaundryBookingManager();
		}
		return managerSingleton;
	}
}
