package se.droidfactory.mitthem.gui;

import java.io.IOException;

import se.droidfactory.mitthem.R;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraper;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraperImpl;
import se.droidfactory.mitthem.helpers.DateNameHelper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {

	private SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MitthemWebscraper mitthem = MitthemWebscraperImpl.getInstance();
		
		DateNameHelper.getInstance().initialize(this);

		/*
		 * If we are already logged in, then go to Dashboard Otherwise show the
		 * login Also, auto-login if possible and preferable
		 */

		if (mitthem.getState().equals(MitthemWebscraperImpl.STATE_LOGGED_IN)) {
			switchToDashboard();
		} else {
			setContentView(R.layout.screen_login);
			
			pref = Preferences.getAppPreferences(this);
			((EditText) findViewById(R.id.login_input_user)).setText(pref.getString("pref_user", ""));
			((EditText) findViewById(R.id.login_input_pin)).setText(pref.getString("pref_pass", ""));
			
			findViewById(R.id.login_btn_ok).setOnClickListener(this);

			if (pref.getBoolean("pref_autologin", false)) {
				this.onClick(findViewById(R.id.login_btn_ok));
			}
		}
	}

	/**
	 * Switch to the Dashboard screen without comming back here on back-button
	 * press.
	 */
	private void switchToDashboard() {
		Intent intent = new Intent(this, Dashboard.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		this.finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn_ok:

			String user = ((EditText) findViewById(R.id.login_input_user)).getText().toString();
			String pass = ((EditText) findViewById(R.id.login_input_pin)).getText().toString();
			
			pref.edit().putString("pref_user", user).putString("pref_pass", pass).apply();
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(findViewById(R.id.login_input_user).getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			imm.hideSoftInputFromWindow(findViewById(R.id.login_input_pin).getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			
			new LoginAsyncTask().execute(user, pass);
			break;
		}

	}

	public class LoginAsyncTask extends AsyncTask<String, Integer, Boolean> {

		private String username, password;
		private boolean loginSuccess;
		private MitthemWebscraper mitthemWebScraper;
		private ProgressDialog progressDlg;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			this.progressDlg = new ProgressDialog(Login.this, ProgressDialog.STYLE_SPINNER);
			this.progressDlg.setTitle("Loggar in");
			this.progressDlg.setMessage("Loggar in på Mina sidor");
			this.progressDlg.setCancelable(false);
			this.progressDlg.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			this.username = params[0];
			this.password = params[1];

			int tries = 3;
			this.mitthemWebScraper = MitthemWebscraperImpl.getInstance();

			while (tries-- > 0) {
				try {
					this.loginSuccess = mitthemWebScraper.login(this.username, this.password);
					break;
				} catch (IOException e) {
					// Try a few times more in case network is starting up
				}
			}

			return this.loginSuccess;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			this.progressDlg.dismiss();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			this.progressDlg.dismiss();

			if (result) {
				switchToDashboard();
			} else {
				Toast.makeText(Login.this, "Misslyckades med inloggningen", Toast.LENGTH_LONG).show();
			}
		}
	}
}
