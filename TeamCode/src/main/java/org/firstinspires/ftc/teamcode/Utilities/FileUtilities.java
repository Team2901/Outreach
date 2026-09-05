package org.firstinspires.ftc.teamcode.utilities;

import android.os.Environment;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class FileUtilities {
    public final static String PICTURES_FOLDER_NAME = "Team";
    public final static String TEAM_FOLDER_NAME = "Team";

    public static void writeConfigFile(String filename,
                                       List<? extends Object> config) throws IOException {

        final File teamDir = new File(Environment.getExternalStorageDirectory(), TEAM_FOLDER_NAME);
        boolean newDir = teamDir.mkdirs();
        final File file = new File(teamDir, filename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Object o : config) {
                writer.write(o.toString());
                writer.newLine();
            }
        }
    }

    public static void writeConfigFile(String filename, Object value) throws IOException{
        final File teamDir = new File(Environment.getExternalStorageDirectory(), TEAM_FOLDER_NAME);
        boolean newDir = teamDir.mkdirs();
        final File file = new File(teamDir, filename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(value.toString());
        }
    }

    public static int readTeamColor(String filename) throws IOException{
        final File teamDir = new File(Environment.getExternalStorageDirectory(), TEAM_FOLDER_NAME);
        boolean newDir = teamDir.mkdirs();
        final File file = new File(teamDir, filename);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return Integer.parseInt(line);
        }
    }

    public static List<String> readConfigFile(String filename) throws IOException {

        final File teamDir = new File(Environment.getExternalStorageDirectory(), TEAM_FOLDER_NAME);
        boolean newDir = teamDir.mkdirs();
        final File file = new File(teamDir, filename);

        final List<String> config = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                config.add(line);
                line = reader.readLine();
            }
        }

        return config;
    }

    public static List<Integer> readIntegerConfigFile(String filename) throws IOException {

        final List<String> stringConfig = readConfigFile(filename);

        final List<Integer> config = new ArrayList<>();
        for (String string : stringConfig) {
            config.add(Integer.valueOf(string));
        }

        return config;
    }
    public static List<Double> readDoubleConfigFile(String filename) throws IOException {

        final List<String> stringConfig = readConfigFile(filename);

        final List<Double> config = new ArrayList<>();
        for (String string : stringConfig) {
            config.add(Double.valueOf(string));
        }

        return config;
    }
}