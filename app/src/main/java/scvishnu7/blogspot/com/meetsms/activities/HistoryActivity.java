package scvishnu7.blogspot.com.meetsms.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import scvishnu7.blogspot.com.meetsms.HistoryConnectorInterface;
import scvishnu7.blogspot.com.meetsms.MeetMeSMSApplication;
import scvishnu7.blogspot.com.meetsms.R;
import scvishnu7.blogspot.com.meetsms.helpers.DatabaseHelper;
import scvishnu7.blogspot.com.meetsms.helpers.HistoryListAdaptor;
import scvishnu7.blogspot.com.meetsms.models.Message;

public class HistoryActivity extends ActionBarActivity {

    private ListView historyListView;
    private HistoryListAdaptor historyListAdaptor;
    private DatabaseHelper dbHelper;
    private ArrayList<Message> msgArraylist;
    private Context context;
    private Tracker mTracker;

    public HistoryConnectorInterface histInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = HistoryActivity.this;

        // Obtain the shared Tracker instance.
        MeetMeSMSApplication application = (MeetMeSMSApplication) getApplication();
        mTracker = application.getDefaultTracker();

        historyListView = (ListView) findViewById(R.id.historyListView);
        dbHelper = new DatabaseHelper(context);
        msgArraylist = dbHelper.getAllHistory();
        historyListAdaptor = new HistoryListAdaptor(context,msgArraylist);
        historyListView.setAdapter(historyListAdaptor );
        historyListAdaptor.setNotifyOnChange(true);
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Message msg = msgArraylist.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteHistory(msg);
                        msgArraylist.remove(msg);
                        historyListAdaptor.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("ReSend", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        histInterface.populateField(msg.msg, msg.to);
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("ReSend Clicked")
                                .build());
                        finishWithResult(msg.msg, msg.to);
                    }
                });
                builder.setTitle("Message Details");
                String dateString = new SimpleDateFormat("yy/MM/dd HH:mm").format(new Date(Long.parseLong(msg.date)));
                builder.setMessage("id: " + msg.id +
                        "\nto: " + msg.to +
                        "\nmsg: " + msg.msg +
                        "\ndate: " + dateString +
                        "\nstatus: " + msg.status);
                builder.show();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        mTracker.setScreenName("History Screen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
//            NavUtils.navigateUpFromSameTask(this);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void finishWithResult(String msg, String to)
    {
        Bundle conData = new Bundle();
        conData.putString("msg", msg);
        conData.putString("to", to);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }

}
