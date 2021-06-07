package whatsnumber1.rargee;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//TODO------------------------------------------------------GAME SCREEN CLASS
public class GameScreen extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "rargee";
    private List<Integer> list = new ArrayList<>();
    private int currentIndex = 0;
    int currentTime = 0;
    private Button yellow, blue, green, red;
    private List<LightPoint> lights;
    private int lightSize = 0;
    private boolean correct = false;
    private CountDownTimer yourCountDownTimer;
    int yellowID, redID, greenID, blueID, winID, loseID;
    GradientDrawable rainbow, rainbowH;
    View rainbowHighlight;
    ActionBar actionBar;
    private int MAXBRI;
    private GetDatFile gdf;

    int hasWonAndCountdownHasntStarted = -1;

    boolean loss = false;
    int curLevel = 0;

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    View currLayout;

    private void stopTimer() {
        if (mTimer1 != null) {
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    private void hideButtons() {
        if (yellow != null)
            yellow.setVisibility(View.GONE);
        if (red != null)
            red.setVisibility(View.GONE);
        if (green != null)
            green.setVisibility(View.GONE);
        if (blue != null)
            blue.setVisibility(View.GONE);
    }

    private void showButtons() {
        if (yellow != null)
            yellow.setVisibility(View.VISIBLE);
        if (red != null)
            red.setVisibility(View.VISIBLE);
        if (green != null)
            green.setVisibility(View.VISIBLE);
        if (blue != null)
            blue.setVisibility(View.VISIBLE);
    }

    private void waitForAnswer() {
        toggleLights();
        stopTimer();

        yellowID = R.raw.efive;
        redID = R.raw.gfive;
        greenID = R.raw.bfour;
        blueID = R.raw.bfive;
        winID = R.raw.win;
        loseID = R.raw.lose;

        startCountdown(false);

    }

    private void startCountdown(final boolean end) {
        hasWonAndCountdownHasntStarted = 0;

        findViewById(R.id.countDown).setVisibility(View.VISIBLE);

        if (end) {
            findViewById(R.id.countDown).setVisibility(View.INVISIBLE);
            hideButtons();
            if (yourCountDownTimer != null)
                yourCountDownTimer.cancel();
            stopTimer();
        } else {
            yourCountDownTimer = new CountDownTimer(11000, 1000) {
                public void onTick(long millisUntilFinished) {
                    String secondsRemaining = String.valueOf(millisUntilFinished / 1000);
                    ((TextView) findViewById(R.id.countDown)).setText(secondsRemaining);

                    if (correct) {
                        yourCountDownTimer.cancel();
                        stopTimer();
                        hideButtons();
                        startTimer();
                        mTimer1.schedule(mTt1, 1, 1000000000);
                    }
                }

                public void onFinish() {

                    if (!correct) {
                        playSound(5);
                        youLose();
                        findViewById(R.id.countDown).setVisibility(View.INVISIBLE);
                        hideButtons();
                        stopTimer();
                    } else {
                        stopTimer();
                        hideButtons();
                        startTimer();
                        mTimer1.schedule(mTt1, 1, 1000000000);
                    }
                }
            }.start();
        }
    }

    private void startTimer() {

        findViewById(R.id.countDown).setVisibility(View.INVISIBLE);

        if (loss) {
            startCountdown(true);
            try {
                playSound(5);
                hideButtons();

                synchronized (this) {
                    wait(1000);
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }

            toggleLights();
            return;
        }

        curLevel++;
        String currLevel = "Level " + String.valueOf(curLevel);
        ((TextView) findViewById(R.id.action_bar_title)).setText(currLevel);
        hideButtons();

        mTimer1 = new Timer(true);
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run() {
                        addRandomToList();
                        iterateThroughColors();
                        waitForAnswer();
                        showButtons();
                    }
                });
            }
        };
    }

    private void playSound(int index) {

        if (MainActivity.soundy) {
            int resID;
            if (index == 0)
                resID = R.raw.efive;
            else if (index == 1)
                resID = R.raw.gfive;
            else if (index == 2)
                resID = R.raw.bfour;
            else if (index == 3)
                resID = R.raw.bfive;
            else if (index == 4)
                resID = R.raw.win;
            else if (index == 5)
                resID = R.raw.lose;
            else
                resID = R.raw.lose;
            final MediaPlayer player = MediaPlayer.create(this, resID);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.release();
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
    }


    //TODO------------------------------------------------------------ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lights = ChooseLights.newList;
        actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar);

        gdf = new GetDatFile(getApplicationContext());
        MAXBRI = Integer.parseInt(new BrightnessSoundFile(getApplicationContext()).getBrightness());

        rainbow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#B44848"), Color.parseColor("#E5AD5C"), Color.parseColor("#E7C630"),
                        Color.parseColor("#6EB45C"), Color.parseColor("#4E9D74"), Color.parseColor("#32506F"), Color.parseColor("#121212")});
        rainbow.setAlpha(30);

        lightSize = lights.size();

        if (lightSize == 4 || lightSize == 1) {
            setContentView(R.layout.four_light_layout);
            currLayout = findViewById(R.id.fourLayout);
        } else if (lightSize == 3) {
            setContentView(R.layout.three_light_layout);
            currLayout = findViewById(R.id.threeLayout);
        } else if (lightSize == 2) {
            setContentView(R.layout.two_light_layout);
            currLayout = findViewById(R.id.twoLayout);
        }

        currLayout.setBackground(rainbow);

        rainbowH = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#B44848"), Color.parseColor("#E7C630"),
                        Color.parseColor("#6EB45C"), Color.parseColor("#32506F"), Color.parseColor("#121212")});
        rainbowH.setAlpha(200);

        rainbowHighlight = findViewById(R.id.rainbowHighlight);
        rainbowHighlight.setVisibility(View.VISIBLE);
        rainbowHighlight.setMinimumHeight(12);
        rainbowHighlight.setBackground(rainbowH);

        yellow = (Button) findViewById(R.id.yellow);
        yellow.setOnClickListener(this);
        if (lightSize == 2 || lightSize == 3)
            yellow.setBackground(getDrawable(R.drawable.two_yellow_handler));


        red = (Button) findViewById(R.id.red);
        red.setOnClickListener(this);
        if (lightSize == 2)
            red.setBackground(getDrawable(R.drawable.two_handler));
        if (lightSize == 3)
            red.setBackground(getDrawable(R.drawable.three_handler_red));

        if (lightSize != 2) {
            green = (Button) findViewById(R.id.green);
            green.setOnClickListener(this);
            if (lightSize == 3)
                green.setBackground(getDrawable(R.drawable.three_handler_green));
        }

        if (lightSize != 3 && lightSize != 2) {
            blue = (Button) findViewById(R.id.blue);
            blue.setOnClickListener(this);
        }

        toggleLights();

        //WAIT
        long now = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        while (now < start + 500) {
            now = System.currentTimeMillis();
        }


        currentTime = 0;

        startTimer();
        mTimer1.schedule(mTt1, 1, 100000);

    }

    //TODO------------------------------------------------------ON CLICK
    @Override
    public void onClick(View view) {
        if (hasWonAndCountdownHasntStarted == 0 || hasWonAndCountdownHasntStarted == -1) {
            int currentClick = -1;
            if (view == yellow) {
                playSound(0);
                currentClick = 0;
            }
            if (view == red) {
                playSound(1);
                currentClick = 1;
            }
            if (lightSize != 2) {
                if (view == green) {
                    playSound(2);
                    currentClick = 2;
                }
            }
            if (lightSize != 2 && lightSize != 3) {
                if (view == blue) {
                    playSound(3);
                    currentClick = 3;
                }
            }
            if (isCorrect(currentClick) > 0) {
                if (currentIndex == list.size() - 1) {
                    Log.d("YOU WIN", "YOU WIN");
                    youWin();
                    currentIndex = 0;
                } else {
                    Log.d("NEXT", "GOOD JOB");
                    currentIndex++;
                }
            } else {
                Log.d("YOU LOSE", "SUCKS");
                youLose();
            }
        }
    }

    //TODO------------------------------------------------------IS CORRECT
    private int isCorrect(int index) {
        if (list.get(currentIndex) != index)
            return -1;
        else
            return 1;
    }

    //TODO------------------------------------------------------ITERATE THROUGH COLORS
    private void iterateThroughColors() {
        correct = false;

        int i = 0;
        while (i < list.size()) {

            final int newI = i;

            toggleLights();

            //WAIT
            long now = System.currentTimeMillis();
            long start = System.currentTimeMillis();
            while (now < start + 100) {
                now = System.currentTimeMillis();
            }

            showColor(list.get(newI));

            //WAIT
            now = System.currentTimeMillis();
            start = System.currentTimeMillis();
            while (now < start + 1000) {
                now = System.currentTimeMillis();
            }

            i++;
        }
    }

    //TODO------------------------------------------------------START GAME
    private void addRandomToList() {
        Random rand = new Random();
        int n;
        if (lightSize == 1)
            n = rand.nextInt(4);
        else
            n = rand.nextInt(lightSize);
        list.add(n);
    }

    //TODO------------------------------------------------------START GAME
    private void showColor(final int n) {

        final LightState lightState = new LightState();

        switch (n) {
            case 0:
                lightState.setSaturation(254);
                lightState.setBrightness(MAXBRI);
                lightState.setHue(10000);
                break;
            case 1:
                lightState.setSaturation(254);
                lightState.setBrightness(MAXBRI);
                lightState.setHue(65300);
                break;
            case 2:
                lightState.setSaturation(254);
                lightState.setBrightness(MAXBRI);
                lightState.setHue(28000);
                break;
            case 3:
                lightState.setSaturation(254);
                lightState.setBrightness(MAXBRI);
                lightState.setHue(47000);
                break;
        }

        final LightPoint currLight;

        if (lightSize == 1)
            currLight = lights.get(0);
        else
            currLight = lights.get(n);


        currLight.updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                final LightPoint handleLight = currLight;
                if (returnCode == ReturnCode.SUCCESS) {
                    Log.i(TAG, "Changed hue of light " + handleLight.getIdentifier() + " to " + lightState.getHue());
                } else {
                    Log.e(TAG, "Error changing hue of light " + handleLight.getIdentifier());
                    for (HueError error : errorList) {
                        Log.e(TAG, error.toString());
                    }
                }
            }
        });
    }

    //TODO------------------------------------------------------TOGGLE LIGHTS
    private void toggleLights() {
        for (final LightPoint light : lights) {
            final LightState lightState = new LightState();

            lightState.setBrightness(1);
            lightState.setSaturation(0);

            light.updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "turned down to 1");
                    } else {
                        Log.e(TAG, "could not turn down to 1 ");
                        for (HueError error : errorList) {
                            Log.e(TAG, error.toString());
                        }
                    }
                }
            });
        }
    }

    private void backToNormal() {
        for (int i = 0; i < lights.size(); i++) {
            final LightState lightState = new LightState();

            lightState.setBrightness(ChooseLights.returnToNormal.get(i).getLightState().getBrightness());
            lightState.setSaturation(ChooseLights.returnToNormal.get(i).getLightState().getSaturation());
            lightState.setHue(ChooseLights.returnToNormal.get(i).getLightState().getHue());

            if (!ChooseLights.returnToNormal.get(i).getLightState().isOn())
                lightState.setOn(false);

            lights.get(i).updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "turned back to normal");
                    } else {
                        Log.e(TAG, "could not turn back to normal");
                        for (HueError error : errorList) {
                            Log.e(TAG, error.toString());
                        }
                    }
                }
            });
        }
    }

    //TODO------------------------------------------------------YOU LOSE
    private void youLose() {
        loss = true;

        for (final LightPoint light : lights) {
            final LightState lightState = new LightState();

            lightState.setBrightness(MAXBRI);
            lightState.setSaturation(254);
            lightState.setHue(65300);

            light.updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "You lost!");
                    } else {
                        Log.e(TAG, "could not show lost colors ");
                        for (HueError error : errorList) {
                            Log.e(TAG, error.toString());
                        }
                    }
                }
            });
        }

        stopTimer();
        startTimer();

    }

    //TODO------------------------------------------------------YOU WIN
    private void youWin() {

        hasWonAndCountdownHasntStarted = 1;

        if (lightSize == 1) {
            if (Integer.parseInt(gdf.get1Score()) < curLevel) {
                gdf.set1Score(String.valueOf(curLevel));
            }
        } else if (lightSize == 2) {
            if (Integer.parseInt(gdf.get2Score()) < curLevel) {
                gdf.set2Score(String.valueOf(curLevel));
            }
        } else if (lightSize == 3) {
            if (Integer.parseInt(gdf.get3Score()) < curLevel) {
                gdf.set3Score(String.valueOf(curLevel));
            }
        } else if (lightSize == 4) {
            if (Integer.parseInt(gdf.get4Score()) < curLevel) {
                gdf.set4Score(String.valueOf(curLevel));
            }
        }

        correct = true;

        playSound(4);

        for (final LightPoint light : lights) {
            final LightState lightState = new LightState();

            lightState.setBrightness(MAXBRI);
            lightState.setSaturation(254);
            lightState.setHue(28000);

            light.updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "You WIN");
                    } else {
                        Log.e(TAG, "could not set to WIN");
                        for (HueError error : errorList) {
                            Log.e(TAG, error.toString());
                        }
                    }
                }
            });
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        GameScreen.this.finish();
        stopTimer();
        if (yourCountDownTimer != null)
            yourCountDownTimer.cancel();
        backToNormal();
    }

    @Override
    public void onStop() {
        super.onStop();
        GameScreen.this.finish();
        stopTimer();
        if (yourCountDownTimer != null)
            yourCountDownTimer.cancel();
        backToNormal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GameScreen.this.finish();
        stopTimer();
        if (yourCountDownTimer != null)
            yourCountDownTimer.cancel();
        backToNormal();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }

}
