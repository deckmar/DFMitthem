package se.droidfactory.mitthem.communication;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import se.droidfactory.mitthem.managers.LaundryBookingManager;
import se.droidfactory.mitthem.models.LaundrySlot;

public class MitthemWebScraperImpl implements IMitthemWebScraper {

	public static final String STATE_LOGGED_OUT = "STATE_LOGGED_OUT";
	public static final String STATE_LOGGED_IN = "STATE_LOGGED_IN";

	private String state = STATE_LOGGED_OUT;
	private Date lastLoginDateTime;
	private Map<String, String> cookiesMitthem;
	private Map<String, String> cookiesAptus;
	private volatile Document docMyPage;
	private volatile Document docLaudryPage;
	private LaundryBookingManager laundryBookingManager;
	private String userFullname, userStreet, userPostnr, userCity, userPhoneHome, userPhoneWork, userMobile, userEmail;

	public MitthemWebScraperImpl() {
		this.laundryBookingManager = LaundryBookingManager.getInstance();
	}

	@Override
	public boolean login(String username, String password) throws IOException {
		try {

			// Get the login-page
			Connection con = Jsoup.connect("http://www.mitthem.se/VFA/USER/MyPagesLogin.aspx");
			Response resp = con.method(Method.GET).execute();

			// Store cookies we get so far
			this.cookiesMitthem = resp.cookies();

			// Prepare to POST the log-in form
			con = Jsoup.connect("http://www.mitthem.se/VFA/USER/MyPagesLogin.aspx");
			con.referrer("http://www.mitthem.se/VFA/USER/MyPagesLogin.aspx");

			// Append our (session) cookies to the coming POST
			this.fillWithCookiesMitthem(con);

			// Get the HTML document of the login page
			Document doc = resp.parse();

			// Set username and password in the login-form
			doc.select("input[name*=ctl00$ctl01$DefaultSiteContentPlaceHolder1$Col2$LoginControl1$txtUserID]").val(
					username);
			doc.select("input[name*=ctl00$ctl01$DefaultSiteContentPlaceHolder1$Col2$LoginControl1$txtPassword]").val(
					password);

			// Add all key-value mappings from the
			// input-tags on the page to coming POST
			Elements inputs = doc.select("form input");
			for (Element el : inputs) {
				if (el.attr("name").contains("Search"))
					continue;
				con.data(el.attr("name"), el.val());
			}

			// Send the POST which include login details
			// Since we will get cookies while being redirected
			// we mustn't follow it automatically.
			con.method(Method.POST).followRedirects(false);
			resp = con.execute();

			// Get cookies and prepare to "follow".
			this.cookiesMitthem.putAll(resp.cookies());
			con = Jsoup.connect("http://www.mitthem.se/VFA/User/MyPages.aspx");
			con.referrer("http://www.mitthem.se/VFA/USER/MyPagesLogin.aspx");

			// Don't forget to all our current cookies
			this.fillWithCookiesMitthem(con);

			// Execute the "follow" to MyPages
			resp = con.method(Method.GET).execute();
			this.cookiesMitthem.putAll(resp.cookies());

			this.docMyPage = resp.parse();
			this.userFullname = this.docMyPage
					.select("#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfileName").text();
			if (this.userFullname.length() == 0) {
				return false;
			}
			this.userStreet = this.docMyPage.select(
					"#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfileAddress1").text();
			this.userPostnr = this.docMyPage.select(
					"#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfileAddress2").text();
			this.userCity = this.docMyPage
					.select("#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfileAddress3").text();
			this.userPhoneHome = this.docMyPage.select(
					"#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfilePhone1").text();
			this.userPhoneWork = this.docMyPage.select(
					"#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfilePhone2").text();
			this.userMobile = this.docMyPage
					.select("#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfilePhone3").text();
			this.userEmail = this.docMyPage.select("#ctl00_ctl01_DefaultSiteContentPlaceHolder1_Col2_lblProfileEmail")
					.text();

			this.setState(STATE_LOGGED_IN);
			this.lastLoginDateTime = new Date(System.currentTimeMillis());

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("failed");
			this.setState(STATE_LOGGED_OUT);
			throw e;
		}
	}

	@Override
	public String getUserFullname() {
		return userFullname;
	}

	public String getUserStreet() {
		return userStreet;
	}

	public String getUserPostnr() {
		return userPostnr;
	}

	public String getUserCity() {
		return userCity;
	}

	public String getUserPhoneHome() {
		return userPhoneHome;
	}

	public String getUserPhoneWork() {
		return userPhoneWork;
	}

	public String getUserMobile() {
		return userMobile;
	}

	public String getUserEmail() {
		return userEmail;
	}

	@Override
	public boolean fetchLaudryData(LaundryDataFetchStatusListener listener) throws IOException {
		if (this.docMyPage == null || this.getState().equals(STATE_LOGGED_OUT))
			return false;

		int totalSteps = 7;
		int step = 0;

		try {
			this.laundryBookingManager.clearSlots();

			// Get the first laundry page
			Connection con = Jsoup
					.connect("http://www.mitthem.se/res/themes/mitthem/pages/laundrybooking.aspx?cmguid=6ff4ac56-c82b-4fa5-ad48-8e7dd6fe142d");
			this.fillWithCookiesMitthem(con);
			Document doc = con.get();

			if (listener != null)
				listener.laundryStatusUpdate(++step, totalSteps);

			String nextUrl = "http://www.mitthem.se/res/themes/mitthem/pages/"
					+ doc.select("a[href*=laundrybooking.aspx?user]").first().attr("href");
			con = Jsoup.connect(nextUrl);
			this.fillWithCookiesMitthem(con);
			doc = con.get();

			// Get laundry page (will get redirected - must handle cookies)
			con = Jsoup.connect("http://www.mitthem.se/HSR/Applications/Aptus/ext_gw.aspx?module=wwwash");
			this.fillWithCookiesMitthem(con);
			con.followRedirects(false);
			Response resp = con.method(Method.GET).execute();
			nextUrl = resp.header("Location");

			con = Jsoup.connect(nextUrl).followRedirects(false);
			resp = con.method(Method.GET).execute();
			this.cookiesAptus = resp.cookies();
			nextUrl = "http://83.166.0.145" + resp.header("Location");

			// 1) Have link for start
			con = Jsoup.connect(nextUrl).followRedirects(true);
			this.fillWithCookiesAptus(con);
			// 2) Fetch doc
			this.docLaudryPage = con.get();
			
			if (listener != null)
				listener.laundryStatusUpdate(++step, totalSteps);

			// 3) Parse doc for laundry
			this.parseAllLaundrySlots(docLaudryPage);

			String laundBaseUrl = "";
			ArrayList<String> laundTypeUrls = new ArrayList<String>();
			ArrayList<String> laundFullUrls = new ArrayList<String>();

			// Order: panelId / type / group

			// 4) Parse doc for type-links
			Elements typeElems = this.docLaudryPage.select(".headerColor[align*=left][style*=cursor:hand]");
			for (Element ele : typeElems) {
				String mousedown = ele.attr("onmousedown");
				if (mousedown.length() == 0)
					continue;
				String path = mousedown.substring(mousedown.indexOf("href='") + 6, mousedown.length() - 1);
				laundBaseUrl = mousedown.substring(mousedown.indexOf("href='") + 6, mousedown.indexOf("&weekoffset"));
				laundBaseUrl = "http://83.166.0.145/aptusportal/" + laundBaseUrl;
				laundTypeUrls.add("http://83.166.0.145/aptusportal/" + path);
			}

			// 5) Parse doc for group-links
			laundFullUrls.addAll(this.parseAllGroupLinks("http://83.166.0.145/aptusportal/", docLaudryPage));

			// 7) For each type-link do step 3, 5
			for (String url : laundTypeUrls) {
				con = Jsoup.connect(url);
				this.fillWithCookiesAptus(con);
				this.docLaudryPage = con.get();

				this.parseAllLaundrySlots(docLaudryPage);
				laundFullUrls.addAll(this.parseAllGroupLinks("http://83.166.0.145/aptusportal/", docLaudryPage));
				
				if (listener != null)
					listener.laundryStatusUpdate(++step, totalSteps);
			}

			// 8) Fetch all the rest
			for (String fullUrl : laundFullUrls) {
				// System.out.println(fullUrl);
				con = Jsoup.connect(fullUrl).followRedirects(true);
				this.fillWithCookiesAptus(con);
				Document docTmp = con.get();
				this.parseAllLaundrySlots(docTmp);
				
				if (listener != null)
					listener.laundryStatusUpdate(++step, totalSteps);

//				String nextWeekUrl = fullUrl.replace("weekoffset=0", "weekoffset=1");
//
//				con = Jsoup.connect(nextWeekUrl).followRedirects(true);
//				this.fillWithCookiesAptus(con);
//				docTmp = con.get();
//				this.parseAllLaundrySlots(docTmp);
//				
//				if (listener != null)
//					listener.laundryStatusUpdate(++step, totalSteps);
			}

			// System.out.println(this.docLaudryPage.outerHtml());

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ArrayList<String> parseAllGroupLinks(String baseUrl, Document doc) {
		ArrayList<String> ret = new ArrayList<String>();
		Elements laundRoomGroups = doc.select(".bgInactiveColor[background*=images/Flik/bg_inactive.gif]");
		for (Element el : laundRoomGroups) {
			String mousedown = el.attr("onmousedown");
			String path = mousedown.substring(mousedown.indexOf("href='") + 6, mousedown.length() - 1);
			ret.add(baseUrl + path);
		}
		return ret;
	}

	private void parseAllLaundrySlots(Document doc) {
		Elements freeTimes = doc.select("img[src*=icon_plus]");
		Elements myTimes = doc.select("img[src*=icon_own]");
		int laundryRoom = Integer.parseInt(doc.select(".textActiveColor[background*=images/Flik/bg_active2.gif] b")
				.html());

		for (Element free : freeTimes) {
			free = free.parent();
			String onclick = free.attr("onmousedown");
			String[] splits = onclick.split("&");
			String date = splits[4].substring(5);
			String interval = splits[5].substring(11);
			interval = interval.substring(0, interval.length() - 1);
			String actionUrl = onclick.substring(onclick.indexOf("location.href='") + 15, onclick.length() - 1);
			actionUrl = "http://83.166.0.145/aptusportal/" + actionUrl;

			LaundrySlot slot = new LaundrySlot(laundryRoom, Date.valueOf(date), Integer.parseInt(interval),
					LaundrySlot.STATUS_SLOT_FREE, actionUrl);
			this.laundryBookingManager.addLaundrySlot(slot);

			System.out.println("Free spot on " + date + " interval " + interval);
		}

		for (Element mine : myTimes) {
			mine = mine.parent();
			String onclick = mine.attr("onmousedown");
			String[] splits = onclick.split("&");
			String date = splits[4].substring(5);
			String interval = splits[5].substring(11);
			interval = interval.substring(0, interval.length() - 1);
			String actionUrl = onclick.substring(onclick.indexOf("location.href='") + 15, onclick.length() - 1);
			actionUrl = "http://83.166.0.145/aptusportal/" + actionUrl;

			LaundrySlot slot = new LaundrySlot(laundryRoom, Date.valueOf(date), Integer.parseInt(interval),
					LaundrySlot.STATUS_SLOT_BOOKED_ME, actionUrl);
			this.laundryBookingManager.addLaundrySlot(slot);

			System.out.println("Free spot on " + date + " interval " + interval);
		}
	}

	public boolean executeBookingAction(String url) {
		if (this.docLaudryPage == null || this.getState().equals(STATE_LOGGED_OUT))
			return false;

		url = url.replace("wwwashwait", "wwwashcommand");

		try {
			Connection con = Jsoup.connect(url);
			this.fillWithCookiesAptus(con);
			Document doc = con.get();

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void fillWithCookiesMitthem(Connection con) {
		for (String key : this.cookiesMitthem.keySet()) {
			con.cookie(key, this.cookiesMitthem.get(key));
		}
	}

	private void fillWithCookiesAptus(Connection con) {
		for (String key : this.cookiesAptus.keySet()) {
			con.cookie(key, this.cookiesAptus.get(key));
		}
	}

	private void setState(String state) {
		this.state = state;
	}

	@Override
	public String getState() {
		/*
		 * If the last login was longer than 5 minutes ago, then say we are
		 * logged out (from inactivity).
		 */
		if (this.lastLoginDateTime != null
				&& this.lastLoginDateTime.before(new Date(System.currentTimeMillis() - 1000 * 60 * 5))) {
			setState(STATE_LOGGED_OUT);
		}
		return state;
	}

	/*
	 * Singleton pattern
	 */
	private static volatile IMitthemWebScraper singletonInstance;

	public static IMitthemWebScraper getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new MitthemWebScraperImpl();
		}
		return singletonInstance;
	}
}
