package de.eightnine.rec;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kai on 06.09.14.
 */
public class CsvReader {

    private BufferedReader br;
    private SimpleDateFormat formatter;
    private String[] values;

    public CsvReader(String filename, String encoding) throws IOException {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
        formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
    }

    public boolean next() throws IOException {
        if(br == null) {
            return false;
        }
        String line = br.readLine();
        if(line == null) {
            br.close();
            br = null;
        } else {
            values = line.split("\\|", -1);
        }
        return values != null;
    }

    public void printLine() {
        for (int i = 0; i < values.length; i++) {
            if(i > 0) {
                System.out.print(", ");
            }
            System.out.print(values[i]);
        }
        System.out.println();
    }

    public String getString(int col) {
        if(values == null || col >= values.length) {
            return null;
        }
        return values[col];
    }

    public int getInt(int col) {
        return Integer.parseInt(values[col]);
    }

    public char getChar(int col) {
        return values[col].charAt(0);
    }

    public Date getDate(int col) throws ParseException {
        if(values == null || col >= values.length || "".equals(values[col])) {
            return null;
        }
        return formatter.parse(values[col]);
    }
}
