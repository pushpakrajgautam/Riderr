package io.github.maniknarang.riderr;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class ResultActivity extends AppCompatActivity
{
    private ResultAdapter adapter;
    private ListView listView;
    private String originAdd, destAdd;
    private TextView originText;
    private TextView destText;
    private LoginManager loginManager;
    public double origin1,origin2,dest1,dest2;
    private boolean compared = false;
    private final long delayMillis=1000;
    private Handler h=null;
    private Runnable r;
    public String urla;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_viw);
        Intent intent = getIntent();
        String url = intent.getStringExtra("JsonUrl");
        urla=url;
        DownloadTask downloadTask = new DownloadTask();
        String url2 = url + "&mode=walking";
        String url3 = url + "&mode=bicycling";
        String urls[] = new String[]{url,url2,url3};
        downloadTask.execute(urls);
        originText = (TextView) findViewById(R.id.origin_text);
        destText = (TextView) findViewById(R.id.dest_text);
        origin1 = intent.getDoubleExtra("origin1",0.00);
        origin2 = intent.getDoubleExtra("origin2",0.00);
        dest1 = intent.getDoubleExtra("dest1",0.00);
        dest2 = intent.getDoubleExtra("dest2",0.00);

        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("gEH7g1vD2lJxewUaOK_Us_g4WisxM3iK")
                .setServerToken("4E4HmWduJNBOCa-au7mfTkRof-MVBfPf-giQqNCu")
                .setRedirectUri("gEH7g1vD2lJxewUaOKUsg4WisxM3iK://uberConnect")
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();
        UberSdk.initialize(config);
    }

    public void requested(View v)
    {
        LoginCallback loginCallback = new LoginCallback()
        {
            @Override
            public void onLoginCancel() {}

            @Override
            public void onLoginError(@NonNull AuthenticationError error) {}

            @Override
            public void onLoginSuccess(@NonNull AccessToken accessToken) {}

            @Override
            public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {}
        };

        Intent intent = new Intent();
        dest1 = intent.getDoubleExtra("dest1",0.00);
        dest2 = intent.getDoubleExtra("dest2",0.00);

        AccessTokenManager accessTokenManager = new AccessTokenManager(this);
        loginManager = new LoginManager(accessTokenManager, loginCallback);
        loginManager.login(this);
        RideRequestButton rideRequestButton = (RideRequestButton) findViewById(R.id.request_buttona);
        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(origin1, origin2, null, originAdd)
                .setDropoffLocation(dest1, dest2, null, destAdd)
                .build();
        rideRequestButton.setRideParameters(rideParams);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent))
                {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                }
                else
                {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;

            case R.id.action_compare:
                Intent intent = new Intent(this,PayActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compare, menu);
        return true;
    }

    private class DownloadTask extends AsyncTask<String, Void, String[]>
    {
        @Override
        protected String[] doInBackground(String... url)
        {
            String data1 = "";
            String data2 = "";
            String data3 = "";

            try
            {
                data1 = downloadUrl(url[0]);
                data2 = downloadUrl(url[1]);
                data3 = downloadUrl(url[2]);
            }
            catch(Exception e)
            {
                Log.d("Background Task",e.toString());
            }
            return new String[]{data1,data2,data3};
        }

        @Override
        protected void onPostExecute(String[] s)
        {
            super.onPostExecute(s);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(s);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, ArrayList<Result>>
    {

        @Override
        protected ArrayList<Result> doInBackground(String... jsonData)
        {
            JSONObject jObject;
            JSONArray routes=null;
            ArrayList<Result> resultArrayList = new ArrayList<>();
            try
            {
                for(int l=0; l<3; l++) {
                    jObject = new JSONObject(jsonData[l]);
                    routes = jObject.getJSONArray("routes");
                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject travelC = routes.getJSONObject(i).optJSONObject("fare");
                        String travelCurr = "$0.00";
                        if (travelC != null)
                            travelCurr = travelC.getString("text");
                        JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");
                        for (int j = 0; j < legs.length(); j++) {
                            String travelDur = legs.getJSONObject(j).getJSONObject("duration").getString("text");
                            String travelDist = legs.getJSONObject(j).getJSONObject("distance").getString("text");
                            String travelMode = legs.getJSONObject(j).getJSONArray("steps").getJSONObject(0)
                                                .getString("travel_mode");
                            Result res = new Result(travelMode, travelDist, travelDur, travelCurr);
                            resultArrayList.add(res);
                        }
                    }
                }

                jObject = new JSONObject(jsonData[0]);
                JSONArray geoPoints = jObject.getJSONArray("geocoded_waypoints");
                String placeId1 = geoPoints.getJSONObject(0).getString("place_id");
                String placeId2 = geoPoints.getJSONObject(geoPoints.length()-1).getString("place_id");

                String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId1+
                        "&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
                String place1Data = downloadUrl(url);
                url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId2+
                        "&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
                String place2Data = downloadUrl(url);

                JSONObject jPlace1 = new JSONObject(place1Data);
                JSONObject jPlace2 = new JSONObject(place2Data);

                originAdd = jPlace1.getJSONObject("result").getString("formatted_address");
                destAdd = jPlace2.getJSONObject("result").getString("formatted_address");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            return resultArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Result> lists)
        {
            super.onPostExecute(lists);
            if(lists!=null)
                adapter = new ResultAdapter(ResultActivity.this,lists);
            listView = (ListView) findViewById(R.id.list_view);
            if(adapter != null)
            {
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                    {

                    }
                });
            }
            originText.setText(originAdd);
            destText.setText(destAdd);
        }
    }

    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }
        catch(Exception e)
        {
        }
        finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        loginManager.onActivityResult(this, requestCode, resultCode, data);
    }
}
