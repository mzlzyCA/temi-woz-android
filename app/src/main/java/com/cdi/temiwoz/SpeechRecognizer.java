// package com.cdi.temiwoz;

// import android.util.Log;
// import com.alibaba.nls.client.AccessToken;
// import com.alibaba.nls.client.protocol.NlsClient;
// // import com.alibaba.nls.client.protocol.OutputFormatEnum;
// // import com.alibaba.nls.client.protocol.SampleRateEnum;
// import com.robotemi.sdk.Robot;
// // import com.robotemi.sdk.listeners.OnRobotReadyListener;
// // import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
// // import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
// // import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
// // import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
// // import com.robotemi.sdk.listeners.OnMovementStatusChangedListener;
// // import com.robotemi.sdk.listeners.OnTtsListener;
// // import com.robotemi.sdk.model.DetectionState;
// // import com.robotemi.sdk.model.MovementStatus;
// // import org.json.JSONException;
// // import org.json.JSONObject;
// import org.json.JSONException;
// import org.json.JSONObject;
// import org.jetbrains.annotations.NotNull;

// import com.cdi.temiwoz.ConfigLoader;

// public class SpeechRecognizer implements Robot.AsrListener {
//     private static final String TAG = "SpeechRecognizer";
//     private String ACCESS_KEY_ID = ConfigLoader.getAccessKeyId();
//     private String ACCESS_KEY_SECRET = ConfigLoader.getAccessKeySecret();
    
//     private NlsClient client;
//     private AccessToken accessToken;
//     private Robot robot;
    
//     public SpeechRecognizer(Robot robot) {
//         this.robot = robot;
//         // 初始化 NlsClient
//         client = new NlsClient();
//         // 获取访问令牌
//         accessToken = AccessToken.apply();
//         // 注册 ASR 监听器
//         robot.addAsrListener(this);
//     }
    
//     // public void startRecognition() {
//     //     try {
//     //         // 创建语音识别请求
//     //         SpeechSynthesizer synthesizer = new SpeechSynthesizer(client);
//     //         synthesizer.setAppKey(accessToken.getAppKey());
//     //         synthesizer.setToken(accessToken.getToken());
//     //         synthesizer.setFormat(OutputFormatEnum.WAV);
//     //         synthesizer.setVoice("xiaoyun");
//     //         synthesizer.setVolume(50);
//     //         synthesizer.setSpeechRate(0);
//     //         synthesizer.setPitchRate(0);
            
//     //         // 开始识别
//     //         SpeechSynthesizerResponse response = synthesizer.start();
//     //         if (response != null) {
//     //             Log.d(TAG, "Recognition started successfully");
//     //         }
//     //     } catch (Exception e) {
//     //         Log.e(TAG, "Failed to start recognition", e);
//     //     }
//     // }
//     @Override
//     public void onAsrResult(@NotNull String text) {
//         System.out.println("onAsrResult: " + text);
//         try {
//             server.broadcast(new JSONObject()
//                 .put("command", "ask")
//                 .put("id", ask_id)
//                 .put("reply", text)
//                 .put("status", "completed")
//                 .toString());
//         } catch (JSONException e) {
//             e.printStackTrace();
//         }
//         robot.finishConversation();
//     }
    
//     public void stopRecognition() {
//         if (client != null) {
//             client.shutdown();
//         }
//         // 移除 ASR 监听器
//         if (robot != null) {
//             robot.removeAsrListener(this);
//         }
//     }
// } 