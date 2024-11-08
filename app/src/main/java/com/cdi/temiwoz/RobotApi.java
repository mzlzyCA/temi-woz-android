package com.cdi.temiwoz;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.util.Log;
import java.io.IOException;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.TtsRequest.Status;


import com.robotemi.sdk.constants.*;
import com.robotemi.sdk.listeners.OnMovementStatusChangedListener;
import com.robotemi.sdk.permission.Permission;
import com.robotemi.sdk.telepresence.*;
import com.robotemi.sdk.Robot.TtsListener;
import com.robotemi.sdk.Robot.AsrListener;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
import com.robotemi.sdk.UserInfo;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;



import org.jetbrains.annotations.NotNull;
import org.json.*;

import java.util.ArrayList;
import java.util.List;


public class RobotApi implements TtsListener,
                                 AsrListener,
                                 OnGoToLocationStatusChangedListener,
        OnBeWithMeStatusChangedListener,
        OnConstraintBeWithStatusChangedListener,
        OnDetectionStateChangedListener,
        OnMovementStatusChangedListener
{

    private Robot robot;

    public TemiWebsocketServer server;

    String speak_id;
    String ask_id;
    String goto_id;
    String tilt_id;
    String turn_id;
    String getContact_id;
    String call_id;

    private static final String TAG = "RobotApi";
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    private MainActivity activity;

    RobotApi (Robot robotInstance, MainActivity activity) {
        this.robot = robotInstance;
        this.activity = activity;
        robot.addTtsListener(this);
        robot.addAsrListener(this);
        robot.addOnGoToLocationStatusChangedListener(this);
        robot.addOnBeWithMeStatusChangedListener(this);
        robot.addOnConstraintBeWithStatusChangedListener(this);
        robot.addOnMovementStatusChangedListener(this);
        robot.addOnDetectionStateChangedListener(this);
        // robot.toggleNavigationBillboard(false);

    }



    // location related
    public void gotoLocation(String location, String id) {
        robot.goTo(location);
        goto_id = id;
    }
    public void saveLocation(String location, String id){
        boolean finished = robot.saveLocation(location);

        try {
            server.broadcast(new JSONObject().put("saveLocation",finished).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void deleteLocation(String location, String id){
        boolean finished = robot.saveLocation(location);

        try {
            server.broadcast(new JSONObject().put("deleteLocation",finished).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //movement..
    public void stopMovement(String id){
        robot.stopMovement();
    }

    public void beWithMe(String id){
        robot.beWithMe();
    }

    public void constraintBeWith(String id){
        robot.constraintBeWith();
    }


    public void tiltAngle(int angle, String id) {
        robot.tiltAngle(angle);
        tilt_id = id;
        try {
            server.broadcast(new JSONObject().put("id", tilt_id).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void turnBy(int angle, String id) {
        robot.turnBy(angle, 1);
        turn_id = id;
//        try {
//            server.broadcast(new JSONObject().put("id", turn_id).toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        // 在movement回调中发送消息
    }

    // call someone
    public void getContact(String id){
        List<UserInfo> list = new ArrayList<>();
        list = robot.getAllContact();

        getContact_id = id;

        try{
            server.broadcast(new JSONObject().put("id", getContact_id).put("userinfo",list).toString());
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void startCall(String userId, String id){
        call_id = id;
        robot.startTelepresence("孙老师", userId);


    }

    // user interaction
    public void wakeup(String id){
        robot.wakeup();
    }
    public void speak(String sentence, String id) {
        robot.speak(TtsRequest.create(sentence, false));
        speak_id = id;
    }

    public void askQuestion(String sentence, String id) {
        robot.askQuestion(sentence);
        ask_id = id;
    }

    public void setDetectionMode(boolean on, String id){
        robot.setDetectionModeOn(on);
        System.out.println("set detection mode on");
        try {
            server.broadcast(new JSONObject().put("detection mode", on).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTrackUserOn(boolean on, String id){
        int ispermise = robot.checkSelfPermission(Permission.SETTINGS);
        System.out.println(ispermise);
        robot.setTrackUserOn(on);
        System.out.println("set track user mode on");
        try {
            server.broadcast(new JSONObject().put("track user mode", on).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void checkDetectionMode(String id){
        boolean isDetectionModeOn = robot.isDetectionModeOn();
        try {
            server.broadcast(new JSONObject().put("CheckDetectionState:", isDetectionModeOn).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    
    public void beWithMe(){
        robot.beWithMe();
    }

    public void constraintBeWith(){
        robot.constraintBeWith();
    }


    @Override
    public void onDetectionStateChanged(int state){
        try {
            server.broadcast(new JSONObject().put("event", "onDetectionStateChanged").put("state",state).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onBeWithMeStatusChanged(String status ){
        try {
            server.broadcast(new JSONObject().put("event", "onBeWithMeStatusChanged").put("status",status).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 改为用来替代 detectmode，当检测到人的时候返回消息

//        if (status.equals("start")){
//            try{
//                server.broadcast(new JSONObject().put("event","onBeWithMeStatusChanged").put("status",status).toString());
//            }catch (JSONException e ){
//                e.printStackTrace();
//            }
//        }
    }


    @Override
    public void onConstraintBeWithStatusChanged(boolean isConstraint) {

        try{
            server.broadcast(new JSONObject().put("event","onConstraintBeWithStatusChanged").put("state",isConstraint).toString());
        }catch (JSONException e ){
            e.printStackTrace();
        }

    }



    @Override
    public void onTtsStatusChanged(TtsRequest ttsRequest) {
        if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED) {
            try {
                server.broadcast(new JSONObject().put("id", speak_id).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @GoToLocationStatus String status, int descriptionId, String description) {
        if (status.equals("complete")) {
            try {
                server.broadcast(new JSONObject().put("id", goto_id).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



//    @Override
//    public void onMovementStatusChanged(String type, String status){
//
//    }

    @Override
    public void onMovementStatusChanged(@NotNull String type, String status){
        if(status.equals("complete")){
            try {
                server.broadcast(new JSONObject().put("id", turn_id).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onAsrResult(@NotNull String text) {
        try {
            server.broadcast(new JSONObject().put("id", ask_id).put("reply",text).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        robot.finishConversation();
    }

    public void stop() {
        stopCamera(null); // 关闭摄像头
        //robot.removeTtsListener(this);
        //robot.removeAsrListener(this);
        //robot.removeOnGoToLocationStatusChangedListener(this);
    }

    public void setSurfaceHolder(SurfaceHolder holder) {
        this.surfaceHolder = holder;
    }

    public void startCamera(String id) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.showCamera();
                }
            });
            try {
                server.broadcast(new JSONObject()
                        .put("event", "cameraStarted")
                        .put("id", id)
                        .put("success", true)
                        .toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopCamera(String id) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.showWebView();
                }
            });
            try {
                server.broadcast(new JSONObject()
                        .put("event", "cameraStopped")
                        .put("id", id)
                        .put("success", true)
                        .toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 添加拍照完成后的 WebSocket 通知
    public void notifyPictureTaken(String filePath) {
        try {
            server.broadcast(new JSONObject()
                    .put("event", "pictureTaken")
                    .put("filePath", filePath)
                    .toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 添加录像完成后的 WebSocket 通知
    public void notifyRecordingStopped(String filePath) {
        try {
            server.broadcast(new JSONObject()
                    .put("event", "recordingStopped")
                    .put("filePath", filePath)
                    .toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
