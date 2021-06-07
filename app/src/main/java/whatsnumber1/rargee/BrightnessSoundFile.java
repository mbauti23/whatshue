package whatsnumber1.rargee;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
/**
 * Created by Deafenedlove on 6/3/2017.
 */

public class BrightnessSoundFile {

    private String brightness, sound;
    private File file;

    public BrightnessSoundFile(Context context)
    {
        file = new File(context.getFilesDir(), "brightness_file.txt");

        if (!file.exists())
        {
            brightness = "254";
            sound = "1";
            writeToFile();
        }
        else {
            readFile();
        }
    }

    private void readFile()
    {
        FileInputStream is;
        BufferedReader reader;
        try {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            brightness = reader.readLine();
            sound = reader.readLine();
            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeToFile()
    {
        try {
            String separator = System.getProperty("line.separator");
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.append(brightness);
            osw.append(separator);
            osw.append(sound);
            osw.flush();
            osw.close();
            fOut.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getBrightness()
    {
        return brightness;
    }
    public String getSound()
    {
        return sound;
    }

    public void setBrightness(String newBri)
    {
        brightness = newBri;
        writeToFile();
    }
    public void setSound(String newSound)
    {
        sound = newSound;
        writeToFile();
    }
}
