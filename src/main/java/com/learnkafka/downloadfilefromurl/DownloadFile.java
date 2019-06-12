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

/**
 *
 * @author bbiadmin
 */
public class DownloadFile {

    public static void main(String[] args) throws MalformedURLException, IOException {
        String FILE_URL = "http://localhost:3000/video?video_id=1";
        String FILE_NAME = "E:\\test-video.mp4";
        int CONNECT_TIMEOUT = 30000;
        int READ_TIMEOUT = 1200000;
        System.out.println("Starting Download...");
        System.out.println("Downloading");
        FileUtils.copyURLToFile(
                new URL(FILE_URL),
                new File(FILE_NAME),
                CONNECT_TIMEOUT,
                READ_TIMEOUT);
        System.out.println("Download complete");
    }
}
