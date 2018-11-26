package xyz.zerobell.loonaclock;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class LockScreen extends AppCompatActivity {

    private ImageView iv;
    private Bitmap[] logo_arr;
    private int img_i = 0;
    private Handler handler;
    private Animator animator;
    private Timer timer;

    public Typeface typeFace = null;

    public int year, month, day, hour, minute;

    private TextView timeView;
    private TextView dateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        typeFace = Typeface.createFromAsset(this.getAssets(), "fonts/PoiretOne-Regular.ttf");
        dateView = findViewById(R.id.dateView);
        timeView = findViewById(R.id.timeView);
        dateView.setTypeface(typeFace);
        timeView.setTypeface(typeFace);

        Intent intent = new Intent(
                getApplicationContext(),//현재제어권자
                OnLock_Service.class); // 이동할 컴포넌트
        startService(intent);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOption = getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            uiOption ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            uiOption ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOption ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOption);

        iv = (ImageView)findViewById(R.id.logoView);

        handler = new Handler();

        logo_arr = OnLock_Service.getBitmap(this);

        Calendar cal = new GregorianCalendar();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR);
        minute = cal.get(Calendar.MINUTE);

        timer = new Timer();
        timer.start();

        timeView.setText(String.format("%02d:%02d", hour, minute));
        dateView.setText(String.format("%04d-%02d-%02d", year, month, day));

        img_i = 0;
        iv.setImageBitmap(logo_arr[img_i]);
        FrameLayout mainLayout = (FrameLayout)findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            float x = -1;
            float y = -1;
            float dx, dy;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (animator!=null) {

                        }
                        else {
                            img_i = 0;
                            iv.setImageBitmap(logo_arr[img_i]);
                            x = motionEvent.getX();
                            y = motionEvent.getY();
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        Log.d("Loona", "x : " + x + " y : " + y + " dx : " + dx + " dy : " + dy);
                        dx = motionEvent.getX() - x;
                        dy = motionEvent.getY() - y;
                        changeLogo((dx*dx + dy*dy));
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (animator!=null) {
                            try {
                                animator.terminate();
                                timer.terminate();
                                timer = null;
                                animator = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                        else {
                            dx = motionEvent.getX() - x;
                            dy = motionEvent.getY() - y;
                            x = -1;
                            y = -1;

                            if ((dx * dx + dy * dy) > 250000) {
                                startAnimation();
                            } else {
                                img_i = 0;
                                iv.setImageBitmap(logo_arr[img_i]);
                            }
                        }
                        break;


                }
                return false;
            }
        });



    }

    public int getRawResIdByName(String resName) {
        String pkgName = this.getPackageName();
        // Return 0 if not found.
        int resID = this.getResources().getIdentifier(resName, "raw", pkgName);
        Log.i("AndroidVideoView", "Res Name: " + resName + "==> Res ID = " + resID);
        return resID;
    }

    public void changeLogo(float value) {
        img_i = value > 250000 ? 18 : (int)(value * 0.000072);
        iv.setImageBitmap(logo_arr[img_i]);
    }

    public void startAnimation() {
        if (animator == null) animator = new Animator();
        animator.start();
    }

    class Animator extends Thread {
        private boolean runnable = true;
        @Override
        public void run() {
            while (img_i < 67) {
                if (runnable) {
                    try {
                        img_i++;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    iv.setImageBitmap(logo_arr[img_i]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        sleep(41);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                if (timer != null) {
                    timer.terminate();
                    timer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }

        public void terminate() {
            runnable = false;
        }
    }

    class Timer extends Thread {
        private boolean runnable = true;
        @Override
        public void run() {
            while (runnable) {
                try {
                    refreshTime();
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void terminate() {
            runnable = false;
        }
    }

    public void refreshTime() {
        Calendar cal = new GregorianCalendar();
        if (minute != cal.get(Calendar.MINUTE)) {
            hour = cal.get(Calendar.HOUR);
            minute = cal.get(Calendar.MINUTE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        timeView.setText(String.format("%02d:%02d", hour, minute));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (day != cal.get(Calendar.DAY_OF_MONTH)) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
            day = cal.get(Calendar.DAY_OF_MONTH);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        dateView.setText(String.format("%04d-%02d-%02d", year, month, day));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (timer != null) {
                timer.terminate();
                timer = null;
            }
            if (animator != null) {
                animator.terminate();
                animator = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (timer == null) {
                timer = new Timer();
                timer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
