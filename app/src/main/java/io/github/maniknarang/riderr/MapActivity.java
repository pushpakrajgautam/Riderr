package io.github.maniknarang.riderr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkButtonBuilder;
import com.varunest.sparkbutton.SparkEventListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import github.nisrulz.recyclerviewhelper.RVHItemClickListener;
import github.nisrulz.recyclerviewhelper.RVHItemDividerDecoration;
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback,ResultCallback<LocationSettingsResult>,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener
{
    private DrawerLayout drawer;
    private SparkButton loc_button,spark_train,spark_parking,spark_cycle,spark_bus;
    public SlidingUpPanelLayout slidingUpPanelLayout;
    private FrameLayout frameLayout;
    private MapFragment mapFragment;
    private LocationManager locationManager;
    private Toast myToast;
    private SparkButton sparkSearch;
    private SparkButton button;
    private CardView cardSearch,cardButton;
    public RecyclerView rvStops;
    public StopAdapter stopAdapter;
    public ArrayList<Stop> stops;
    public SwipeRefreshLayout swipeRefreshLayout;
    private FragmentManager fragmentManager;
    private DetailFragment detailFragment;
    private LinearLayout linearLayout;
    private int thePanelHeight;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Typeface font = Typeface.createFromAsset(getAssets(),"Lindbergh_Baby.ttf");
        TextView riderrHead = (TextView) findViewById(R.id.riderr_head_nav);
        riderrHead.setTypeface(font);

        mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        fragmentManager = getSupportFragmentManager();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                buildGoogleApiClient();
                mapFragment.alertLocRequest();
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mapFragment.mLocationRequest);
                builder.setAlwaysShow(true);
                PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings
                        (mapFragment.mGoogleApiClient, builder.build());
                result.setResultCallback(this);
            }
        }
        else
            buildGoogleApiClient();

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int heightt = slidingUpPanelLayout.getPanelHeight();
        spark_train = (SparkButton) findViewById(R.id.spark_train);
        spark_bus = (SparkButton) findViewById(R.id.spark_bus);
        spark_cycle = (SparkButton) findViewById(R.id.spark_cycle);
        spark_parking = (SparkButton) findViewById(R.id.spark_parking);

        ViewGroup.LayoutParams params = spark_train.getLayoutParams();
        params.height=heightt;
        spark_train.setLayoutParams(params);
        spark_bus.setLayoutParams(params);
        spark_cycle.setLayoutParams(params);
        spark_parking.setLayoutParams(params);

        spark_train.setEventListener(new SparkEventListener()
        {
            @Override
            public void onEvent(ImageView button, boolean buttonState)
            {
                if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

                spark_train.setBackgroundColor(getResources().getColor(R.color.colorImp));
                spark_bus.setBackgroundColor(Color.WHITE);
                spark_cycle.setBackgroundColor(Color.WHITE);
                spark_parking.setBackgroundColor(Color.WHITE);

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

        loc_button = (SparkButton) findViewById(R.id.spark_loc);

        cardButton = (CardView) findViewById(R.id.card_button_view);
        button  = (SparkButton) findViewById(R.id.spark_draw);
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

        cardSearch = (CardView) findViewById(R.id.card_search_view);
        sparkSearch = (SparkButton) findViewById(R.id.spark_search);
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

        stops = new ArrayList<>();
        rvStops = (RecyclerView) findViewById(R.id.the_list);
        rvStops.setLayoutManager(new LinearLayoutManager(this));
        stopAdapter = new StopAdapter(this,stops);
        rvStops.setAdapter(stopAdapter);
        linearLayout = (LinearLayout) findViewById(R.id.panel_text);
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                thePanelHeight = linearLayout.getHeight();
            }
        });

        ItemTouchHelper.Callback callback = new RVHItemTouchHelperCallback(stopAdapter, true, true,
                true);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(rvStops);
        rvStops.addItemDecoration(new RVHItemDividerDecoration(this, LinearLayoutManager.VERTICAL));
        rvStops.addOnItemTouchListener(new RVHItemClickListener(this, new RVHItemClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, final int position)
            {

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(300);
                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationEnd(Animation animation)
                    {
                        Stop stop = stops.get(position);
                        Bundle bundle = new Bundle();
                        String[] stopTimes = new String[stop.getTimes().size()];
                        stopTimes = stop.getTimes().toArray(stopTimes);
                        String[] oppTimes = new String[stop.getoppTimes().size()];
                        oppTimes = stop.getoppTimes().toArray(oppTimes);
                        String[] fragPass = {stop.getName(),stop.getRoute(),stop.getTime(),stop.getOtherTime(),
                                            stop.getOrderNo(),stop.getOppOrder(),stop.getLine()};
                        bundle.putStringArray("stop_detail_head",fragPass);
                        bundle.putStringArray("stop_detail_time",stopTimes);
                        bundle.putStringArray("stop_detail_opp_time",oppTimes);
                        bundle.putString("stop_polyline",stop.getPolyline());
                        double[] coords = {stop.getLat(),stop.getLng()};
                        bundle.putDoubleArray("coords",coords);
                        linearLayout.setVisibility(View.GONE);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        detailFragment = new DetailFragment();
                        detailFragment.setArguments(bundle);
                        fragmentTransaction.setCustomAnimations(R.anim.slide_up,R.anim.slide_down);
                        fragmentTransaction.replace(R.id.linear_slide,detailFragment).commit();

                    }
                    public void onAnimationRepeat(Animation animation) {}
                    public void onAnimationStart(Animation animation) {}
                });

                Animation fadeOut1 = new AlphaAnimation(1, 0);
                fadeOut1.setInterpolator(new AccelerateInterpolator());
                fadeOut1.setDuration(300);
                swipeRefreshLayout.startAnimation(fadeOut1);
                linearLayout.startAnimation(fadeOut);
            }
        }));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                mapFragment.createTask(mapFragment.urlMetro);
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
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        this.googleMap=googleMap;
        loc_button.setEventListener(new SparkEventListener()
        {
            @Override
            public void onEvent(ImageView button, boolean buttonState)
            {
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MapActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapActivity.this);
                        alertDialog.setMessage("Your GPS seems to be disabled. Turn it on here.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(final DialogInterface dialog, final int id)
                                    {
                                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivityForResult(i, 500);
                                    }
                                });
                        AlertDialog dialog = alertDialog.create();
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                    }
                }
                else
                {
                    try
                    {
                        if (googleMap.getMyLocation() != null && mapFragment.mCurrentLocation != null)
                        {
                            double lat = googleMap.getMyLocation().getLatitude();
                            double lon = googleMap.getMyLocation().getLongitude();
                            LatLng latLng = new LatLng(lat, lon);
                            CameraPosition cameraPosition = CameraPosition.builder()
                                    .target(new LatLng(mapFragment.mCurrentLocation.getLatitude(),
                                    mapFragment.mCurrentLocation.getLongitude()))
                                    .zoom(16f).bearing(0.0f).tilt(0.0f).build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                            googleMap.animateCamera(cameraUpdate);
                        }
                        else
                        {
                            if(myToast != null)
                                myToast.cancel();
                            myToast = Toast.makeText(MapActivity.this, "Just a sec!", Toast.LENGTH_LONG);
                            myToast.show();

                        }
                    }
                    catch (IllegalStateException e)
                    {
                        Toast.makeText(MapActivity.this,"Just a sec. The app is enabling the location layer",
                                Toast.LENGTH_LONG).show();
                    }
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

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener()
        {
            @Override
            public void onPanelSlide(View panel, float slideOffset)
            {
                final int panelHeight = findViewById(R.id.linear_slide).getHeight();
                final int visiblePanelHeight = slidingUpPanelLayout.getPanelHeight();
                CameraPosition cameraPosition = googleMap.getCameraPosition();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                googleMap.setPadding(0,0,0, (int) ((panelHeight - visiblePanelHeight) *slideOffset));
                loc_button.setTranslationY(-(panelHeight - visiblePanelHeight) *slideOffset);
                final int scrollViewHeight = (int) ((panelHeight - visiblePanelHeight) * (slideOffset));
                rvStops.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        scrollViewHeight));
                if(slideOffset >0.8)
                {
                    sparkSearch.setAlpha(1 - slideOffset);
                    button.setAlpha(1 - slideOffset);
                    cardSearch.setAlpha(1-slideOffset);
                    cardButton.setAlpha(1-slideOffset);
                }
                else
                {
                    sparkSearch.setAlpha(1);
                    button.setAlpha(1);
                    cardSearch.setAlpha(1);
                    cardButton.setAlpha(1);
                }
                googleMap.moveCamera(cameraUpdate);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState)
            {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED)
                {
                    button.setEnabled(false);
                    sparkSearch.setEnabled(false);
                    button.setVisibility(View.GONE);
                    sparkSearch.setVisibility(View.GONE);
                    cardButton.setVisibility(View.GONE);
                    cardSearch.setVisibility(View.GONE);
                }
                else if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED && detailFragment != null)
                {
                    slidingUpPanelLayout.setPanelHeight(detailFragment.heightr);
                }
                else if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED && detailFragment == null)
                {
                    slidingUpPanelLayout.setPanelHeight(thePanelHeight);
                }
                else
                {
                    button.setEnabled(true);
                    sparkSearch.setEnabled(true);
                    button.setVisibility(View.VISIBLE);
                    sparkSearch.setVisibility(View.VISIBLE);
                    cardButton.setVisibility(View.VISIBLE);
                    cardSearch.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    @Override
    public void onBackPressed()
    {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        else if(detailFragment != null && slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED)
        {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(detailFragment).commit();
            detailFragment = null;
            linearLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(new LatLng(mapFragment.mCurrentLocation.getLatitude(),
                            mapFragment.mCurrentLocation.getLongitude()))
                    .zoom(16f).bearing(0.0f).tilt(0.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
        }
        else
            super.onBackPressed();
    }

    @Override
    public void onResult(LocationSettingsResult result)
    {
        final Status status = result.getStatus();
        switch (status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:
                mapFragment.mGoogleApiClient.connect();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try
                {
                    status.startResolutionForResult(this, 101);
                }
                catch (IntentSender.SendIntentException e)
                {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(this,"Settings change unavailable. You might have to reinstall the app.",Toast.LENGTH_LONG);
                break;
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                if(!(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(this,"Location permission denied. The app won't function properly.",
                            Toast.LENGTH_LONG).show();
                else
                {
                    buildGoogleApiClient();
                    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        mapFragment.mGoogleApiClient.connect();
                }
                return;

            case 101:
                return;

            default:
                return;
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(mapFragment.mGoogleApiClient != null)
            mapFragment.mGoogleApiClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(mapFragment.mGoogleApiClient != null && mapFragment.mGoogleApiClient.isConnected())
            mapFragment.mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(this, "Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        try
        {
            mapFragment.mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mapFragment.mGoogleApiClient);
            if (mapFragment.mCurrentLocation == null)
            {
                mapFragment.mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(1000);
                LocationServices.FusedLocationApi.requestLocationUpdates(mapFragment.mGoogleApiClient,
                        mapFragment.mLocationRequest, this);
            }
            else
            {
                mapFragment.initCamera(mapFragment.mCurrentLocation);
            }
        }

        catch(SecurityException e) {}

    }

    protected synchronized void buildGoogleApiClient()
    {
        mapFragment.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(mapFragment.mCurrentLocation == null)
        {
            mapFragment.mCurrentLocation = location;
            mapFragment.initCamera(mapFragment.mCurrentLocation);
        }
        else
            mapFragment.mCurrentLocation=location;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case 500:
                mapFragment.mGoogleApiClient.connect();
        }

    }

}
