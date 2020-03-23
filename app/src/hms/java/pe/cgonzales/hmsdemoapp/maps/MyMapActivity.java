package pe.cgonzales.hmsdemoapp.maps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pe.cgonzales.hmsdemoapp.R;

public class MyMapActivity extends FragmentActivity implements OnMapReadyCallback,
        HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 10;

    private HuaweiMap mMap;

    @Nullable
    private HuaweiApiClient mGoogleApiClient;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private Marker mCurrentLocationMarker;

    private FusedLocationProviderClient fusedLocationProvider;

    private LocationCallback locationCallback;

    private EditText mAutoCompleteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //checkLocationPermission(this);

        buildGoogleApiClient();
        mAutoCompleteView = findViewById(R.id.autoCompleteView);
        mAutoCompleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAutoComplete();
            }
        });
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
    }

    private void launchAutoComplete() {
        try {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            // Start the autocomplete intent.
            Autocomplete.IntentBuilder intentBuilder = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields
            );
            startActivityForResult(intentBuilder.build(this), PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (Exception e) {
            Log.e("Places", e.getLocalizedMessage());
        }
    }

    private void buildGoogleApiClient() {
        // Create a GoogleApiClient instance
        mGoogleApiClient = new HuaweiApiClient.Builder(this)
                //.addApi(LocationServices.API)
                .addConnectionCallbacks(this) // TODO added line
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connectForeground();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(HuaweiMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(new HuaweiMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng centerOfMap = mMap.getCameraPosition().target;
                onNewCamera(centerOfMap.latitude, centerOfMap.longitude);
            }
        });

        if (checkLocationPermission(this)) {

        }
    }

    protected void onNewCamera(double latitude, double longitude) {
        mAutoCompleteView.setText(
                new LocationUtils().getCompleteAddress(this, latitude, longitude)
        );
    }

    public static boolean checkLocationPermission(final Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle("R.string.title_location_permission")
                        .setMessage("R.string.text_location_permission")
                        .setPositiveButton("R.string.ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Request location updates:
                }

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (mGoogleApiClient != null && !mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connectForeground();
                }
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onSuccessAutocompletePlace(data);
            }
        }
    }

    private void onSuccessAutocompletePlace(Intent data) {
        Place place = Autocomplete.getPlaceFromIntent(data);
        mAutoCompleteView.setText(place.getName());
        LatLng latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        //mLatitude = latLng!!.latitude
        //mLongitude = latLng.longitude
    }

    // region GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProvider = LocationServices
                .getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        fusedLocationProvider.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.myLooper()
        );


//        FusedLocationProviderApi oldFusedLocationApi = LocationServices.FusedLocationApi;
//        oldFusedLocationApi.requestLocationUpdates(mGoogleApiClient,
//                mLocationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    // endregion

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                location.getLatitude() + "," +
                location.getLongitude();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .title("My Current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        if(mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }
        mCurrentLocationMarker = mMap.addMarker(markerOptions);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        fusedLocationProvider.removeLocationUpdates(locationCallback);

//        oldFusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationCallback);
    }

    // region GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                if(mGoogleApiClient != null) {
                    mGoogleApiClient.connectForeground();
                }
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            //showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }
    // endregion

    // region Error dialog
    /* Creates a dialog for an error message */
//    private void showErrorDialog(int errorCode) {
//        // Create a fragment for the error dialog
//        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
//        // Pass the error that should be displayed
//        Bundle args = new Bundle();
//        args.putInt(DIALOG_ERROR, errorCode);
//        dialogFragment.setArguments(args);
//        dialogFragment.show(getSupportFragmentManager(), "errordialog");
//    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
//    public void onDialogDismissed() {
//        mResolvingError = false;
//    }

    /* A fragment to display an error dialog */
//    public static class ErrorDialogFragment extends DialogFragment {
//        public ErrorDialogFragment() { }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // Get the error code and retrieve the appropriate dialog
//            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
//            return GooglePlayServicesUtil.getErrorDialog(errorCode,
//                    this.getActivity(), REQUEST_RESOLVE_ERROR);
//        }
//
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            ((MyMapActivity)getActivity()).onDialogDismissed();
//        }
//    }
    // endregion
}

