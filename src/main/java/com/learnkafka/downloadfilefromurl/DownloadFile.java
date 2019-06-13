/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.learnkafka.downloadfilefromurl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author bbiadmin
 */
public class DownloadFile {

    private static String LANDING_PATH = "E://iot/ads/videos/";
    private static String API_URL = "http://localhost:3000";
    private static JSONArray existingIdsArray = new JSONArray();

    public static void main(String[] args) throws MalformedURLException, IOException {
        String response = getVideoUrls(100);
        JSONObject jsonObj = new JSONObject(response);
        String arrayString = jsonObj.get("videos").toString();
        JSONArray arrayOfVideoData = new JSONArray(arrayString);
        JSONArray newIdsArray = getNewVideoIds(arrayOfVideoData);
        existingIdsArray = getExistingVideoIds();
//        System.out.println("Existing ids " + existingIdsArray);
//        System.out.println("New ids " + newIdsArray);
        refreshVideoAndList(newIdsArray, existingIdsArray, arrayOfVideoData);

    }

    private static void refreshVideoAndList(JSONArray newIdsArray, JSONArray existingIdsArray, JSONArray videoArray) {
        ArrayList<Integer> nl = new ArrayList<>();
        ArrayList<Integer> el = new ArrayList<>();
        for (int i = 0; i < newIdsArray.length(); i++) {
            nl.add(Integer.parseInt(newIdsArray.get(i).toString()));
        }
        for (int i = 0; i < existingIdsArray.length(); i++) {
            el.add(Integer.parseInt(existingIdsArray.get(i).toString()));
        }

        ArrayList<Integer> temp = new ArrayList<>(el);
        el.removeAll(nl);
        nl.removeAll(temp);
        for (int i : nl) {
            downloadVideo(i, videoArray);
        }
        for (int i : el) {
            deleteVideo(i, videoArray);
        }
    }

    private static JSONArray getNewVideoIds(JSONArray data) {
        JSONArray newIdsArray = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            JSONObject tempJson = new JSONObject(data.get(i).toString());
            int video_id = (int) tempJson.get("video_id");
            newIdsArray.put(video_id);
        }
        return newIdsArray;
    }

    private static JSONArray getExistingVideoIds() throws IOException {
        JSONArray existingIdsArray = new JSONArray();
        try (FileReader reader = new FileReader(LANDING_PATH + "ids.json")) {
            String fileContent = "";
            int i = 0;
            while ((i = reader.read()) != -1) {
                fileContent += ((char) i);
            }
            reader.close();
            existingIdsArray = new JSONArray(fileContent);
        } catch (FileNotFoundException e) {
            System.out.println("JSON file not found at the specified location");
            FileWriter file = new FileWriter(LANDING_PATH + "ids.json");
            file.write("[]");
            file.flush();
            String args[] = {};
            main(args);
        } catch (IOException e) {
            System.out.println("Problem reading existing IDS JSON file");
        }
        return existingIdsArray;
    }

    private static boolean writeIdsToFile(String data) {
        try (FileWriter file = new FileWriter(LANDING_PATH + "ids.json")) {
            file.write(data);
            file.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String getVideoUrls(int gateway_id) {
        String urlToRead = API_URL + "/api/videos/" + gateway_id;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {
            System.out.println("Problem getting Video Urls");
        }
        return result.toString();
    }

    private static void downloadVideo(String video_url, int video_id, String file_type) {
        video_url += "&video_type=" + file_type;
        String FILE_NAME = generateFileName(video_id, file_type);
        int CONNECT_TIMEOUT = 30000;
        int READ_TIMEOUT = 1200000;
        System.out.println("Starting Download...");
        System.out.println("Downloading...");
        try {
            FileUtils.copyURLToFile(
                    new URL(video_url),
                    new File(FILE_NAME),
                    CONNECT_TIMEOUT,
                    READ_TIMEOUT);
            System.out.println("Download complete!");
        } catch (Exception e) {
            System.out.println("Problem Downloading file!");
            System.out.println(e);
        }

    }

    private static void downloadVideo(int video_id, JSONArray data) {
        System.out.println("Want to download the video " + video_id);
        existingIdsArray.put(video_id);
        writeIdsToFile(existingIdsArray.toString());
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = new JSONObject(data.get(i).toString());
            if ((int) currentObject.get("video_id") == video_id) {
                downloadVideo(currentObject.get("video_url").toString(), video_id, currentObject.get("video_type").toString());
            }
        }
    }

    private static void deleteVideo(int video_id, JSONArray data) {
        System.out.println("Want to delete the video " + video_id);
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < existingIdsArray.length(); i++) {
            temp.add(Integer.parseInt(existingIdsArray.get(i).toString()));
        }
        temp.remove((Integer) video_id);
        existingIdsArray = new JSONArray();
        for (int i = 0; i < temp.size(); i++) {
            existingIdsArray.put(temp.get(i));
        }
        writeIdsToFile(existingIdsArray.toString());
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentObject = new JSONObject(data.get(i).toString());
            System.out.println("Current object " + currentObject);
            if ((int) currentObject.get("video_id") == video_id) {
                System.out.println("Found an matching Object to delete " + currentObject);
            }
        }
    }

    private static void deleteVideo(int video_id, String file_type) {
        File file = new File(LANDING_PATH + video_id + "." + file_type);
        System.out.println("Deleing " + LANDING_PATH + video_id + "." + file_type);
        if (file.delete()) {
            System.out.println("File deleted successfully");
        } else {
            System.out.println("Failed to delete the file");
        }
    }

    private static String generateFileName(int video_id, String file_type) {
        return LANDING_PATH + video_id + '.' + file_type;
    }

}
