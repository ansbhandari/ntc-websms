package scvishnu7.blogspot.com.meetsms.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by vishnu on 12/3/15.
 */
public class PreferenceHelper {

   private SharedPreferences prefs;

    private final String keySMSSentToday="SMSSentToday";
    private final String keyDateLastSmsSent="LastSMSSentDate";

    public PreferenceHelper(Context context){
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getSmsSentToday(){
       return prefs.getInt(keySMSSentToday,0);
    }

    public void setSMSSentToday(int smsCount){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(keySMSSentToday,smsCount);
        Log.v("PREFS","UPDATED prefs with "+smsCount);
        editor.commit();
    }

    public long getDateLastSMSSent(){
        return prefs.getLong(keyDateLastSmsSent,0);
    }
    public void setDateLastSMSSent(Long date){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(keyDateLastSmsSent,date);
        editor.commit();
    }

}
