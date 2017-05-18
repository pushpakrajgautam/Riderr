package io.github.maniknarang.riderr;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener, TextWatcher, View.OnTouchListener {
    private EditText autoCompleteTextView;
    private AutoCompleteAdapter autoCompleteAdapter;
    private MapActivity mapActivity;
    private String query;
    private AutoCompleteTask autoCompleteTask;
    private long startClickTime;
    private InputMethodManager imm;
    private OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.search_frag,container,false);
        view.setClickable(true);
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();
        mapActivity = (MapActivity) getActivity();
        mapActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        autoCompleteTextView = (EditText) view.findViewById(R.id.auto_complete_text_view);
        autoCompleteTextView.requestFocus();
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_IMPLICIT_ONLY);
        Typeface font1 = Typeface.createFromAsset(getContext().getAssets(),"Raleway-SemiBold.ttf");
        Typeface dogFont = Typeface.createFromAsset(getContext().getAssets(),"GoodDog.otf");
        autoCompleteTextView.setTypeface(font1);
        ArrayList<OptionName> optionNames = new ArrayList<>();
        autoCompleteAdapter = new AutoCompleteAdapter(getContext(),R.layout.complete_list_item,optionNames);
        autoCompleteTextView.addTextChangedListener(this);
        ListView listView = (ListView) view.findViewById(R.id.list_autocomp);
        listView.setOnItemClickListener(this);
        listView.setAdapter(autoCompleteAdapter);
        listView.setOnTouchListener(this);
        TextView textView = (TextView) view.findViewById(R.id.powered);
        textView.setTypeface(dogFont);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        autoCompleteTextView.setText(((OptionName)adapterView.getItemAtPosition(i)).getName());
        Toast.makeText(getContext(),"Touched",Toast.LENGTH_LONG).show();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {
        query = charSequence.toString();
        if (autoCompleteTask != null)
        {
            autoCompleteTask.cancel(true);
            autoCompleteTask = new AutoCompleteTask();
            autoCompleteTask.execute(query);
        }
        else
        {
            autoCompleteTask = new AutoCompleteTask();
            autoCompleteTask.execute(query);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {}

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            startClickTime = System.currentTimeMillis();
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {

            if (System.currentTimeMillis() - startClickTime < ViewConfiguration.getTapTimeout())
            {
            }
            else
                imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }

        return false;
    }


    private class AutoCompleteTask extends AsyncTask<String,Void,ArrayList<OptionName>>
    {
        @Override
        protected ArrayList<OptionName> doInBackground(String... urls)
        {
            ArrayList<OptionName> names = new ArrayList<>();
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" +
                    urls[0] + "&key=AIzaSyDbndlJPMDm4YnF3zOBNipW_7rD2MfvEXc";
            Request request = new Request.Builder()
                    .url(url).build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray jsonArray = jsonObject.getJSONArray("predictions");
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    String fullName = jsonArray.getJSONObject(i).getString("description");
                    String placeId = jsonArray.getJSONObject(i).getString("id");
                    JSONArray typeArr = jsonArray.getJSONObject(i).getJSONArray("types");
                    String symbol = fullName;
                    for(int f=0; f<typeArr.length(); f++)
                    {
                        if(typeArr.getString(f).equals("transit_station"))
                        {
                            symbol = "bus";
                            break;
                        }
                    }
                    String[] named = fullName.split(",");
                    String detaild = "";
                    for (int j = 1; j < named.length; j++)
                    {
                        if (j != named.length - 1)
                            detaild += named[j] + ",";
                        else
                            detaild += named[j];

                        if(isCancelled())
                            return new ArrayList<OptionName>();
                    }
                    if (detaild.length() > 1)
                        detaild = detaild.substring(1);

                    String[] metr = fullName.split(" ");
                    for(int j=0; j<metr.length;j++)
                    {
                        if(metr[j].equals("metro") || metr[j].equals("Metro"))
                        {
                            symbol="metro";
                            break;
                        }
                    }

                    names.add(new OptionName(named[0], detaild, symbol, placeId));
                }
                return names;
            }
            catch (IOException e) {}
            catch (JSONException e) {}
            finally
            {
                if(response != null)
                    response.body().close();
            }
            return names;
        }

        @Override
        protected void onPostExecute(ArrayList<OptionName> optionNames)
        {
            super.onPostExecute(optionNames);
            autoCompleteAdapter.clear();
            autoCompleteAdapter.addAll(optionNames);
        }

    }
}
