package com.jiowhere;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements OnMapReadyCallback,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

    private MapView mMapView;
    private GoogleMap gMap;
    private GoogleApiClient client;
    private LocationRequest request;
    private LatLng latlng;
    private DatabaseReference dbref;
    private FirebaseAuth mAuth;
    private ArrayList<LocationModel> locationList;
    private ArrayList<Users> userList;
    private Location location;
    private Button findBtn, suggestPlacesBtn;
    int height = 100;
    int width = 100;
    private Bitmap myBitmap, bm;
    private String imgUrl, place;
    private Spinner placesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        dbref = FirebaseDatabase.getInstance().getReference();

        locationList = new ArrayList<LocationModel>();
        userList = new ArrayList<Users>();


        //mMapView.onResume(); // needed to get the map to display immediately
        suggestPlacesBtn = rootView.findViewById(R.id.suggested_find_btn);
        suggestPlacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestedPlace();
            }
        });
        placesList = rootView.findViewById(R.id.places_list);
        initspinnerfooter();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        client = new GoogleApiClient.Builder(getContext()).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(final Location location) {
        gMap.clear();
        if (location == null) {
            Toast.makeText(getContext(), "location not found", Toast.LENGTH_SHORT).show();
        } else {
            latlng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 15);
            gMap.animateCamera(update);
            final MarkerOptions option = new MarkerOptions();
            option.position(latlng);
            //option.snippet("snippet");
            option.title("Your location");
            gMap.addMarker(option);
            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Toast.makeText(getContext(), "Your location", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            final String lat, lng;
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());

            final HashMap<String, Object> locationMap = new HashMap<>();
            locationMap.put("lat", lat);
            locationMap.put("lng", lng);

            try {
                dbref.child("users").child(mAuth.getCurrentUser().getUid()).
                        updateChildren(locationMap);
            }catch (Exception e){
                //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }

            dbref.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        LocationModel l = dataSnapshot1.getValue(LocationModel.class);
                        Users u = dataSnapshot1.getValue(Users.class);

                     //   Toast.makeText(getContext(), l.getLat() + ":" + l.getLng(), Toast.LENGTH_SHORT).show();
                        locationList.add(l);
                        userList.add(u);
                        final double result[] = new double[locationList.size()];

                        for (int i = 0; i < locationList.size(); i++) {
                            try {
                                int Radius = 6371;// radius of earth in Km
                                double lat1 = location.getLatitude();
                                double lat2 = Double.parseDouble(locationList.get(i).getLat());
                                double lon1 = location.getLongitude();
                                double lon2 = Double.parseDouble(locationList.get(i).getLng());
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
                                String userId[] = new String[locationList.size()];
                                String image[] = new String[locationList.size()];

                                userId[i] = userList.get(i).getUserId();
                                image[i] = userList.get(i).getImage();
                                result[i] = Double.valueOf(newFormat.format(valueResult));
                                try {
                                    createMarker(
                                            locationList.get(i).getLat(),
                                            locationList.get(i).getLng(),
                                            result[i],
                                            userId[i],
                                            image[i]
                                    );
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                               // Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    protected void createMarker(String latitude, String longitude, final double v,
                                final String userId, String image) throws IOException {
        final double lat, lng;
        lat = Double.parseDouble(latitude);
        lng = Double.parseDouble(longitude);

        Glide.with(getContext())
                .load(image).asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        final float scale = getContext().getResources().getDisplayMetrics().density;
                        int pixels = (int) (50 * scale + 0.5f);
                        Bitmap bitmap = Bitmap.createScaledBitmap(resource, pixels, pixels, true);

                        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                        Bitmap bmp = Bitmap.createBitmap(250, 220, conf);
                        Canvas canvas1 = new Canvas(bmp);
                        Paint color = new Paint();
                        canvas1.drawRGB(244, 66, 95);
                        color.setColor(Color.WHITE);
                        color.setStrokeWidth(0);
                        color.setTextSize(40);
                        canvas1.drawText(v + " km", 5, 30, color);
                        canvas1.drawBitmap(bitmap, 25,
                                50,
                                color);
                        try {
                            gMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title("")
                                    .snippet(userId)
                                    .anchor(0.5f, 0.5f)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                            );

                        } catch (Exception e) {
                            //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                })

                .centerCrop()
                .preload();

      /* gMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .snippet(userId)
                .title(v + "km away")
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(myBitmap))
        );*/
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker.getTitle().equals("Your location")){
                    Toast.makeText(getContext(), "Your location", Toast.LENGTH_SHORT).show();
                }else{
                String userid = marker.getSnippet();
                Intent chat = new Intent(getContext(), ChatActivity.class);
                chat.putExtra("userid", userid);
                startActivity(chat);
                //getActivity().finish();
                }
                return false;
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setInterval(1000000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void find(String places) {
        gMap.clear();

        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + latlng.latitude + "," + latlng.longitude);
        stringBuilder.append("&radius=" + 1500);
        stringBuilder.append("&keyword=" + places);

        stringBuilder.append("&key=" + getResources().getString(R.string.place_api));
        String url = stringBuilder.toString();
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = gMap;
        dataTransfer[1] = url;

        GetNearBY getNearBY = new GetNearBY(getContext());
        getNearBY.execute(dataTransfer);
    }

    private void initspinnerfooter() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.places));
        placesList.setAdapter(adapter);
        placesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               try{
                   find(parent.getItemAtPosition(position).toString());
               }catch (Exception e){
                  // Toast.makeText(getContext(),e.toString(), Toast.LENGTH_SHORT).show();
               }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
    public void suggestedPlace(){
        gMap.clear();
            dbref.child("users").child(mAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("suggested").exists()){
                    String lat=dataSnapshot.child("suggested").child("lat").getValue().toString();
                    String lng=dataSnapshot.child("suggested").child("lng").getValue().toString();
                    double la=Double.parseDouble(lat);
                    double ln=Double.parseDouble(lng);
                    latlng=new LatLng(la,ln);

                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 15);
                    gMap.animateCamera(update);
                    final MarkerOptions option = new MarkerOptions();
                    option.position(latlng);
                    option.snippet("snippet");
                    option.title("title");
                    gMap.addMarker(option);
                    gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            Toast.makeText(getContext(), "Suggested place", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });
                }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
}