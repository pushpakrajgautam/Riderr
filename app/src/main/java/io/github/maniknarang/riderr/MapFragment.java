package io.github.maniknarang.riderr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, LocationListener,GoogleMap.OnMarkerDragListener
{
    private GoogleApiClient mGoogleApiClient;
    public static Location mCurrentLocation;
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_NORMAL,
                                     GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_TERRAIN,
                                     GoogleMap.MAP_TYPE_NONE };
    private int curMapTypeIndex = 1;
    private LocationRequest mLocationRequest;
    private boolean mapped = false;
    private ArrayList<LatLng> markerPoints;
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        markerPoints = new ArrayList<LatLng>();
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
            else
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
        initListeners();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(getContext(), "Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }

    @Override
    public void onMapClick(LatLng latLng)
    {
            markerPoints.clear();
            getMap().clear();
            LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            markerPoints.add(origin);
            markerPoints.add(latLng);
            MarkerOptions options = new MarkerOptions().position(latLng);
            options.title(getAddressFromLatLng(latLng));
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            getMap().addMarker(options).setDraggable(true);
            if (markerPoints.size() >= 2) {
                LatLng dest = markerPoints.get(1);
                url = getDirectionsUrl(origin, dest);
                mapped=true;
                Toast.makeText(getContext(),"Long tap to view results",Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        if(mapped)
        {
            mapped=false;
            Intent intent = new Intent(getActivity(),ResultActivity.class);
            intent.putExtra("JsonUrl",url);
            startActivity(intent);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation=location;
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        markerPoints.clear();
        getMap().clear();
        LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        markerPoints.add(origin);
        LatLng latLng = marker.getPosition();
        markerPoints.add(latLng);
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        getMap().addMarker(options).setDraggable(true);
        if(markerPoints.size() >= 2)
        {
            LatLng dest = markerPoints.get(1);
            String url = getDirectionsUrl(origin, dest);
            mapped=true;
            Toast.makeText(getContext(),"Long tap to view results",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 101:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10 * 1000)
                            .setFastestInterval(1 * 1000);

                }
                else
                {

                }
                return;
            }
        }
    }

    public void locationAlertDialog(final Context context)
    {

        AlertDialog.Builder d = new AlertDialog.Builder(context);
        d.setTitle("No Providers");
        d.setCancelable(false);
        d.setMessage("No providers to get your location. Press Ok to turn on your GPS.");

        d.setPositiveButton(context.getResources().getString(R.string.dialogAccept), new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(i, 0);
            }
        });
        AlertDialog dialog = d.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    public void onConnected(Bundle bundle)
    {
        try
        {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch(SecurityException e)
        {

        }
        if(mCurrentLocation == null)
        {
            if (mLocationRequest == null)
            {
                locationAlertDialog(getActivity());
                mLocationRequest = LocationRequest.create();
            }
            try
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            }
            catch(SecurityException e)
            {

            }
        }
        else
        {
            initCamera(mCurrentLocation);
        }
    }

    private void initCamera(Location mCurrentLocation)
    {
        CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude())).zoom(16f).bearing(0.0f).tilt(0.0f).build();
        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), null);
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
    }

    private void initListeners()
    {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener( this );
        getMap().setOnMarkerDragListener(this);
        getMap().setOnMapClickListener(this);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);
        try
        {
            getMap().setMyLocationEnabled(true);
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
}