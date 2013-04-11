package se.droidfactory.mitthem.gui;

import java.io.IOException;

import se.droidfactory.mitthem.R;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraper;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraperImpl;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class Dashboard extends Activity implements OnClickListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_dashboard);

		this.findViewById(R.id.dash_btn_appartments).setOnClickListener(this);
		this.findViewById(R.id.dash_btn_laundry).setOnClickListener(this);
		this.findViewById(R.id.dash_btn_info).setOnClickListener(this);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View button) {
		if (button.getId() == R.id.dash_btn_laundry) {
			Intent intent = new Intent(this, Laundry.class);
			startActivity(intent);
		}

		if (button.getId() == R.id.dash_btn_appartments) {
			new AlertDialog.Builder(this).setTitle("Not implemented")
					.setMessage("This functionality is not yet implemented.")
					.setIcon(android.R.drawable.ic_dialog_info).create().show();
		}

		if (button.getId() == R.id.dash_btn_info) {
			Intent intent = new Intent(this, MyInfo.class);
			startActivity(intent);
		}
	}

	/*
	 * Navigating with the Action Bar
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dashboard_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}