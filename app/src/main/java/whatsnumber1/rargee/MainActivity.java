package whatsnumber1.rargee;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "rargee";

    public static Bridge bridge;

    public static Typeface typeLeg;
    public static Typeface typeFeet;

    private BridgeDiscovery bridgeDiscovery;

    private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

    String newBriVal;

    // UI elements
    private TextView statusTextView;
    private ListView bridgeDiscoveryListView;
    private TextView bridgeIpTextView;
    private View pushlinkImage;
    private Button randomizeLightsButton;
    private Button bridgeDiscoveryButton;
    private Button highScoreButton;
    private Button bri_level_button;
    private Button sound_onoff_button;
    View rainbowHighlight;
    GradientDrawable rainbowH;

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    float textViewAlpha;

    AlertDialog.Builder helpBuilder;
    AlertDialog helpDialog;
    BrightnessSoundFile bsf;

    public static boolean soundy = false;

    Handler handlers = new Handler();
    Runnable runnable;

    enum UIState {
        Idle,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rainbowH = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#B44848"), Color.parseColor("#E7C630"),
                        Color.parseColor("#6EB45C"), Color.parseColor("#32506F"), Color.parseColor("#121212")});
        rainbowH.setAlpha(200);

        rainbowHighlight = findViewById(R.id.rainbowHighlight);
        rainbowHighlight.setVisibility(View.VISIBLE);
        rainbowHighlight.setMinimumHeight(12);
        rainbowHighlight.setBackground(rainbowH);

        typeFeet = Typeface.createFromAsset(getAssets(), "fonts/NEXT ART_Regular.otf");
        typeLeg = Typeface.createFromAsset(getAssets(), "fonts/libre-franklin.thin.ttf");


        // Setup the UI
        statusTextView = (TextView) findViewById(R.id.status_text);
        statusTextView.setTypeface(typeLeg);
        bridgeDiscoveryListView = (ListView) findViewById(R.id.bridge_discovery_result_list);
        bridgeDiscoveryListView.setOnItemClickListener(this);
        bridgeIpTextView = (TextView) findViewById(R.id.bridge_ip_text);
        bridgeIpTextView.setTypeface(typeLeg);
        pushlinkImage = findViewById(R.id.pushlink_image);
        bridgeDiscoveryButton = (Button) findViewById(R.id.bridge_discovery_button);
        bridgeDiscoveryButton.setOnClickListener(this);
        bridgeDiscoveryButton.setTypeface(typeFeet);
        randomizeLightsButton = (Button) findViewById(R.id.randomize_lights_button);
        randomizeLightsButton.setOnClickListener(this);
        randomizeLightsButton.setTypeface(typeFeet);
        highScoreButton = (Button) findViewById(R.id.high_score_button);
        highScoreButton.setOnClickListener(this);
        highScoreButton.setTypeface(typeFeet);
        bri_level_button = (Button) findViewById(R.id.bri_level);
        bri_level_button.setOnClickListener(this);
        sound_onoff_button = (Button) findViewById(R.id.sound_on_off);
        sound_onoff_button.setOnClickListener(this);

        // Connect to a bridge or start the bridge discovery
        String bridgeIp = getLastUsedBridgeIp();
        if (bridgeIp == null)
            startBridgeDiscovery();
        else
            connectToBridge(bridgeIp);

        bsf = new BrightnessSoundFile(getApplicationContext());

        if (bsf.getSound().equals("1")) {
            sound_onoff_button.setText("Sound On");
            soundy = true;
        } else if (bsf.getSound().equals("0")) {
            sound_onoff_button.setText("Sound Off");
            soundy = false;
        }

    }

    /**
     * Use the KnownBridges API to retrieve the last connected bridge
     *
     * @return Ip address of the last connected bridge, or null
     */
    private String getLastUsedBridgeIp() {
        List<KnownBridge> bridges = KnownBridges.getAll();

        if (bridges.isEmpty()) {
            return null;
        }

        return Collections.max(bridges, new Comparator<KnownBridge>() {
            @Override
            public int compare(KnownBridge a, KnownBridge b) {
                return a.getLastConnected().compareTo(b.getLastConnected());
            }
        }).getIpAddress();
    }

    /**
     * Start the bridge discovery search
     * Read the documentation on meethue for an explanation of the bridge discovery options
     */
    private void startBridgeDiscovery() {
        disconnectFromBridge();

        bridgeDiscovery = new BridgeDiscovery();
        bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.UPNP, bridgeDiscoveryCallback);

        updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");
    }

    /**
     * Stops the bridge discovery if it is still running
     */
    private void stopBridgeDiscovery() {
        if (bridgeDiscovery != null) {
            bridgeDiscovery.stop();
            bridgeDiscovery = null;
        }
    }

    /**
     * The callback that receives the results of the bridge discovery
     */
    private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
        @Override
        public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
            // Set to null to prevent stopBridgeDiscovery from stopping it
            bridgeDiscovery = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (returnCode == ReturnCode.SUCCESS) {
                        bridgeDiscoveryListView.setAdapter(new BridgeDiscoveryResultAdapter(getApplicationContext(), results));
                        bridgeDiscoveryResults = results;

                        updateUI(UIState.BridgeDiscoveryResults, "Found " + results.size() + " bridge(s) in the network.");
                    } else if (returnCode == ReturnCode.STOPPED) {
                        Log.i(TAG, "Bridge discovery stopped.");
                    } else {
                        updateUI(UIState.Idle, "Error doing bridge discovery: " + returnCode);
                    }
                }
            });
        }
    };

    /**
     * Use the BridgeBuilder to create a bridge instance and connect to it
     */
    private void connectToBridge(String bridgeIp) {
        stopBridgeDiscovery();
        disconnectFromBridge();

        Log.d("WHAT", bridgeIp);

        bridge = new BridgeBuilder("app name", "device name")
                .setIpAddress(bridgeIp)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build();

        bridge.connect();

        bridgeIpTextView.setText("Bridge IP: " + bridgeIp);
        updateUI(UIState.Connecting, "Connecting to bridge...");
    }

    /**
     * Disconnect a bridge
     * The hue SDK supports multiple bridge connections at the same time,
     * but for the purposes of this demo we only connect to one bridge at a time.
     */
    private void disconnectFromBridge() {
        if (bridge != null) {
            bridge.disconnect();
            bridge = null;
        }
    }

    /**
     * The callback that receives bridge connection events
     */
    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
            Log.i(TAG, "Connection event: " + connectionEvent);

            switch (connectionEvent) {
                case LINK_BUTTON_NOT_PRESSED:
                    updateUI(UIState.Pushlinking, "Press the link button to authenticate.");
                    break;

                case COULD_NOT_CONNECT:
                    updateUI(UIState.Connecting, "Could not connect.");
                    break;

                case CONNECTION_LOST:
                    updateUI(UIState.Connecting, "Connection lost. Attempting to reconnect.");
                    break;

                case CONNECTION_RESTORED:
                    updateUI(UIState.Connected, "Connection restored.");
                    break;

                case DISCONNECTED:
                    // User-initiated disconnection.
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
            for (HueError error : list) {
                Log.e(TAG, "Connection error: " + error.toString());
            }
        }
    };

    /**
     * The callback the receives bridge state update events
     */
    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);

            switch (bridgeStateUpdatedEvent) {
                case INITIALIZED:
                    // The bridge state was fully initialized for the first time.
                    // It is now safe to perform operations on the bridge state.
                    updateUI(UIState.Connected, "Ready to play!");
                    break;

                case LIGHTS_AND_GROUPS:
                    // At least one light was updated.
                    break;

                default:
                    break;
            }
        }
    };


    // UI methods

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String bridgeIp = bridgeDiscoveryResults.get(i).getIP();
        connectToBridge(bridgeIp);
    }

    @Override
    public void onClick(View view) {
        if (view == randomizeLightsButton) {
            Intent intent = new Intent(this, ChooseLights.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        }

        if (view == highScoreButton) {
            Intent intent = new Intent(this, HighScores.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        }


        if (view == bridgeDiscoveryButton) {

            startBridgeDiscovery();

        }

        if (view == bri_level_button) {
            openBriDialog();
        }

        if (view == sound_onoff_button) {
            if (bsf.getSound().equals("1")) {
                bsf.setSound("0");
                sound_onoff_button.setText("Sound Off");
                soundy = false;
            } else if (bsf.getSound().equals("0")) {
                bsf.setSound("1");
                sound_onoff_button.setText("Sound On");
                soundy = true;
            }

        }

    }


    void discoveryButton() {
        disconnectFromBridge();
    }


    private void openBriDialog() {

        final View layout = LayoutInflater.from(this).inflate(R.layout.bri_popup,
                null);

        ((TextView) layout.findViewById(R.id.textAlpha)).setTypeface(typeFeet);
        ((TextView) layout.findViewById(R.id.textAlpha1)).setTypeface(typeFeet);

        helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("");
        helpBuilder.setView(layout);
        helpDialog = helpBuilder.create();
        helpDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        helpDialog.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        helpDialog.getWindow().setGravity(Gravity.CENTER);
        helpDialog.show();
        helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams lp = helpDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        helpDialog.getWindow().setAttributes(lp);
        helpDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        SeekArc seekBar = (SeekArc) layout.findViewById(R.id.pplWithin);
        seekBar.setProgressColor(Color.WHITE);
        seekBar.setProgressWidth(200);
        seekBar.setArcColor(Color.DKGRAY);
        seekBar.setArcWidth(200);
        seekBar.setTouchInSide(true);

        bsf = new BrightnessSoundFile(getApplicationContext());

        seekBar.setProgress(Integer.parseInt(bsf.getBrightness()));
        newBriVal = bsf.getBrightness();
        int percentAlpha = (Integer.parseInt(newBriVal));
        textViewAlpha = (percentAlpha / 254);
        if (percentAlpha > 20) {
            ((ImageView) layout.findViewById(R.id.lightbulb)).getDrawable().setAlpha(percentAlpha);
        } else {
            ((ImageView) layout.findViewById(R.id.lightbulb)).getDrawable().setAlpha(20);
        }

        seekBar.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                bsf.setBrightness(newBriVal);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                if (progress >= 1 && progress <= 254) {
                    String progressString = String.valueOf(progress);
                    newBriVal = progressString;
                    textViewAlpha = progress / 254;
                    if (progress > 20) {
                        ((ImageView) layout.findViewById(R.id.lightbulb)).getDrawable().setAlpha(progress);
                    } else
                        ((ImageView) layout.findViewById(R.id.lightbulb)).getDrawable().setAlpha(20);
                }
            }
        });
    }


    private void updateUI(final UIState state, final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Status: " + status);
                statusTextView.setText(status);

                bridgeDiscoveryListView.setVisibility(View.GONE);
                bridgeIpTextView.setVisibility(View.GONE);
                pushlinkImage.setVisibility(View.GONE);
                randomizeLightsButton.setVisibility(View.GONE);
                bridgeDiscoveryButton.setVisibility(View.GONE);

                switch (state) {
                    case Idle:
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryRunning:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryResults:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case Connecting:
                        bridgeIpTextView.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case Pushlinking:
                        bridgeIpTextView.setVisibility(View.VISIBLE);
                        pushlinkImage.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case Connected:
                        bridgeIpTextView.setVisibility(View.INVISIBLE);
                        randomizeLightsButton.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        highScoreButton.setVisibility(View.VISIBLE);
                        bri_level_button.setVisibility(View.VISIBLE);
                        sound_onoff_button.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }
}