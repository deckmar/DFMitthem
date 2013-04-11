package se.droidfactory.mitthem.communication.webscraper;

import java.io.IOException;

import se.droidfactory.mitthem.communication.LaundryDataFetchStatusListener;

public interface MitthemWebscraper {

	public abstract boolean login(String username, String password) throws IOException;

	public abstract String getUserFullname();

	public String getUserStreet();

	public String getUserPostnr();

	public String getUserCity();

	public String getUserPhoneHome();

	public String getUserPhoneWork();

	public String getUserMobile();

	public String getUserEmail();

	public abstract boolean fetchLaudryData(LaundryDataFetchStatusListener listener) throws IOException;
	
	public abstract boolean executeBookingAction(String url);

	public abstract String getState();
}
