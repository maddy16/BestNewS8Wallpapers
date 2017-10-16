package com.sizzling.apps.bests8wallpapers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    boolean isZoomed;
    private Animator mCurrentAnimator;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 110;
    int zoomedImage = -1;
    int adCount = 0;
    static NavigationActivity instance;
    String currentCategory;
    private AdView mAdView;
    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;
    RecyclerView recyclerView;
    int selectedImage;
    //    ImageView expandedImageView;
    ViewPager slider;
    static HashMap<String, ArrayList<Integer>> listsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((App) getApplication()).loadAds();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateApp();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mAdView = (AdView) findViewById(R.id.bannerAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        isZoomed = false;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

//        expandedImageView = (ImageView) findViewById(R.id.zoomedImage);
        slider = (ViewPager) findViewById(R.id.image_slider);
        slider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedImage = listsMap.get(currentCategory).get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        instance = this;
        //recyclerView.setHasFixedSize(true);

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        prepareData();
        setAdapterByKey("abstract");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            if(expandedImageView.getVisibility()==View.VISIBLE){
//                expandedImageView.performClick();
//            } else {
//                super.onBackPressed();
//            }
            if (slider.getVisibility() == View.VISIBLE) {
                slider.performClick();
            } else {
                super.onBackPressed();
            }
        }
    }

    void setAdapterByKey(String key) {
        currentCategory = key;
        getSupportActionBar().setTitle(key.substring(0, 1).toUpperCase() + key.substring(1) + " Wallpapers");
//        if(expandedImageView.getVisibility()==View.VISIBLE) {
//            expandedImageView.performClick();
//        }
        if (slider.getVisibility() == View.VISIBLE) {
            slider.performClick();
        }
        MyAdapter adapter = new MyAdapter(getApplicationContext(), listsMap.get(key));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        menu.findItem(R.id.action_share).setVisible(isZoomed);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            shareImg();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String title = item.getTitle().toString().toLowerCase();
        if (title.contains(" "))
            title = title.split(" ")[0];
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        setAdapterByKey(title);
        return true;
    }

    public void prepareData() {
        listsMap = new HashMap<>();
        PopupMenu p = new PopupMenu(this, null);
        Menu menu = p.getMenu();
        getMenuInflater().inflate(R.menu.activity_navigation_drawer, menu);
        for (int i = 0; i < menu.size(); i++) {
            String title = menu.getItem(i).getTitle().toString();
            if (title.contains(" ")) {
                title = title.split(" ")[0];
            }
            Log.d("MADDY", title.toLowerCase().trim() + ">");
            listsMap.put(title.toLowerCase(), new ArrayList<Integer>());
        }
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.startsWith("wallp")) {
                String[] words = name.split("_");
                String key = words[1].toLowerCase();
                if (key.contains(" ")) {
                    key = key.split(" ")[0];
                }
//                Log.d("MADDY", key);
                listsMap.get(key.trim()).add(getResources().getIdentifier(field.getName(), "drawable", getPackageName()));
            }
        }
    }

    public static class ImageZoomer implements View.OnClickListener {

        int imageId;
        int currentItem;

        public ImageZoomer(int imageId, int currentItem) {
            this.imageId = imageId;
            this.currentItem = currentItem;
        }

        @Override
        public void onClick(View v) {
            instance.selectedImage = imageId;
            instance.zoomImageFromThumb(v, imageId, currentItem);
        }
    }

    void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }


    }

    void setWallpaper() {
        final ProgressBar pb = (ProgressBar) findViewById(R.id.wallpaperSpinner);
        final Button btn = (Button) findViewById(R.id.setWBtn);
        btn.setVisibility(View.VISIBLE);

        AsyncTask task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                pb.setVisibility(View.VISIBLE);
                btn.setVisibility(View.GONE);
            }

            @Override
            protected Object doInBackground(Object[] params) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(NavigationActivity.this);
                Drawable drawable = getResources().getDrawable(selectedImage);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                try {
                    wallpaperManager.setBitmap(bitmap);
                    return true;

                } catch (IOException e) {
                    e.printStackTrace();

                }
                return false;
            }

            @Override
            protected void onPostExecute(Object o) {
                pb.setVisibility(View.INVISIBLE);
                btn.setVisibility(View.VISIBLE);
                Boolean flag = (Boolean) o;
                if(flag) {
                    Toast.makeText(NavigationActivity.this, "Wallpaper Set Successfully.", Toast.LENGTH_SHORT).show();
                    InterstitialAd fullScreenAd = App.instance.getFullScreenAd();
                    if (fullScreenAd.isLoaded()) {
                        fullScreenAd.show();
                    } else {
                        Log.d("MADDY", "Interstitial Not Loaded");
                        App.instance.requestNewInterstitial();
//                    App.instance.countIntAd--;
                    }
                }
                else
                    Toast.makeText(NavigationActivity.this, "Failed to Set Wallpaper.", Toast.LENGTH_SHORT).show();
            }
        };
        task.execute();
    }

    void zoomImageFromThumb(final View thumbView, final int imageResId, int currentItem) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        recyclerView.setVisibility(View.INVISIBLE);

        // Load the high-resolution "zoomed-in" image.

        isZoomed = true;
        invalidateOptionsMenu();
        ImageAdapter adapter = new ImageAdapter(this, listsMap.get(currentCategory));
        slider.setAdapter(adapter);
        slider.setCurrentItem(currentItem);
//        expandedImageView.setImageResource(imageResId);
        Button btn = (Button) findViewById(R.id.setWBtn);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setWallpaper();
            }
        });
        zoomedImage = imageResId;
        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.content_navigation)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        findViewById(R.id.content_navigation).setBackgroundColor(Color.BLACK);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
//        expandedImageView.setVisibility(View.VISIBLE);
        slider.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
//        expandedImageView.setPivotX(0f);
//        expandedImageView.setPivotY(0f);
        slider.setPivotX(0f);
        slider.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
//        AnimatorSet set = new AnimatorSet();
//        set
//                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
//                        startBounds.left, finalBounds.left))
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
//                        startBounds.top, finalBounds.top))
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
//                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
//                View.SCALE_Y, startScale, 1f));
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(slider, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(slider, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(slider, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(slider,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        slider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }
                recyclerView.setVisibility(View.VISIBLE);
                findViewById(R.id.setWBtn).setVisibility(View.INVISIBLE);
                findViewById(R.id.content_navigation).setBackgroundColor(Color.WHITE);
                isZoomed = false;
                invalidateOptionsMenu();

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
//                AnimatorSet set = new AnimatorSet();
//                set.play(ObjectAnimator
//                        .ofFloat(expandedImageView, View.X, startBounds.left))
//                        .with(ObjectAnimator
//                                .ofFloat(expandedImageView,
//                                        View.Y, startBounds.top))
//                        .with(ObjectAnimator
//                                .ofFloat(expandedImageView,
//                                        View.SCALE_X, startScaleFinal))
//                        .with(ObjectAnimator
//                                .ofFloat(expandedImageView,
//                                        View.SCALE_Y, startScaleFinal));
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(slider, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(slider,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(slider,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(slider,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
//                        expandedImageView.setVisibility(View.GONE);
                        slider.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                        showFullScreenAd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
//                        expandedImageView.setVisibility(View.GONE);
                        slider.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                        showFullScreenAd();
                    }
                });
                set.start();
                mCurrentAnimator = set;

            }
        });
    }

    void showFullScreenAd() {
        if (adCount == 0) {
            InterstitialAd fullScreenAd = App.instance.getFullScreenAd();
            if (fullScreenAd.isLoaded()) {
                fullScreenAd.show();
            } else {
                Log.d("MADDY", "Interstitial Not Loaded");
                App.instance.requestNewInterstitial();
//                    App.instance.countIntAd--;
            }
        }
        adCount++;
        if (adCount == 2)
            adCount = 0;
    }

    public void shareImg() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        } else {
            permissionAllowed();
        }
    }

    @TargetApi(23)
    public void checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            permissionAllowed();
        }
    }

    public void permissionAllowed() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), selectedImage);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/LatestShare.jpg";
        OutputStream out = null;
        File file = new File(path);
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        path = file.getPath();
        Uri bmpUri = Uri.parse("file://" + path);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out new Wallpapers. Live at the Playstore.\nhttps://play.google.com/store/apps/details?id=" + getPackageName());
        shareIntent.setType("image/png");
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionAllowed();
                } else {
                    Toast.makeText(this, "Application Requires Permission for sharing", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

}
