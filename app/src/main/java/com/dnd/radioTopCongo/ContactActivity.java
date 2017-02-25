package com.dnd.radioTopCongo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.dnd.radioTopCongo.helpers.PermissionHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ContactActivity extends Activity {

    private static final String TAG = ContactActivity.class.getSimpleName();

	private ContactActivity activity = this;
	private FrameLayout wrapperView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_activity);

		// GET UI ELEMENTS
        wrapperView = (FrameLayout) findViewById(R.id.wrapperView);

        initCloseButton();
        initContactButtons();

        Button privacyPolicyButton = (Button) findViewById(R.id.privacy_policy_button);
        if (privacyPolicyButton != null) {
            privacyPolicyButton.setPaintFlags(privacyPolicyButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            privacyPolicyButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContactActivity.this, PrivatePolicyActivity.class);
                    startActivity(intent);
                }
            });
        }
	}

	@Override
    protected void onStart() {
        super.onStart();

		TopCongoApplication application = (TopCongoApplication) getApplication();
		Tracker tracker = application.getDefaultTracker();

        if (tracker != null) {
            tracker.setScreenName("ContactView");
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ContactActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

	private void initCloseButton() {

		ImageButton closeButton = (ImageButton) wrapperView.findViewById(R.id.closeButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
                ContactActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
			}
		});

	}

    public static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 100;
    private String phoneNumberToCall;
    public void setPhoneNumberToCall(String phoneNumberToCall) {
        this.phoneNumberToCall = phoneNumberToCall;
    }

    private void initContactButtons() {

		// PHONE

		Button contactEditorialBoardButton = (Button) wrapperView.findViewById(R.id.contactEditorialBoardButton);
		contactEditorialBoardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243818120812");
                startPhoneCall();
			}
		});

		Button contactCommercialTeamButton = (Button) wrapperView.findViewById(R.id.contactCommercialTeamButton);
		contactCommercialTeamButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243841001000");
                startPhoneCall();
			}
		});

		Button contactSpeakAboutProgramButton = (Button) wrapperView.findViewById(R.id.contactSpeakAboutProgramButton);
		contactSpeakAboutProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243813029007");
                startPhoneCall();
			}
		});

		Button contactHappyBirthdayProgramButton = (Button) wrapperView.findViewById(R.id.contactHappyBirthdayProgramButton);
		contactHappyBirthdayProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243998106077");
                startPhoneCall();
			}
		});

		Button contactListenersMagazineProgramButton = (Button) wrapperView.findViewById(R.id.contactListenersMagazineProgramButton);
		contactListenersMagazineProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243841001000");
                startPhoneCall();
			}
		});

		Button contactTopDedicationProgramButton = (Button) wrapperView.findViewById(R.id.contactTopDedicationProgramButton);
		contactTopDedicationProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243999900196");
                startPhoneCall();
			}
		});

		Button contactTopPressProgramButton = (Button) wrapperView.findViewById(R.id.contactTopPressProgramButton);
		contactTopPressProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243841001000");
                startPhoneCall();
			}
		});

		Button contactTopDisaporaProgramButton = (Button) wrapperView.findViewById(R.id.contactTopDisaporaProgramButton);
		contactTopDisaporaProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                setPhoneNumberToCall("+243841001000");
                startPhoneCall();
			}
		});


		// EMAIL

		Button contactInformationButton = (Button) wrapperView.findViewById(R.id.contactInformationButton);
		contactInformationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setType("text/plain");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"thierka@hotmail.com"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "");
				startActivity(Intent.createChooser(emailIntent, "Envoyer un email"));
			}
		});

		Button contactTopMorningProgramButton = (Button) wrapperView.findViewById(R.id.contactTopMorningProgramButton);
		contactTopMorningProgramButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setType("text/plain");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"matin.topcongo@gmail.com"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "");
				startActivity(Intent.createChooser(emailIntent, "Envoyer un email"));
			}
		});

		Button contactTechnicalAssistanceButton = (Button) wrapperView.findViewById(R.id.contactTechnicalAssistanceButton);
		contactTechnicalAssistanceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setType("text/plain");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mmingiedi@gmail.com"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "");
				startActivity(Intent.createChooser(emailIntent, "Envoyer un email"));
			}
		});


		// SKYPE

		Button contactSkypeButton = (Button) wrapperView.findViewById(R.id.contactSkypeButton);
		contactSkypeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Context context = ((Context) activity);

				// http://developer.skype.com/skype-uris/skype-uri-tutorial-android

				if (!isSkypeClientInstalled(context)) {
					goToMarket(context);
					return;
				}

				// Create the Intent from our Skype URI
				Uri skypeUri = Uri.parse("skype:topcongo?call");
				Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

				// Restrict the Intent to being handled by the Skype for Android client only
				myIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				// Initiate the Intent. It should never fail since we've already established the
				// presence of its handler (although there is an extremely minute window where that
				// handler can go away...)
				context.startActivity(myIntent);

			}
		});

	}

    private void startPhoneCall() {
        if (TextUtils.isEmpty(phoneNumberToCall)) {
            return;
        }
        if (PermissionHelper.isPermissionGranted(ContactActivity.this, android.Manifest.permission.CALL_PHONE, CALL_PHONE_PERMISSION_REQUEST_CODE)) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumberToCall));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handleRequestPermissionResult(requestCode, permissions, grantResults);
    }

    private void handleRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
            if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE) {
                startPhoneCall();
            }
        }
    }
	
	public boolean isSkypeClientInstalled(Context context) {
		
		PackageManager myPackageMgr = context.getPackageManager();
		
		try {
			
			myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
			
		} catch (ActivityNotFoundException e) {
			return false;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		
		return true;
}
	
	public void goToMarket(Context context) {
		
		try {
            
			Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			context.startActivity(marketIntent);
        } catch (ActivityNotFoundException e) {
            Log.e("GOOGLE PLAY", "MARKET APP NOT FOUND", e);
        }
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

}
