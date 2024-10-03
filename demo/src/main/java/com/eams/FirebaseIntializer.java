package com.eams;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;


public class FirebaseIntializer {
    /**
     * 
     */
    public static void initializeFirebase(){
       try{
            FileInputStream serviceAccount =
            new FileInputStream("/Users/joshuafong/Desktop/project-project-group-5/demo/src/main/resources/eams-4ceab-firebase-adminsdk-xs9e0-0ba909465c.json");

            @SuppressWarnings("deprecation")
            FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

            FirebaseApp.initializeApp(options);
            System.err.println("Firebase intialized");
       }catch(IOException e){
            e.printStackTrace();
       }
    }

}
