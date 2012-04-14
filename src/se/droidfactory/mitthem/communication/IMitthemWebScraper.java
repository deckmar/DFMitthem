package se.droidfactory.mitthem.communication;

import java.io.IOException;

public interface IMitthemWebScraper {

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
