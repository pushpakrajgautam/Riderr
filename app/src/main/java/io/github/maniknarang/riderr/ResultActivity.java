package io.github.maniknarang.riderr;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
import java.util.HashMap;
import java.util.List;

public class ResultActivity extends AppCompatActivity
{
    private ResultAdapter adapter;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_viw);
        Intent intent = getIntent();
        String url = intent.getStringExtra("JsonUrl");
        DownloadTask downloadTask = new DownloadTask();
        String url1 = url + "&mode=transit";
        String url2 = url + "&mode=walking";
        String url3 = url + "&mode=bicycling";
        String urls[] = new String[]{url,url1,url2,url3};
        downloadTask.execute(urls);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadTask extends AsyncTask<String, Void, String[]>
    {
        @Override
        protected String[] doInBackground(String... url)
        {
            // For storing data from web service
            String data1 = "";
            String data2 = "";
            String data3 = "";
            String data4 = "";

            try
            {
                // Fetching the data from web service
                data1 = downloadUrl(url[0]);
                data2 = downloadUrl(url[1]);
                data3 = downloadUrl(url[2]);
                data4 = downloadUrl(url[3]);
            }
            catch(Exception e)
            {
                Log.d("Background Task",e.toString());
            }
            return new String[]{data1,data2,data3,data4};
        }

        @Override
        protected void onPostExecute(String[] s)
        {
            super.onPostExecute(s);
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
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
                for(int l=0; l<4; l++) {
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
                            String travelMode = legs.getJSONObject(j).getJSONArray("steps").getJSONObject(0).getString("travel_mode");
                            Result res = new Result(travelMode, travelDist, travelDur, travelCurr);
                            resultArrayList.add(res);
                        }
                    }
                }
            }
            catch (JSONException e)
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
        }
    }

    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e)
        {
        }
        finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

}
