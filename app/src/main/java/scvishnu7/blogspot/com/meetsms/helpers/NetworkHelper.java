package scvishnu7.blogspot.com.meetsms.helpers;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vishnu on 11/15/15.
 *
 *
 * curl --request POST 'http://www.meet.net.np/meet/action/login' --data "username=$username" --data "password=$password" --data "loginPage=true" --data "loginbut=login"  -c cookies.dat


 curl --request POST 'http://www.meet.net.np/meet/mod/sms/actions/send.php' --data "recipient=$recipient" --data "message=$message" --data "sendbutton=Send Now" -b cookies.dat -v

 */
public class NetworkHelper {

    private static final String cookiesFile="meetCookies.dat";

    private static final String loginUrl="http://www.meet.net.np/meet/action/login";
    private static final String sendMessageUrl="http://www.meet.net.np/meet/mod/sms/actions/send.php";
    CookieManager cookieManager = new CookieManager();

    public NetworkHelper() {
        CookieHandler.setDefault(cookieManager);
        Log.d("NetworkHelper","Constructor here");
    }

    private BufferedReader simpleHttpReq(String link, ArrayList<NameValuePair> postData) throws IOException {

        String line;
        StringBuilder content = new StringBuilder();
        URL url = new URL(link);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");

        OutputStream os = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(postData));
        writer.flush();
        writer.close();
        os.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        Log.d("Utils", "bufferedReader returned");

        for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
            Log.d("Cookies",httpCookie.getName()+" : "+httpCookie.getValue());
        }


        return bufferedReader;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    /**
     *
     * @return true on success of login
     *          false on failure of login
     *
     *           --data "username=$username" --data "password=$password" --data "loginPage=true" --data "loginbut=login"  -c cookies.dat
     *
     *           Success Msg
     *           <ul class="elgg-system-messages"><li class="hidden"></li><li class="elgg-message elgg-state-success">You have been logged in.</li></ul>                         </div>


        Error Msg
    <ul class="elgg-system-messages"><li class="hidden"></li><li class="elgg-message elgg-state-error">We could not log you in. Please check your username/email and password.</li></ul>            </div>

     */
    public boolean requestLogin(String uname, String pass){

        boolean status=false;

        ArrayList<NameValuePair> postData=new ArrayList<>();
        postData.add(new BasicNameValuePair("username",uname));
        postData.add(new BasicNameValuePair("password",pass));
        postData.add(new BasicNameValuePair("loginPage","true"));
        postData.add(new BasicNameValuePair("loginbut","login"));

        String line;
        BufferedReader bfreader=null;
        StringBuilder content=new StringBuilder();
        boolean continueReading=false;

        try {
            bfreader = simpleHttpReq(loginUrl,postData);
            while( (line=bfreader.readLine())!=null){
                content.append(line);
                Log.d("Utils",line+"\n");
                if( line.contains("You have been logged in")){
                    status = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

    /**
     *
     * @return
     *
     * "recipient=$recipient" --data "message=$message" --data "sendbutton=Send Now" -b cookies.dat -v
     *
     * Success MSG
     * <ul class="elgg-system-messages"><li class="hidden"></li><li class="elgg-message elgg-state-success">Your messages for 1 recipients were saved for submission.</li></ul>                                </div>

     Quote MSG
    <p style="color:chocolate"><b>Free SMS Quota:10 ; Subscribed SMS Quota:0 ; Used SMS Quota : 2</b></p>


     No Login MSG
    <ul class="elgg-system-messages"><li class="hidden"></li><li class="elgg-message elgg-state-error">You must be logged in to view that page.</li></ul>           </div>


     */
    public boolean sendSms(String message, String recipient) {

        boolean status=false;
        ArrayList<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("recipient",recipient));
        postData.add(new BasicNameValuePair("message",message));
        postData.add(new BasicNameValuePair("sendbutton","Send Now"));

        String line;
        BufferedReader bfreader=null;
        StringBuilder content=new StringBuilder();
        boolean continueReading=false;

        try {
            bfreader = simpleHttpReq(sendMessageUrl,postData);
            while( (line = bfreader.readLine()) != null) {
//                content.append(line);
//                Log.d("Utils sendsms",line+"\n");
                if(line.contains("Your messages for 1 recipients were saved for submission")){
                    status=true;
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }



}
