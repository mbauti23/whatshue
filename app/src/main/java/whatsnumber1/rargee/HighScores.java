package whatsnumber1.rargee;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Deafenedlove on 12/22/2017.
 */
public class HighScores extends AppCompatActivity {

    View layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.high_score_layout);
        layout = findViewById(R.id.scoreView);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        ((TextView) findViewById(R.id.action_bar_title)).setText("Scores");

        GradientDrawable rainbow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#B44848"), Color.parseColor("#E5AD5C"), Color.parseColor("#E7C630"),
                        Color.parseColor("#6EB45C"), Color.parseColor("#4E9D74"), Color.parseColor("#32506F"), Color.parseColor("#121212")});
        rainbow.setAlpha(30);

        layout.setBackground(rainbow);

        GradientDrawable rainbowH = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#121212"), Color.parseColor("#B44848"), Color.parseColor("#E7C630"),
                        Color.parseColor("#6EB45C"), Color.parseColor("#32506F"), Color.parseColor("#121212")});
        rainbowH.setAlpha(200);

        GradientDrawable spacer = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#050505"), Color.parseColor("#333333"), Color.parseColor("#999999"),
                        Color.parseColor("#bbbbbb"), Color.parseColor("#ffffff"), Color.parseColor("#bbbbbb"),
                        Color.parseColor("#999999"), Color.parseColor("#333333"), Color.parseColor("#050505")});
        spacer.setAlpha(200);

        View spacers[] = {findViewById(R.id.space2), findViewById(R.id.space3), findViewById(R.id.space4)};

        for (View spacer1 : spacers)
            spacer1.setBackground(spacer);

        View rainbowHigh = findViewById(R.id.rainbowHighlight);
        rainbowHigh.setVisibility(View.VISIBLE);
        rainbowHigh.setMinimumHeight(12);
        rainbowHigh.setBackground(rainbowH);

        GetDatFile gdf = new GetDatFile(getApplicationContext());

        String one, two, three, four;

        String error = "No score yet";

        one = gdf.get1Score();
        two = gdf.get2Score();
        three = gdf.get3Score();
        four = gdf.get4Score();

        if (one.equals("0"))
            one = error;
        if (two.equals("0"))
            two = error;
        if (three.equals("0"))
            three = error;
        if (four.equals("0"))
            four = error;


        ((TextView) findViewById(R.id.first)).setText(one);
        ((TextView) findViewById(R.id.second)).setText(two);
        ((TextView) findViewById(R.id.third)).setText(three);
        ((TextView) findViewById(R.id.fourth)).setText(four);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }
}
