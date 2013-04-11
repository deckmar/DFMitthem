package se.droidfactory.mitthem.gui;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.droidfactory.mitthem.R;
import se.droidfactory.mitthem.communication.LaundryDataFetchStatusListener;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraper;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraperImpl;
import se.droidfactory.mitthem.gui.fragments.laundry.LaundryFragmentDay;
import se.droidfactory.mitthem.helpers.DateNameHelper;
import se.droidfactory.mitthem.managers.LaundryBookingManager;
import se.droidfactory.mitthem.models.LaundrySlot;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Laundry extends Activity {

	private ActionBar bar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_laundry);

		bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);

		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		refreshLaundryData();
	}
	
	public void refreshLaundryData() {
		new LaundryFetchAsyncTask().execute();
	}

	private void refreshTabs() {

		bar.removeAllTabs();

		DateNameHelper dateHelp = DateNameHelper.getInstance();
		ActionBar.Tab tabToday = bar.newTab().setText("Idag").setTag(0);
		tabToday.setTabListener(new MyTabsListener<LaundryFragmentDay>(this, "", LaundryFragmentDay.class));
		bar.addTab(tabToday);

		int daysToShow = 5;
		for (int i = 1; i < daysToShow; i++) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, i);
			ActionBar.Tab tabFuture = bar.newTab().setText(dateHelp.weekDayNames.get(c.get(Calendar.DAY_OF_WEEK))).setTag(i);
			tabFuture.setTabListener(new MyTabsListener<LaundryFragmentDay>(this, "", LaundryFragmentDay.class));
			bar.addTab(tabFuture);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * Navigating with the Action Bar
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.laundry_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_laund_refresh:
			new LaundryFetchAsyncTask().execute();
			return true;
		case android.R.id.home: {
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, Login.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		case R.id.menu_preferences: {
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateLaundryListFromDb() {
		// LaundryBookingManager mgr = LaundryBookingManager.getInstance();
		// List<LaundrySlot> slots = mgr.getAllKnownSlots();
		// ListAdapter adapter = new LaundryArrayAdapter(Laundry.this,
		// R.layout.listitem_laundry_slot, slots);

		// LaundryBooking.this.setListAdapter(adapter);
	}

	/***
	 * Generic boilerplate fragments-in-tabs from
	 * http://developer.android.com/guide/topics/ui/actionbar.html
	 * 
	 * @author JohnDoe
	 * 
	 * @param <T>
	 */
	public class MyTabsListener<T extends Fragment> implements ActionBar.TabListener {

		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public MyTabsListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// Ain't do nuthin
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Check if the fragment is already initialized
			if (mFragment == null) {
				// If not, instantiate and add it to the activity
				Bundle fragData = new Bundle();
				fragData.putInt("day_offset", (Integer) tab.getTag());
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), fragData);
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.detach(mFragment);
			}
		}
	}

	public class LaundryArrayAdapter extends ArrayAdapter<LaundrySlot> {

		List<LaundrySlot> slots;
		LaundryBookingManager mgr;

		public LaundryArrayAdapter(Context context, int textViewResourceId, List<LaundrySlot> objects) {
			super(context, textViewResourceId, objects);

			this.slots = objects;
			this.mgr = LaundryBookingManager.getInstance();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = View.inflate(Laundry.this, R.layout.listitem_laundry_slot, null);
			}

			LaundrySlot slot = this.slots.get(position);
			if (slot.getSlotState() == LaundrySlot.STATUS_SLOT_FREE) {
				((ImageView) convertView.findViewById(R.id.laund_state_img)).setImageResource(R.drawable.ic_laund_free);
			} else if (slot.getSlotState() == LaundrySlot.STATUS_SLOT_BOOKED_OTHER) {
				((ImageView) convertView.findViewById(R.id.laund_state_img))
						.setImageResource(R.drawable.ic_laund_booked_other);
			} else if (slot.getSlotState() == LaundrySlot.STATUS_SLOT_BOOKED_ME) {
				((ImageView) convertView.findViewById(R.id.laund_state_img))
						.setImageResource(R.drawable.ic_laund_booked_me);
			}

			String slotStr = "";
			slotStr += LaundrySlot.getTimeIntervalString(slot.getLaundryTimeInterval());
			slotStr += ", rum " + slot.getLaundryRoom();
			((TextView) convertView.findViewById(R.id.laund_text_str)).setText(slotStr);

			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			return R.layout.listitem_laundry_slot;
		}
	}

	private class LaundryFetchAsyncTask extends AsyncTask<String, Integer, Boolean> implements LaundryDataFetchStatusListener {

		ProgressDialog progressDlg;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			this.progressDlg = new ProgressDialog(Laundry.this, ProgressDialog.THEME_HOLO_LIGHT);
			this.progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.progressDlg.setTitle("Hämtar tvättider");
			this.progressDlg.setMessage("Vänligen vänta medan lediga tvättider hämtas");
			this.progressDlg.setCancelable(false);
			this.progressDlg.setIndeterminate(true);
			this.progressDlg.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			MitthemWebscraper scraper = MitthemWebscraperImpl.getInstance();
			boolean fetchSuccess = false;

			int tries = 3;
			while (tries-- > 0) {
				try {
					fetchSuccess = scraper.fetchLaudryData(this);
					break;
				} catch (IOException e) {
					// Try a few times more in case network is starting up
				}
			}

			return fetchSuccess;
		}
		
		@Override
		public void laundryStatusUpdate(int step, int totalSteps) {
			this.publishProgress(step, totalSteps);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
			this.progressDlg.setProgress(values[0]);
			this.progressDlg.setMax(values[1]);
			this.progressDlg.setIndeterminate(false);
		}

		@Override
		protected void onCancelled(Boolean result) {
			super.onCancelled(result);
			this.progressDlg.dismiss();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (result) {
				updateLaundryListFromDb();
				refreshTabs();
			} else {
				Toast.makeText(Laundry.this, "Misslyckades med uppdateringen", Toast.LENGTH_LONG).show();
			}
			this.progressDlg.dismiss();

		}
	}
}
