package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.survey.JsonParser;
import org.beiwe.app.survey.QuestionsDownloader;
import org.beiwe.app.survey.SurveyAnswersRecorder;
import org.beiwe.app.survey.SurveyTimingsRecorder;
import org.beiwe.app.survey.SurveyType;
import org.beiwe.app.survey.SurveyType.Type;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SurveyActivity extends SessionActivity {
	
	private LinearLayout surveyLayout;
	private SurveyAnswersRecorder answersRecorder;
	private String surveyId;
	private SurveyType.Type surveyType;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
				
		QuestionsDownloader downloader = new QuestionsDownloader(getApplicationContext());

		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String surveyTypeKey = extras.getString("SurveyType");
				if (surveyTypeKey.equals(SurveyType.Type.DAILY.dictKey)) {
					surveyType = Type.DAILY;
					renderSurvey(downloader.getJsonSurveyString(surveyType));
				}
				else if (surveyTypeKey.equals(SurveyType.Type.WEEKLY.dictKey)) {
					surveyType = Type.WEEKLY;
					renderSurvey(downloader.getJsonSurveyString(surveyType));
				}
			}
		}
	}
	
	
	/**
	 * Display, in the survey's spot in the Activity/page, the survey itself
	 * @param jsonSurveyString the JSON file, as a string, used to render the survey questions
	 */
	private void renderSurvey(String jsonSurveyString) {
		// Get the survey layout objects that we'll add questions to
		surveyLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.survey_layout, null);
		
		// Parse the JSON list of questions and render them as Views
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		surveyId = jsonParser.renderSurveyFromJSON(surveyLayout, jsonSurveyString);

		// Display the survey instead of the "Loading..." wheel
		replaceSurveyPageContents(surveyLayout);
		
		// Record the time that the survey was first visible to the user
		SurveyTimingsRecorder.recordSurveyFirstDisplayed(surveyId);
	}
	
	
	/**
	 * Delete/empty the contents of the Survey Activity/page, and replace
	 * them with the View provided here. This is called because adding multiple
	 * children to a ScrollView crashes the app; also, we want to show only the
	 * loading wheel, or the survey, but not both at once.
	 * @param newContents the View to display in the survey Activity/page
	 */
	private synchronized void replaceSurveyPageContents(View newContents) {
		ViewGroup page = (ViewGroup) findViewById(R.id.scrollViewMain);
		page.removeAllViews();
		page.addView(newContents);
	}
	
	
	/**
	 * Called when the user presses "Submit" at the bottom of the survey,
	 * saves the answers, and takes the user back to the main page.
	 * @param v
	 */
	public void submitButtonPressed(View v) {
		AppNotifications.dismissNotificatoin(getApplicationContext(), surveyType.notificationCode);

		SurveyTimingsRecorder.recordSubmit(getApplicationContext());
		
		answersRecorder = new SurveyAnswersRecorder();
		String unansweredQuestions = answersRecorder.gatherAllAnswers(surveyLayout, getApplicationContext());
		if (unansweredQuestions.length() > 0) {
			// If there are unanswered questions, show a warning
			showUnansweredQuestionsWarning(unansweredQuestions);
		}
		else {
			// If there are no unanswered questions, record the answers and close
			recordAnswersAndClose();
		}
	}
	
	
	/**
	 * Show a warning pop-up saying "some questions aren't answered; do you 
	 * want to go back, or submit anyway?"
	 * @param unansweredQuestions a String of unanswered questions
	 */
	private void showUnansweredQuestionsWarning(String unansweredQuestions) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SurveyActivity.this);
		alertBuilder.setTitle("Unanswered Questions");
		alertBuilder.setMessage("You did not answer the following questions: " + unansweredQuestions + ". Do you want to submit the survey anyways?");
		alertBuilder.setPositiveButton("Submit anyway", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// If the user clicks "Submit anyway", record the answers and close
				recordAnswersAndClose();
			}
		});
		alertBuilder.setNegativeButton("Go back to survey", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// If the user clicked "Go back to survey", then close the pop-up and do nothing.
			}
		});
		alertBuilder.create().show();
	}
	
	
	/**
	 * Write the Survey answers to a new SurveyAnswers.csv file, and show a
	 * Toast reporting either success or failure
	 */
	private void recordAnswersAndClose() {
		int messageId = 0;

		// Write the data to a SurveyAnswers file
		if (answersRecorder.writeLinesToFile(surveyId)) {
			messageId = R.string.survey_submit_success_message;
		}
		else {
			messageId = R.string.survey_submit_error_message;
		}
		
		// Show a Toast telling the user either "Thanks, success!" or "Oops, there was an error"
		String msg = getApplicationContext().getResources().getString(messageId);
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

		// Close the Activity
		finish();
	}
		
}