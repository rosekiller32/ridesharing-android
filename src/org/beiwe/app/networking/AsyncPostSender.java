package org.beiwe.app.networking;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.ui.AlertsManager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

public class AsyncPostSender extends AsyncTask<Void, Void, Void>{
	
	//TODO: Dori.  Document.
	
	// Private fields
	private String response;
	private String url;
	private Activity activity;
	private LoginSessionManager session;
	private View bar;
	
	/* ************************************************************************
	 * **************************** Constructor *******************************
	 * ************************************************************************/
	public AsyncPostSender(String url, Activity activity, LoginSessionManager session) {
		this.url = url;
		this.activity = activity;
		this.session = session;
	}
	
	/* ************************************************************************
	 * ************************* Convenience Functions ************************
	 * ********************************************************************** */
	public void setupProgressBar() {
		bar = (ProgressBar) activity.findViewById(R.id.progressBar);
	}
	
	/* ************************************************************************
	 * ************** Functions that deal with logging in *********************
	 * ********************************************************************** */
	
	@Override
	protected void onPreExecute() {
		setupProgressBar();
		bar.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		String parameters = NetworkUtilities.makeFirstTimeParameters();
		response = PostRequest.make_register_request( parameters, url );
		return null;
	} 
	
	@Override
	protected void onPostExecute(Void result) {
		bar.setVisibility(View.GONE);
		if (response.equals("200")) { 
			session.setRegistered(true);
			activity.startActivity(new Intent(activity.getApplicationContext(), DebugInterfaceActivity.class));
			activity.finish();
		} else { 				
			alertUser(activity);
		}
	}
	
	private void alertUser(final Activity activity) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				AlertsManager.showAlert(NetworkUtilities.handleServerResponseCodes(response), activity);
			}
		});
	}
}