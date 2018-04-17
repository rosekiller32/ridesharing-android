package org.beiwe.app.ui.user;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.beiwe.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends BaseMapsActivity {

    @BindView(R.id.rootFrame)
    FrameLayout rootFrame;

    @BindView(R.id.rootll)
    LinearLayout rootll;

    //@BindView(R.id.viewPager)
    //ViewPager viewPager;

    @BindView(R.id.rlwhere)
    RelativeLayout rlWhere;

    @BindView(R.id.ivHome)
    ImageView ivHome;

    @BindView(R.id.tvWhereTo)
    TextView tvWhereto;

    @BindView(R.id.ivMenu)
    ImageView menu;

    public static LatLng from;
    public static LatLng to;
    ArgbEvaluator argbEvaluator;

    private LatLng destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        getSupportActionBar().hide();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        argbEvaluator = new ArgbEvaluator();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int devHeight = displayMetrics.heightPixels;
        int devWidth = displayMetrics.widthPixels;

        //setUpPagerAdapter();
        //viewPager.setClipToPadding(false);
        //viewPager.setPageMargin(-devWidth / 2);

        //viewPager.addOnPageChangeListener(pageChangeListener);
        //viewPager.setPageTransformer(true, pageTransformer);


    }

    /*ViewPager.PageTransformer pageTransformer = new ViewPager.PageTransformer() {
        @Override
        public void transformPage(View page, float position) {
            if (position < -1) { // [-Infinity,-1)


            } else if (position <= 1) { // [-1,1]
                if (position >= -1 && position < 0) {
                    rideRequestButton = (RideRequestButton) page.findViewById(R.id.bRideRequest);
                } else if (position >= 0 && position <= 1) {
                    TextView uberPreTv = (TextView) page.findViewById(R.id.tvuberPre);
                    LinearLayout uberPre = (LinearLayout) page.findViewById(R.id.llUberPre);

                    if (uberPreTv != null && uberPre != null) {

                        uberPreTv.setTextColor((Integer) new ArgbEvaluator().evaluate((1 - position), getResources().getColor(R.color.grey)
                                , getResources().getColor(R.color.black)));

                        uberPreTv.setTextSize(12 + 4 * (1 - position));
                        uberPre.setX(uberPre.getLeft() + (page.getWidth() * (position)));
                    }
                }

            }
        }
    };*/


    /*ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };*/

    @OnClick(R.id.ivHome)
    void showViewPagerWithTransition() {

        //TransitionManager.beginDelayedTransition(rootFrame);
        //viewPager.setVisibility(View.VISIBLE);
        //ivHome.setVisibility(View.INVISIBLE);
        //rlWhere.setVisibility(View.INVISIBLE);

        //mMap.setPadding(0, 0, 0, viewPager.getHeight());
        Intent i = new Intent(this, DirectionsActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.ivMenu)
    void showMenu() {

        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.ivMenu));
        popupMenu.getMenu().add(1, R.id.main_menu_call_clinician, 1, R.string.default_call_clinician_text);
        popupMenu.getMenu().add(1, R.id.menu_call_research_assistant, 2, R.string.call_research_assistant);
        popupMenu.getMenu().add(1, R.id.menu_about,3, R.string.menu_about);
        popupMenu.getMenu().add(1, R.id.menu_change_password, 4, R.string.menu_change_password);
        popupMenu.getMenu().add(1, R.id.view_survey_answers,5, R.string.view_survey_answers);
        popupMenu.getMenu().add(1, R.id.menu_signout,6, R.string.menu_sign_out);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return MapsActivity.super.onOptionsItemSelected(item);
            }
        });

        popupMenu.show();

    }

    @OnClick(R.id.rlwhere)
    void openPlacesView() {
        openPlaceAutoCompleteView();
    }

    void startRevealAnimation() {

        int cx = rootFrame.getMeasuredWidth() / 2;
        int cy = rootFrame.getMeasuredHeight() / 2;

        Animator anim = ViewAnimationUtils.createCircularReveal(rootll, cx, cy, 50, rootFrame.getWidth());

        anim.setDuration(500);
        anim.setInterpolator(new AccelerateInterpolator(2));
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                rlWhere.setVisibility(View.VISIBLE);
                //ivHome.setVisibility(View.VISIBLE);
            }
        });

        anim.start();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        rlWhere.setVisibility(View.VISIBLE);
        //startRevealAnimation();


    }

    @Override
    protected void setUpPolyLine(Place place) {
        if (place != null) {
            tvWhereto.setText(place.getName().toString());
        }
        else {
            tvWhereto.setText("Where to?");
        }
        ivHome.setVisibility(View.VISIBLE);
        LatLng source = new LatLng(getUserLocation().getLatitude(), getUserLocation().getLongitude());
        LatLng destination = getDestinationLatLong();
        from = source;
        to = place.getLatLng();
        /*if (source != null && destination != null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            getPolyline polyline = retrofit.create(getPolyline.class);

            polyline.getPolylineData(source.latitude + "," + source.longitude, destination.latitude + "," + destination.longitude)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                            JsonObject gson = new JsonParser().parse(response.body().toString()).getAsJsonObject();

                            if (gson == null) {
                                return;
                            }

                            try {

                                Single.just(parse(new JSONObject(gson.toString())))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<List<List<HashMap<String, String>>>>() {
                                            @Override
                                            public void accept(List<List<HashMap<String, String>>> lists) throws Exception {

                                                drawPolyline(lists);
                                            }
                                        });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {

                        }
                    });
        } else
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();*/
    }

    /*private void setUpPagerAdapter() {

        List<Integer> data = Arrays.asList(0, 1);
        CarsPagerAdapter adapter = new CarsPagerAdapter(data);
        viewPager.setAdapter(adapter);
    }*/

    @Override
    public void onBackPressed() {

        if (ivHome.getVisibility() == View.VISIBLE) {

            TransitionManager.beginDelayedTransition(rootFrame);
            //viewPager.setVisibility(View.INVISIBLE);
            mMap.setPadding(0, 0, 0, 0);
            ivHome.setVisibility(View.INVISIBLE);
            rlWhere.setVisibility(View.VISIBLE);
            tvWhereto.setText("Where to?");
            mMap.clear();
            return;
        }
        finish();
    }


}
