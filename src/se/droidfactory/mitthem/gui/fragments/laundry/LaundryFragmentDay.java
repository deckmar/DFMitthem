package se.droidfactory.mitthem.gui.fragments.laundry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import se.droidfactory.mitthem.R;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraper;
import se.droidfactory.mitthem.communication.webscraper.MitthemWebscraperImpl;
import se.droidfactory.mitthem.gui.Laundry;
import se.droidfactory.mitthem.helpers.DateNameHelper;
import se.droidfactory.mitthem.managers.LaundryBookingManager;
import se.droidfactory.mitthem.models.LaundrySlot;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LaundryFragmentDay extends Fragment implements OnClickListener {

	private Calendar thisDayDate;
	private DateNameHelper dateHelp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_laundry, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int day_offset = getArguments().getInt("day_offset", 0);
		thisDayDate = Calendar.getInstance();
		thisDayDate.add(Calendar.DATE, day_offset);

		dateHelp = DateNameHelper.getInstance();

		LayoutInflater inflater = getActivity().getLayoutInflater();

		String topHeaderStr = dateHelp.weekDayNames.get(thisDayDate.get(Calendar.DAY_OF_WEEK)) + " ";
		topHeaderStr += thisDayDate.get(Calendar.DAY_OF_MONTH) + " ";
		topHeaderStr += dateHelp.monthNames.get(thisDayDate.get(Calendar.MONTH));
		((TextView) getActivity().findViewById(R.id.laund_top_header)).setText(topHeaderStr);

		LaundryBookingManager manager = LaundryBookingManager.getInstance();
		Calendar minDate = (Calendar) thisDayDate.clone();
		minDate.set(Calendar.HOUR_OF_DAY, 0);
		minDate.clear(Calendar.MINUTE);
		minDate.clear(Calendar.SECOND);
		minDate.clear(Calendar.MILLISECOND);
		Calendar maxDate = (Calendar) minDate.clone();
		maxDate.add(Calendar.DAY_OF_YEAR, 1);
		maxDate.add(Calendar.SECOND, -1);
		ArrayList<LaundrySlot> slots = manager.getAllSlotsBetweenDays(minDate.getTime(), maxDate.getTime());

		Collections.sort(slots, new Comparator<LaundrySlot>() {
			@Override
			public int compare(LaundrySlot lhs, LaundrySlot rhs) {
				if (lhs.getLaundryTimeInterval() < rhs.getLaundryTimeInterval())
					return -1;
				if (lhs.getLaundryTimeInterval() > rhs.getLaundryTimeInterval())
					return 1;
				if (lhs.getLaundryRoom() < rhs.getLaundryRoom())
					return -1;
				if (lhs.getLaundryRoom() > rhs.getLaundryRoom())
					return 1;
				return 0;

			}
		});

		int lastTimeInterval = -1;
		LinearLayout slotlist = (LinearLayout) getActivity().findViewById(R.id.laund_slotlist);

		for (int position = 0; position < slots.size(); position++) {

			LaundrySlot slot = slots.get(position);

			/**
			 * When appropriate, inflate and insert a section header saying the
			 * laundry time interval
			 */

			if (lastTimeInterval == -1 || slot.getLaundryTimeInterval() != lastTimeInterval) {
				lastTimeInterval = slot.getLaundryTimeInterval();

				View listSectionHeader = inflater.inflate(R.layout.listitem_laundry_sectionheader, null);
				String sectionHeader = LaundrySlot.getTimeIntervalString(slot.getLaundryTimeInterval());
				((TextView) listSectionHeader.findViewById(R.id.laund_text_sectionheader)).setText(sectionHeader);
				slotlist.addView(listSectionHeader);
			}

			/**
			 * Inflate and insert a view for the laundry slot
			 */

			View listItem = inflater.inflate(R.layout.listitem_laundry_slot, null);

			ImageView iw_state = (ImageView) listItem.findViewById(R.id.laund_state_img);
			switch (slot.getSlotState()) {
			case LaundrySlot.STATUS_SLOT_FREE:
				iw_state.setImageResource(R.drawable.ic_laund_free);
				break;

			case LaundrySlot.STATUS_SLOT_BOOKED_ME:
				iw_state.setImageResource(R.drawable.ic_laund_booked_me);
				break;
			}
			iw_state.setOnClickListener(this);

			TextView tw_name = (TextView) listItem.findViewById(R.id.laund_text_str);
			tw_name.setText("Rum " + slot.getLaundryRoom());

			listItem.setTag(slot);
			iw_state.setTag(slot);
			slotlist.addView(listItem);
		}
	}

    @Override
    public void onClick(View view) {

        // Only handle clicks on the state image for now
        if (view.getId() != R.id.laund_state_img) return;

        if (isLoggedIn()) {
            LaundrySlot slot = (LaundrySlot) view.getTag();
            handleBookingClick(slot);
        } else {
            Toast.makeText(getActivity(), "Not logged in. (Debug) Restart app for now.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isLoggedIn() {
        return MitthemWebscraperImpl.getInstance().getState()
                .equals(MitthemWebscraperImpl.STATE_LOGGED_IN);
    }

    private void handleBookingClick(LaundrySlot slot) {

        switch (slot.getSlotState()) {
            case LaundrySlot.STATUS_SLOT_FREE:
                showBookingConfirmDialog(slot);
                break;
            case LaundrySlot.STATUS_SLOT_BOOKED_ME:
                showBookingCancelDialog(slot);
                break;
        }
    }

    private void showBookingConfirmDialog(final LaundrySlot slot) {

        getGenericAlertDialogBuilder()
                .setTitle("Bekr�fta bokning")
                .setMessage("Vill du boka denna tv�ttid?\n\n" +  getSlotTimeInfo(slot))
                .setPositiveButton("Boka", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ActionUrlAsyncTask("Bokar tv�ttid..", "V�nta medan tv�ttiden bokas.").execute(slot);
                    }
                }).show();
    }

    private void showBookingCancelDialog(final LaundrySlot slot) {

        getGenericAlertDialogBuilder()
                .setTitle("Bekr�fta avbokning")
                .setMessage("Vill du avboka denna tv�ttid?\n\n" + getSlotTimeInfo(slot))
                .setPositiveButton("Avboka", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ActionUrlAsyncTask("Avbokar tv�ttid..", "V�nta medan tv�ttiden avbokas.").execute(slot);
                    }
                }).show();
    }

    private AlertDialog.Builder getGenericAlertDialogBuilder() {

        return new AlertDialog.Builder(getActivity())
                .setNegativeButton("Avbryt", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled
                    }
                });
    }

    private String getSlotTimeInfo(LaundrySlot slot) {
        Date date = slot.getLaundryDate();

        return String.format("Datum: %s %d %s\nTid: %s\nRum: %d",
                dateHelp.weekDayNames.get(date.getDay()),
                date.getDate(),
                dateHelp.monthNames.get(date.getMonth()),
                LaundrySlot.getTimeIntervalString(slot.getLaundryTimeInterval()),
                slot.getLaundryRoom());
    }

	private class ActionUrlAsyncTask extends AsyncTask<LaundrySlot, Integer, Boolean> {

		ProgressDialog dlg;

		public ActionUrlAsyncTask(String title, String msg) {
			this.dlg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
			this.dlg.setTitle(title);
			this.dlg.setMessage(msg);
			this.dlg.setCancelable(false);
			this.dlg.setIndeterminate(true);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			this.dlg.show();
		}

		@Override
		protected Boolean doInBackground(LaundrySlot... params) {
			MitthemWebscraper mitt = MitthemWebscraperImpl.getInstance();
			LaundrySlot slot = params[0];

			boolean result = mitt.executeBookingAction(slot.getActionUrl());
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			this.dlg.dismiss();

			if (!result) {
				new AlertDialog.Builder(getActivity())
						.setTitle("Misslyckades")
						.setMessage(
								"Ett fel intr�ffade med bokningen. Prova senare. Det kan hj�lpa att starta om appen.")
						.create().show();
			} else {
				((Laundry) getActivity()).refreshLaundryData();
			}
		}

	}
}
