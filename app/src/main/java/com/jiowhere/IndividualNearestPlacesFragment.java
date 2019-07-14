package com.jiowhere;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class IndividualNearestPlacesFragment extends Fragment implements OnMapReadyCallback,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

    private DatabaseReference dbref;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    private String lat, lng;
    private TextView suggestedPlaceText;
    private View view;
    private MapView mMapViews;
    private GoogleMap gMaps;
    private GoogleApiClient clients;
    private LocationRequest requests;
    private LatLng latlng;
    private String myLat, myLng;
    private LocationListener location;
    private AdView mAdView;


    public IndividualNearestPlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_individual_nearest_places, container, false);
        mMapViews = (MapView) view.findViewById(R.id.sug_mapView);
        mMapViews.onCreate(savedInstanceState);
        MobileAds.initialize(getContext(), "ca-app-pub-6373781499552576~6645798312");
        mAdView = view.findViewById(R.id.sug_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        pd=new ProgressDialog(getContext());
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Loading...");
        pd.show();
        //mMapViews.onResume(); // needed to get the map to display immediately

        mMapViews.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            dbref.addValueEventListener
                    (new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            FirebaseUser user=mAuth.getCurrentUser();
                            if (user != null) {
                            if (dataSnapshot.child(mAuth.getCurrentUser().getUid()).
                                    child("suggested").exists()) {
                                    lat = dataSnapshot.child(mAuth.getCurrentUser().getUid())
                                            .child("suggested").child("lat").getValue().toString();
                                    lng = dataSnapshot.child(mAuth.getCurrentUser().getUid())
                                            .child("suggested").child("lng").getValue().toString();

                                    myLat = dataSnapshot.child("users").child(mAuth.getCurrentUser().getUid())
                                            .child("lat").getValue().toString();
                                    myLng = dataSnapshot.child("users").child(mAuth.getCurrentUser().getUid())
                                            .child("lng").getValue().toString();


                                    Double result;
                                    int Radius = 6371;// radius of earth in Km
                                    double lat1 = Double.valueOf(lat);
                                    double lat2 = Double.valueOf(myLat);
                                    double lon1 = Double.valueOf(lng);
                                    double lon2 = Double.valueOf(myLng);
                                    double dLat = Math.toRadians(lat2 - lat1);
                                    double dLon = Math.toRadians(lon2 - lon1);
                                    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                            + Math.cos(Math.toRadians(lat1))
                                            * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                                            * Math.sin(dLon / 2);
                                    double c = 2 * Math.asin(Math.sqrt(a));
                                    double valueResult = Radius * c;
                                    double km = valueResult / 1;
                                    DecimalFormat newFormat = new DecimalFormat("#.###");
                                    // int kmInDec = Integer.valueOf(newFormat.format(km));
                                    double meter = valueResult % 1000;
                                    //int meterInDec = Integer.valueOf(newFormat.format(meter));
                                    //return Radius * c;
                                    newFormat.setRoundingMode(RoundingMode.CEILING);
                                    result = Double.valueOf(newFormat.format(valueResult));

                                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                    List<Address> addresses = null;
                                    try {
                                        addresses = geocoder.getFromLocation(Double.valueOf(lat),
                                                Double.valueOf(lng), 1);
                                        String area = addresses.get(0).getSubLocality();
                                        String province = addresses.get(0).getAdminArea();
                                        String street = addresses.get(0).getFeatureName();
                                        String city = addresses.get(0).getLocality();
                                        String countryName = addresses.get(0).getCountryName();
                                        //////////// markers //////////
                                        latlng = new LatLng(Double.valueOf(lat),
                                                Double.valueOf(lng));

                                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 15);
                                        gMaps.animateCamera(update);

                                        final MarkerOptions option = new MarkerOptions();
                                        option.position(latlng);
                                        if (street == null) {
                                            street = "";
                                        } else if (area == null) {
                                            area = "";
                                        } else if (province == null) {
                                            province = "";
                                        } else if (city == null) {
                                            city = "";
                                        } else if (countryName == null) {
                                            countryName = "";
                                        }

                                        option.title(street + ", " + area + ", " + province + ", "
                                                + city + ", " + countryName);
                                        option.snippet(result + " Km away");

                                        gMaps.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                            @Override
                                            public View getInfoWindow(Marker arg0) {
                                                return null;
                                            }

                                            @Override
                                            public View getInfoContents(Marker marker) {

                                                LinearLayout info = new LinearLayout(getContext());
                                                info.setOrientation(LinearLayout.VERTICAL);

                                                TextView title = new TextView(getContext());
                                                title.setTextColor(Color.BLACK);
                                                title.setGravity(Gravity.CENTER);
                                                title.setTypeface(Typeface.DEFAULT_BOLD);
                                                title.setText(marker.getTitle());

                                                TextView snippet = new TextView(getContext());
                                                snippet.setTextColor(Color.GRAY);
                                                snippet.setGravity(Gravity.CENTER);
                                                snippet.setText(marker.getSnippet());

                                                info.addView(title);
                                                info.addView(snippet);

                                                return info;
                                            }
                                        });

                                        gMaps.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                            @Override
                                            public void onInfoWindowClick(final Marker marker) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "YES", "NO"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Suggest this place to nearby friends?");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which==0){
                                                            dbref.child(mAuth.getCurrentUser().getUid())
                                                                    .child("suggested").removeValue();
                                                            marker.setVisible(false);
                                                        }else{

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                        gMaps.addMarker(option);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    //Toast.makeText(getContext(), "No suggested Place", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } catch (Exception e) {
            //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requests = new LocationRequest().create();
        requests.setInterval(1000000);
        requests.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(clients, requests, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMaps = googleMap;

        clients = new GoogleApiClient.Builder(getContext()).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        clients.connect();
        pd.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapViews.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapViews.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapViews.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapViews.onLowMemory();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
