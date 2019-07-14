package com.jiowhere;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class GetNearBY extends AsyncTask<Object, String, String> {
    private DatabaseReference mref;
    private static int v=0;
    Context context;
    GetNearBY(Context context){
        this.context=context;
    }
    private GoogleMap googleMap;
    private String url;
    private InputStream is;
    private BufferedReader br;
    private StringBuilder stringBuilder;
    private  String data;


    @Override
    protected String doInBackground(Object... objects) {
        googleMap=(GoogleMap) objects[0];
        url=(String) objects[1];


        try {
            URL myUrl=new URL(url);
            HttpURLConnection httpURLConnection=(HttpURLConnection)myUrl.openConnection();
            httpURLConnection.connect();
            is=httpURLConnection.getInputStream();
            br=new BufferedReader(new InputStreamReader(is));
            String line="";
            stringBuilder=new StringBuilder();
            while((line=br.readLine())!=null){
                stringBuilder.append(line);
            }
            data=stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject parentObject=new JSONObject(s);
            JSONArray resultArray=parentObject.getJSONArray("results");
            v=resultArray.length();
            for (int i=0;i<resultArray.length();i++) {

                JSONObject jsonObject=resultArray.getJSONObject(i);
                JSONObject locationObj=jsonObject.getJSONObject("geometry").getJSONObject("location");
                String lat=locationObj.getString("lat");
                String lng=locationObj.getString("lng");
                JSONObject nameobj=resultArray.getJSONObject(i);
                String name_res=nameobj.getString("name");
                String vicinity=nameobj.getString("vicinity");
                String icon=nameobj.getString("icon");


                // JSONArray photoArray=jsonObject.getJSONArray("photos");
                //JSONObject photoObj=photoArray.getJSONObject(i);
                //String ref=photoObj.getString("photo_reference");
                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                mref= FirebaseDatabase.getInstance().getReference();
                HashMap<String, String> dataMap= new HashMap<>();
                dataMap.put("device_token",deviceToken);
                dataMap.put("icon",icon);
                dataMap.put("name",name_res);
                dataMap.put("vicinity",vicinity);
                mref.child("data").child(deviceToken).child(String.valueOf(i)).setValue(dataMap);
                HashMap<String, Integer> d=new HashMap<>();
                d.put("results",resultArray.length());
                mref.child("data").child(deviceToken).child("res").setValue(d);


                LatLng latLng=new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                CameraUpdate update= CameraUpdateFactory.newLatLngZoom(latLng,11);
                googleMap.animateCamera(update);

                MarkerOptions markerOptions=new MarkerOptions();
                markerOptions.title(name_res+" , "+vicinity);
                markerOptions.position(latLng);
                googleMap.addMarker(markerOptions);
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(final Marker marker) {
                        CharSequence options[] = new CharSequence[]{
                                "YES", "NO"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Suggest this place to nearby friends?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    double lat=marker.getPosition().latitude;
                                    double lng=marker.getPosition().longitude;
                                    Intent friends=new Intent(context,FriendsActivity.class);
                                    friends.putExtra("lat",String.valueOf(lat));
                                    friends.putExtra("lng",String.valueOf(lng));
                                    context.startActivity(friends);
                                }else{

                                }
                            }
                        });
                        builder.show();

                    }
                });
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                       /* CharSequence options[] = new CharSequence[]{
                                "YES", "NO"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Suggest this place to nearby friends?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    double lat=marker.getPosition().latitude;
                                    double lng=marker.getPosition().longitude;
                                    Intent friends=new Intent(context,FriendsActivity.class);
                                    friends.putExtra("lat",String.valueOf(lat));
                                    friends.putExtra("lng",String.valueOf(lng));
                                    context.startActivity(friends);
                                }else{

                                }
                            }
                        });
                        builder.show();*/
                        return false;
                    }
                });


                // Toast.makeText(context, name_res+vicinity, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
