package io.github.maniknarang.riderr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.stripe.android.model.Token;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, LocationListener, GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback {
    private GoogleApiClient mGoogleApiClient;
    public static Location mCurrentLocation;
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private int curMapTypeIndex = 1;
    private LocationRequest mLocationRequest;
    private boolean mapped = false;
    private String url;
    private Marker marker;
    private GoogleMap googleMap;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            alertLocationDialog();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void alertLocationDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Settings");
        alertDialog.setMessage("The app requires GPS. Enable it?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(i, 0);
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), "Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        //markerPoints.clear();
        //getMap().clear();
        //LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        //markerPoints.add(origin);
        //markerPoints.add(latLng);
        initMarker(latLng);
        /*if (markerPoints.size() >= 2) {
            LatLng dest = markerPoints.get(1);
            url = getDirectionsUrl(origin, dest);
            mapped=true;
            Toast.makeText(getContext(),"Long tap to view results",Toast.LENGTH_LONG).show();
         }*/
        // sequence - results(array) - geometry - location - lat lng
        // dir url = https://maps.googleapis.com/maps/api/directions/json?origin=Magadi+Main+Rd,Bengaluru,Karnataka,India&destination=Majestic,Bengaluru,Karnataka,India&mode=transit&transit_mode=subway&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.maps));
        initListeners();
    }

    private class NearbyPlacesTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... urls) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();
            Request request1 = new Request.Builder()
                    .url(urls[0])
                    .build();
            Request request2 = new Request.Builder()
                    .url(urls[1])
                    .build();
            client.newCall(request1).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.v("1:", responseData);
                    } catch (JSONException e) {
                    }
                }
            });

            client.newCall(request2).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.v("Hey", urls[1]);

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseData);
                        Log.v("2:", responseData);
                    } catch (JSONException e) {
                        Log.v("Hey", "JSON" + e.toString());
                    }
                }
            });

            RequestBody formBody = new FormBody.Builder()
                    .add("stopID", "2714")
                    .build();
            Request request = new Request.Builder()
                    .url("http://bmtcmob.hostg.in/api/itsstopwise/details")
                    .post(formBody)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful())
                    Log.v("resp:", response.body().string());
                else
                    Log.v("err", response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        /*if(mapped)
        {
            mapped=false;
            Intent intent = new Intent(getActivity(),ResultActivity.class);
            intent.putExtra("JsonUrl",url);
            startActivity(intent);
        }*/
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation == null) {
            mCurrentLocation = location;
            initCamera(mCurrentLocation);
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            initMarker(latLng);
        } else {
            mCurrentLocation = location;
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        /*markerPoints.clear();
        getMap().clear();
            LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            markerPoints.add(origin);
            LatLng latLng = marker.getPosition();
            markerPoints.add(latLng);
            MarkerOptions options = new MarkerOptions().position(latLng);
            options.title(getAddressFromLatLng(latLng));
            options.icon(BitmapDescriptorFactory.fromBitmap(generateBitmapFromDrawable(R.drawable.ic_house)));
            getMap().addMarker(options).setDraggable(true);
        if(markerPoints.size() >= 2)
        {
            LatLng dest = markerPoints.get(1);
            String url = getDirectionsUrl(origin, dest);
            mapped=true;
            Toast.makeText(getContext(),"Long tap to view results",Toast.LENGTH_LONG).show();
        }*/

        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;
        String urlMetro = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng +
                "&radius=1000&type=subway_station&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
        String urlBus = "http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + lat + "/lon/" + lng + "/rad/1";
        new NearbyPlacesTask().execute(urlMetro, urlBus);
    }

    public void onConnected(Bundle bundle)
    {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
        try
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
        }
        catch(SecurityException e) {}

    }

    private void initCamera(Location mCurrentLocation)
    {
        CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude())).zoom(16f).bearing(0.0f).tilt(0.0f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), null);
        googleMap.setMapType( MAP_TYPES[curMapTypeIndex] );
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        initMarker(latLng);
    }

    private void initListeners()
    {
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnInfoWindowClickListener( this );
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        try
        {
            googleMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e)
        {

        }
    }

    private String getAddressFromLatLng( LatLng latLng )
    {
        Geocoder geocoder = new Geocoder( getActivity() );

        String address = "";
        try
        {
            address = geocoder
                    .getFromLocation( latLng.latitude, latLng.longitude, 1 )
                    .get( 0 ).getAddressLine( 0 );
        }
        catch (IOException e )
        {
        }

        return address;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest)
    {
        // Origin of route
        String str_origin = "&origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+
                "&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";

        return url;
    }

    private Bitmap generateBitmapFromDrawable(int drawablesRes)
    {
        Bitmap bitmap;
        Drawable drawable = getResources().getDrawable(drawablesRes);
        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        bitmap = Bitmap.createScaledBitmap(bitmap,2*bitmap.getWidth(),2*bitmap.getHeight(),true);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void initMarker(LatLng latLng)
    {
        if(marker != null)
            marker.remove();
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));
        options.icon(BitmapDescriptorFactory.fromBitmap(generateBitmapFromDrawable(R.drawable.ic_house)));
        marker = googleMap.addMarker(options);
        marker.setDraggable(true);

        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;
        String urlMetro = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng +
                "&radius=1000&type=subway_station&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
        //String urlBus = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng +
        //        "&radius=1000&type=bus_station&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
        String urlBus = "http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + lat +"/lon/" + lng + "/rad/1";
        new NearbyPlacesTask().execute(urlMetro,urlBus);
    }
}