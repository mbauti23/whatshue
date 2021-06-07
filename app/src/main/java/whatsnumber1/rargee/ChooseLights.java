package whatsnumber1.rargee;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.ClipAttribute;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;

import java.util.ArrayList;
import java.util.List;

public class ChooseLights extends AppCompatActivity {

    private Bridge bridge;
    public static List<LightPoint> backToNormal;
    public static List<LightPoint> lights;
    public static List<LightPoint> newList;
    int[] intArray = new int[50];
    TextView user_conversations;
    ScrollView conversations;
    View whitespace, rainbowHigh;
    LinearLayout ll;
    Button acceptState;
    BridgeState bridgeState;
    public static List<LightPoint> returnToNormal;
    GradientDrawable rainbow, selection, selectedSelection;
    int[] wasOnArray = new int[50];
    int[] oldBrightness = new int[50];

    private void whichOnesAreOn() {
        int x = 1;

        for (final LightPoint light : lights) {

            if (light.getLightState().isOn())
                wasOnArray[x] = 1;
            else
                wasOnArray[x] = 0;

            x++;
        }

        while (x < 50) {
            wasOnArray[x] = -1;
            x++;
        }

    }

    private void initOldBrightness() {
        int x = 1;

        for (final LightPoint light : lights) {

            oldBrightness[x] = light.getLightState().getBrightness();

            x++;
        }

        while (x < 50) {
            wasOnArray[x] = -1;
            x++;
        }

    }

    private int wasOn(int n) {
        if (wasOnArray[n] == 1)
            return 1;
        else if (wasOnArray[n] == 0)
            return 0;
        else
            return -1;
    }

    private int oldBrightnessValue(int n) {
        return wasOnArray[n];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lights_layout);
        bridge = MainActivity.bridge;
        conversations = (ScrollView) findViewById(R.id.lightList);
        ll = (LinearLayout) findViewById(R.id.lightListHolder);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        ((TextView) findViewById(R.id.action_bar_title)).setText("Choose up to 4 lights to play with");

        rainbow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#B44848"), Color.parseColor("#E7C630"),
                        Color.parseColor("#6EB45C"), Color.parseColor("#32506F"), Color.parseColor("#121212")});
        rainbow.setAlpha(200);

        selection = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#222222"), Color.parseColor("#343434"),
                        Color.parseColor("#222222"), Color.parseColor("#121212"), Color.parseColor("#222222")});
        selection.setAlpha(200);

        selectedSelection = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#021002"), Color.parseColor("#113511"), Color.parseColor("#245024"),
                        Color.parseColor("#113511"), Color.parseColor("#021002"), Color.parseColor("#113511")});
        selectedSelection.setAlpha(200);

        rainbowHigh = findViewById(R.id.rainbowHighlight);
        rainbowHigh.setVisibility(View.VISIBLE);
        rainbowHigh.setMinimumHeight(12);
        rainbowHigh.setBackground(rainbow);

        bridgeState = bridge.getBridgeState();

        lights = bridgeState.getLights();
        backToNormal = bridgeState.getLights();

        whichOnesAreOn();
        initOldBrightness();

        newList = new ArrayList<>();

        acceptState = (Button) findViewById(R.id.acceptButton);
        acceptState.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addIndexesToList();
                returnToNormal = newList;
                Intent intent = new Intent(getApplication(), GameScreen.class);
                startActivity(intent);
                ChooseLights.this.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                finish();
            }
        });

        acceptState.setVisibility(View.GONE);

        for (int i = 1; i <= 40; i++) {
            intArray[i] = 0;
        }

        addLightsToList();

    }

    private void addLightsToList() {
        for (final LightPoint light : lights) {
            user_conversations = new TextView(getApplicationContext());
            user_conversations.setHint(light.getIdentifier());
            user_conversations.setText(light.getName());
            user_conversations.getHint();
            user_conversations.setGravity(Gravity.CENTER);
            user_conversations.setBackground(selection);
            user_conversations.setTextColor(Color.WHITE);
            user_conversations.setHeight(250);

            user_conversations.setOnClickListener(new View.OnClickListener() {
                private boolean stateChanged;

                public void onClick(View view) {
                    if (stateChanged) {
                        // reset background to default;
                        view.setBackground(selection);
                        ((TextView) view).setTextColor(Color.WHITE);
                        intArray[Integer.parseInt(((TextView) view).getHint().toString())] = 0;

                        if (oldBrightnessValue(Integer.parseInt(((TextView) view).getHint().toString())) != -1)
                            backToOldBrightness(Integer.parseInt(((TextView) view).getHint().toString()), oldBrightnessValue(Integer.parseInt(((TextView) view).getHint().toString())));

                        if (wasOn(Integer.parseInt(((TextView) view).getHint().toString())) == 0)
                            turnOffLight(Integer.parseInt(((TextView) view).getHint().toString()));

                        if (checkIntArraySize() == 0) {
                            ((TextView) findViewById(R.id.action_bar_title)).setText("Choose up to 4 lights to play with");
                            acceptState.setVisibility(View.GONE);
                        } else if (checkIntArraySize() > 4) {
                            acceptState.setVisibility(View.GONE);
                            ((TextView) findViewById(R.id.action_bar_title)).setText("Too many lights");
                        } else if (checkIntArraySize() == 4) {
                            acceptState.setVisibility(View.VISIBLE);
                            String thingToSay = "Max number of lights selected";
                            ((TextView) findViewById(R.id.action_bar_title)).setText(thingToSay);
                        } else if (checkIntArraySize() == 3) {
                            acceptState.setVisibility(View.VISIBLE);
                            String thingToSay = "Choose up to " + String.valueOf(4 - checkIntArraySize()) + " more light";
                            ((TextView) findViewById(R.id.action_bar_title)).setText(thingToSay);
                        } else {
                            acceptState.setVisibility(View.VISIBLE);
                            String thingToSay = "Choose up to " + String.valueOf(4 - checkIntArraySize()) + " more lights";
                            ((TextView) findViewById(R.id.action_bar_title)).setText(thingToSay);
                        }

                    } else {
                        view.setBackground(selectedSelection);
                        ((TextView) view).setTextColor(Color.WHITE);
                        intArray[Integer.parseInt(((TextView) view).getHint().toString())] = 1;
                        turnOnLight(Integer.parseInt(((TextView) view).getHint().toString()));
                        if (checkIntArraySize() > 4) {
                            acceptState.setVisibility(View.GONE);
                            ((TextView) findViewById(R.id.action_bar_title)).setText("Too many lights");
                        } else if (checkIntArraySize() == 0) {
                            ((TextView) findViewById(R.id.action_bar_title)).setText("Choose up to 4 lights to play with");
                            acceptState.setVisibility(View.GONE);
                        } else if (checkIntArraySize() == 4) {
                            acceptState.setVisibility(View.VISIBLE);
                            String thingToSay = "Max number of lights selected";
                            ((TextView) findViewById(R.id.action_bar_title)).setText(thingToSay);
                        } else if (checkIntArraySize() == 3) {
                            acceptState.setVisibility(View.VISIBLE);
                            String thingToSay = "Choose up to " + String.valueOf(4 - checkIntArraySize()) + " more light";
                            ((TextView) findViewById(R.id.action_bar_title)).setText(thingToSay);
                        } else {
                            acceptState.setVisibility(View.VISIBLE);
                            String thingToSay = "Choose up to " + String.valueOf(4 - checkIntArraySize()) + " more lights";
                            ((TextView) findViewById(R.id.action_bar_title)).setText(thingToSay);
                        }

                    }
                    stateChanged = !stateChanged;

                }
            });

            ll.addView(user_conversations);
            whitespace = new View(getApplicationContext());
            whitespace.setMinimumHeight(9);
            whitespace.setBackgroundColor(Color.parseColor("#000000"));
            ll.addView(whitespace);
        }

    }

    private void allBackToNormal() {

        for (int i = 0; i < lights.size(); i++) {
            final LightPoint light = backToNormal.get(i);


            if (light.getLightState().getHue() != null) {
                LightState lightState = new LightState();
                lightState.setBrightness(light.getLightState().getBrightness());
                lightState.setSaturation(light.getLightState().getSaturation());
                lightState.setHue(light.getLightState().getHue());

                if (wasOn(Integer.valueOf(light.getIdentifier())) == 0) {
                    lightState.setOn(false);
                }

                lights.get(i).updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                            @Override
                            public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                            }
                        }
                );
            }
        }
    }

    private void turnOnLight(int i) {
        LightState lightState = new LightState();


        lightState.setBrightness(Integer.parseInt(new BrightnessSoundFile(getApplicationContext()).getBrightness()));
        lightState.setSaturation(getLightWithIdentifier(i).getLightState().getSaturation());
        lightState.setHue(getLightWithIdentifier(i).getLightState().getHue());

        lightState.setOn(true);

        getLightWithIdentifier(i).updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                    @Override
                    public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                        if (returnCode == ReturnCode.SUCCESS) {
                            //Log.i(TAG, "turned back to normal");
                        } else {
                            //Log.e(TAG, "could not turn back to normal");
                            //for (HueError error : errorList) {
                            // Log.e(TAG, error.toString());
                        }
                    }
                }
        );
    }

    private void backToOldBrightness(int i, int oldBri) {
        LightState lightState = new LightState();


        lightState.setBrightness(oldBri);
        lightState.setSaturation(getLightWithIdentifier(i).getLightState().getSaturation());
        lightState.setHue(getLightWithIdentifier(i).getLightState().getHue());

        getLightWithIdentifier(i).updateStateFast(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                    @Override
                    public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                        if (returnCode == ReturnCode.SUCCESS) {
                            //Log.i(TAG, "turned back to normal");
                        } else {
                            //Log.e(TAG, "could not turn back to normal");
                            //for (HueError error : errorList) {
                            // Log.e(TAG, error.toString());
                        }
                    }
                }
        );
    }

    private void turnOffLight(int i) {
        LightState lightState = new LightState();


        lightState.setBrightness(getLightWithIdentifier(i).getLightState().getBrightness());
        lightState.setSaturation(getLightWithIdentifier(i).getLightState().getSaturation());
        lightState.setHue(getLightWithIdentifier(i).getLightState().getHue());

        lightState.setOn(false);

        getLightWithIdentifier(i).updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                    @Override
                    public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                        if (returnCode == ReturnCode.SUCCESS) {
                            //Log.i(TAG, "turned back to normal");
                        } else {
                            //Log.e(TAG, "could not turn back to normal");
                            //for (HueError error : errorList) {
                            // Log.e(TAG, error.toString());
                        }
                    }
                }
        );
    }

    private void addIndexesToList() {
        for (int i = 1; i <= 40; i++) {
            if (intArray[i] == 1) {
                newList.add(getLightWithIdentifier(i));
            }
        }
    }

    private LightPoint getLightWithIdentifier(int identifier) {
        for (final LightPoint light : lights) {
            if (light.getIdentifier().equals(String.valueOf(identifier))) {
                return light;
            }
        }
        return null;
    }

    private int checkIntArraySize() {
        int counter = 0;

        for (int i = 1; i <= 40; i++) {
            if (intArray[i] == 1)
                counter++;
        }

        return counter;
    }

    @Override
    public void onPause() {
        super.onPause();
        allBackToNormal();
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        allBackToNormal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        allBackToNormal();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        allBackToNormal();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }


}
