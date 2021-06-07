package whatsnumber1.rargee;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

class GetDatFile {

    private String score1, score2, score3, score4;
    private File file;

    GetDatFile(Context context) {
        file = new File(context.getFilesDir(), "highest_score.txt");

        if (!file.exists()) {
            score1 = "0";
            score2 = "0";
            score3 = "0";
            score4 = "0";
            writeToFile();
        } else {
            readFile();
        }
    }

    private void readFile() {
        FileInputStream is;
        BufferedReader reader;
        try {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            score1 = reader.readLine();
            score2 = reader.readLine();
            score3 = reader.readLine();
            score4 = reader.readLine();
            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeToFile() {
        try {
            String separator = System.getProperty("line.separator");
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.append(score1);
            osw.append(separator);
            osw.append(score2);
            osw.append(separator);
            osw.append(score3);
            osw.append(separator);
            osw.append(score4);
            osw.flush();
            osw.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String get1Score() {
        return score1;
    }

    String get2Score() {
        return score2;
    }

    String get3Score() {
        return score3;
    }

    String get4Score() {
        return score4;
    }

    void set1Score(String newScore) {
        score1 = newScore;
        writeToFile();
    }

    void set2Score(String newScore) {
        score2 = newScore;
        writeToFile();
    }

    void set3Score(String newScore) {
        score3 = newScore;
        writeToFile();
    }

    void set4Score(String newScore) {
        score4 = newScore;
        writeToFile();
    }
}
