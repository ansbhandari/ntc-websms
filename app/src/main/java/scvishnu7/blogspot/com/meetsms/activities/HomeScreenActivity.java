package scvishnu7.blogspot.com.meetsms.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Func0;
import scvishnu7.blogspot.com.meetsms.R;
import scvishnu7.blogspot.com.meetsms.helpers.NetworkHelper;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class HomeScreenActivity extends ActionBarActivity {


    private static final int CONTACT_PICKER_RESULT = 1001;

    private LinearLayout sendSMSView;
    private EditText receiversEditText;
    private EditText messageEditText;
    private Button sendButton;
    private Button searchContactButton;

    private LinearLayout registerView;
    private Button gotoSettingButton;
    private Button registerButton;

    private Boolean isLogined=false;

    private Handler backgroundHandler;

    private NetworkHelper networkHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        networkHelper = new NetworkHelper();

        sendSMSView = (LinearLayout) findViewById(R.id.sendsmsView);
        registerView = (LinearLayout) findViewById(R.id.registerView);

        receiversEditText = (EditText) findViewById(R.id.receiversEditText);
        messageEditText = (EditText) findViewById(R.id.messageTextEditText);

        progressDialog = new ProgressDialog(HomeScreenActivity.this);

        // Background Thread
        Log.d("Main","Starting bghandler register");
        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        Log.d("Main","backgroundhandler started");

        searchContactButton = (Button) findViewById(R.id.searchContactButton);
        searchContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeScreenActivity.this,"Will implement later",Toast.LENGTH_SHORT).show();
//                launchContactPicker();

            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=messageEditText.getText().toString();
                String recipient = receiversEditText.getText().toString();

                //Show progessbar


            if ( message ==  null || message.trim().length() ==0 || recipient == null || recipient.trim().length() == 0){
                    Toast.makeText(HomeScreenActivity.this, "Please provide recipient and message text.",Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.setMessage("Sending Message ...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);

                progressDialog.show();
                    sendSMSNow(message, recipient);
                }

            }
        });


    }

    private void launchContactPicker() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    // handle contact results
                    Bundle extras = data.getExtras();
                    Set keys = extras.keySet();
                    Iterator iterate = keys.iterator();
                    while (iterate.hasNext()) {
                        String key = iterate.next().toString();
                        Log.v("ActivityResult", key + "[" + extras.get(key) + "]");
                    }
                    Uri result = data.getData();
                    Log.v("ActivityResult", "Got a result: "
                            + result.toString());
                    break;
            }

        } else {
            // gracefully handle failure
            Log.w("ActivityResult", "Warning: activity result not ok");
        }
    }




    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(HomeScreenActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void sendSMSNow(String message, String recipient) {

        sampleObservable(networkHelper, message, recipient)
                //Run on BackgroundThread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                        //be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("Counter", "onComplete of subscriber");
                        //STOP progress bar
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "SMS send successfully",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Counter", "onError" + e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("Counter", "onNext(" + s + ")");
                    }
                });
    }

    static Observable<String> sampleObservable(final NetworkHelper nh, final String msg, final String recv) {


        return Observable.defer(new Func0<Observable<String>>() {

            @Override
            public Observable<String> call() {

                nh.requestLogin("scvishnu7", "MyMeetPass");
                // Will handle this thing later :P
                // may need to use presistance cookies store
                nh.sendSms(msg, recv);
                return Observable.just("Done");
            }
        });
    }

    static  class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }
}


