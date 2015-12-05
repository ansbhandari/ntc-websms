package scvishnu7.blogspot.com.meetsms.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Func0;
import scvishnu7.blogspot.com.meetsms.R;
import scvishnu7.blogspot.com.meetsms.helpers.NetworkHelper;
import scvishnu7.blogspot.com.meetsms.helpers.PreferenceHelper;
import scvishnu7.blogspot.com.meetsms.helpers.Utils;
import scvishnu7.blogspot.com.meetsms.models.Constants;
import scvishnu7.blogspot.com.meetsms.models.PhoneNumber;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class HomeScreenActivity extends ActionBarActivity {


    private static final String TAG="HomeScreen";
    private PreferenceHelper prefHelper;
    private static final int CONTACT_PICKER_RESULT = 1001;

    private LinearLayout sendSMSView;
    private EditText receiversEditText;
    private EditText messageEditText;
    private TextView charCounterTextView;
    private TextView smsCounterTextView;
    private TextView quotaTextView;

    private Button sendButton;
    private Button searchContactButton;

    private LinearLayout registerView;
    private Button gotoSettingButton;
    private Button registerButton;

    private Boolean isLogined=false;
    private Boolean isSignatureOn=false;
    private String signature;
    private String uname;
    private String pass;
    private int textLimit;

    private Handler backgroundHandler;

    private NetworkHelper networkHelper;
    private ProgressDialog progressDialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        context=HomeScreenActivity.this;
        networkHelper = new NetworkHelper(context);
        prefHelper = new PreferenceHelper(context);
        checkSMSsentDate();

        charCounterTextView = (TextView) findViewById(R.id.charCounterTextView);

        sendSMSView = (LinearLayout) findViewById(R.id.sendsmsView);
        registerView = (LinearLayout) findViewById(R.id.registerView);

        receiversEditText = (EditText) findViewById(R.id.receiversEditText);
        smsCounterTextView = (TextView) findViewById(R.id.smsCountTextView);
        quotaTextView = (TextView) findViewById(R.id.quotaTextView);

        quotaTextView.setText(prefHelper.getSmsSentToday()+"/10 sms today");

        messageEditText = (EditText) findViewById(R.id.messageTextEditText);
        messageEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                int charRemaning = textLimit-s.length();
                if(charRemaning==textLimit) {
                    charCounterTextView.setTextColor(Color.BLACK);
                    smsCounterTextView.setText("0");
                    sendButton.setEnabled(true);
                } else if( charRemaning > (textLimit-Constants.SingleSMSSize) ){
                    charCounterTextView.setTextColor(Color.BLACK);
                    smsCounterTextView.setText("1");
                    sendButton.setEnabled(true);
                } else if ( charRemaning >=0 ){
                    smsCounterTextView.setText("2");
                    charCounterTextView.setTextColor(Color.BLACK);
                    sendButton.setEnabled(true);
                } else {
                    charCounterTextView.setTextColor(Color.RED);
                    smsCounterTextView.setText("2");
                    sendButton.setEnabled(false);
                }


                charCounterTextView.setText(charRemaning+"");
            }

            public void afterTextChanged(Editable s) {
            }
        });


        progressDialog = new ProgressDialog(context);

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
//                Toast.makeText(HomeScreenActivity.this,"Will implement later",Toast.LENGTH_SHORT).show();
                launchContactPicker();

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
                try  {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
                progressDialog.setMessage("Sending Message ...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);

                progressDialog.show();

                if(isSignatureOn){
                    message=message+" "+signature;
                }

                Log.d("Prefs",uname+" :: "+pass+" :: "+message);

                    sendSMSNow(message, recipient, uname, pass);
                }

            }
        });


    }


    private void launchContactPicker() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<PhoneNumber> contact = new ArrayList<>();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    try {
                    if (cursor != null) {
                            while (cursor.moveToNext()) {
                                String contactId =cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                contact.add(new PhoneNumber("Name",name));
                                //
                                //  Get all phone numbers.
                                //
                                Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);
                                while (phones.moveToNext()) {
                                    String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                                    int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
                                    String typeStr="";
                                    switch (type) {
                                        case Phone.TYPE_HOME:
                                            typeStr="Home";
                                            break;
                                        case Phone.TYPE_MOBILE:
                                            typeStr="Mobile";
                                            break;
                                        case Phone.TYPE_WORK:
                                            typeStr="Work";
                                            break;
                                        case Phone.TYPE_COMPANY_MAIN:
                                            typeStr="Company";
                                            break;
                                        case Phone.TYPE_CUSTOM:
                                            typeStr="Other";
                                            break;
                                           }
                                    contact.add(new PhoneNumber(typeStr,number));
                                }
                                phones.close();
                            }
                            cursor.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            displayContact(contact);
        }
    }

    private void displayContact(ArrayList<PhoneNumber> cont){

        if (cont.size()==1){
            Toast.makeText(context,"No Phone associated with this contact",Toast.LENGTH_SHORT).show();
            return;
        } else if (cont.size()==2){
            populateRecipient(cont.get(1).value);
            return;
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(cont.get(0).value+" has multiple phone. Select one");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                context,
                android.R.layout.select_dialog_singlechoice);
       for(int i=1;i<cont.size();i++){
           arrayAdapter.add(cont.get(i).value);
       }
        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        populateRecipient(arrayAdapter.getItem(which));

                    }
                });
        builderSingle.show();
    }

    private void populateRecipient(String recipient){
        receiversEditText.setText(recipient);
    }



    @Override
    public void onResume(){
        super.onResume();

//            Toast.makeText(context,"OnResume",Toast.LENGTH_LONG).show();
            signature="";
            isSignatureOn = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Utils.pref_key_signature_on,false);
            if( isSignatureOn ) {
                signature = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString(Utils.pref_key_msg_signature, "");
                textLimit=Constants.CharacterLimit-signature.length()-1;
            } else {
                textLimit=Constants.CharacterLimit;
            }
        charCounterTextView.setText(textLimit+"");

        uname= PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .getString(Utils.pref_key_uname, "");
            pass= PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .getString(Utils.pref_key_pass, "");

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

    void sendSMSNow(String message, String recipient, String uname, String pass) {
        String striped_recipient = recipient.replace("-","");
        sampleObservable(networkHelper, message, recipient, uname, pass)
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
//                        Toast.makeText(getApplicationContext(), "SMS send successfully", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Counter", "onError" + e.getMessage());
                        progressDialog.dismiss();
//                        Toast.makeText(getApplicationContext(), "Failed to send sms.", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("Counter", "onNext(" + s + ")");
                        if(s.contains("login_failed")){
                            Toast.makeText(getApplicationContext(), "Login Failed. Check username and/or password", Toast.LENGTH_LONG).show();
                        } else if (s.contains("sms_sent")){
                            Toast.makeText(getApplicationContext(), "SMS send successfully", Toast.LENGTH_LONG).show();
                            checkSMSsentDate();
                            quotaTextView.setText(prefHelper.getSmsSentToday()+"/10 sms today");
                        } else if (s.contains("sms_send_failed")){
                            Toast.makeText(getApplicationContext(), "Failed to send sms.", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "something else happened.. :O", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void checkSMSsentDate(){
        //difference with current date
        //if older then a day
        //write current date
        Calendar today = Calendar.getInstance();
        Calendar storedDay = Calendar.getInstance();
        storedDay.setTimeInMillis(prefHelper.getDateLastSMSSent());

        if(today.get(Calendar.DAY_OF_YEAR) != storedDay.get(Calendar.DAY_OF_YEAR)){
            prefHelper.setDateLastSMSSent(today.getTimeInMillis());
            prefHelper.setSMSSentToday(0);
        }
    }

    static Observable<String> sampleObservable(final NetworkHelper nh, final String msg, final String recv, final String uname, final String pass) {


        return Observable.defer(new Func0<Observable<String>>() {

            @Override
            public Observable<String> call() {

                if ( nh.requestLogin(uname, pass) ) {

                    if( nh.sendSms(msg, recv) ){
                        return Observable.just("sms_sent");
                    } else {
                        return Observable.just("sms_send_failed");
                    }
                } else {

                    return Observable.just("login_failed");

                }
                // Will handle this thing later :P
                // may need to use presistance cookies store
            }
        });
    }

    static  class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }
}


