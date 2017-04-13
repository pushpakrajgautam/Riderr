package io.github.maniknarang.riderr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkButtonBuilder;
import com.varunest.sparkbutton.SparkEventListener;

import java.io.IOException;
import java.lang.reflect.Type;

import static io.github.maniknarang.riderr.MapFragment.mCurrentLocation;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback
{
    private DrawerLayout drawer;
    public static LocationRequest mLocationRequest;
    private SparkButton loc_button;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Typeface font = Typeface.createFromAsset(getAssets(),"Lindbergh_Baby.ttf");
        TextView riderrHead = (TextView) findViewById(R.id.riderr_head_nav);
        riderrHead.setTypeface(font);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED))
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Exiting");
            alertDialog.setMessage("No internet connection");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivityForResult(i, 0);
                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        loc_button = (SparkButton) findViewById(R.id.spark_loc);

        SparkButton button  = (SparkButton) findViewById(R.id.spark_draw);
        button.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState)
            {
                if(drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                else
                    drawer.openDrawer(GravityCompat.START);

            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState)
            {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState)
            {

            }
        });

        SparkButton sparkSearch = (SparkButton) findViewById(R.id.spark_search);
        sparkSearch.setEventListener(new SparkEventListener()
        {
            @Override
            public void onEvent(ImageView button, boolean buttonState)
            {

            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState)
            {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState)
            {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.nav_menu)
        {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","p1gautam@ucsd.edu", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback - Riderr");
            startActivity(Intent.createChooser(emailIntent, "Send Email..."));
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        try
        {
            googleMap.setMyLocationEnabled(true);

        }
        catch (SecurityException e) {}
        loc_button.setEventListener(new SparkEventListener()
        {
            @Override
            public void onEvent(ImageView button, boolean buttonState)
            {
                if(googleMap.getMyLocation() != null)
                {
                    double lat = googleMap.getMyLocation().getLatitude();
                    double lon = googleMap.getMyLocation().getLongitude();
                    LatLng latLng = new LatLng(lat, lon);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,16);
                    googleMap.animateCamera(cameraUpdate);
                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState)
            {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState)
            {

            }
        });
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
}
