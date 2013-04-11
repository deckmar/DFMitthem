package se.droidfactory.mitthem.gui;

import se.droidfactory.mitthem.R;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraper;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraperImpl;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MyInfo extends Activity {

	MitthemWebscraper mitthemWebScraper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_myinfo);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		this.mitthemWebScraper = MitthemWebscraperImpl.getInstance();

		((TextView) findViewById(R.id.dash_userinfo_name)).setText(this.mitthemWebScraper.getUserFullname());
		((TextView) findViewById(R.id.dash_userinfo_address)).setText(this.mitthemWebScraper.getUserStreet() + "\n"
				+ this.mitthemWebScraper.getUserPostnr() + " " + this.mitthemWebScraper.getUserCity());
		((TextView) findViewById(R.id.dash_userinfo_email)).setText(this.mitthemWebScraper.getUserEmail());
	}

	/*
	 * Navigating with the Action Bar
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.myinfo_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_info_edit:
			return true;
		case android.R.id.home:
			// app icon in action bar clicked; go home
			this.finish();
			return true;
		case R.id.menu_preferences:
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
