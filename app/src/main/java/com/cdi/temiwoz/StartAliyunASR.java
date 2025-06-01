package com.cdi.temiwoz;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;

import com.cdi.temiwoz.ConfigLoader;
import com.cdi.temiwoz.TokenManager;

/**
 * 此示例演示了从麦克风采集语音并实时识别的过程
 */
public class StartAliyunASR {
    private static final String TAG = "StartAliyunASR";
    private static StartAliyunASR instance;
    private static final Object lock = new Object();
    
    private String appKey;
    private String accessToken;
    private String accessKeyId;
    private String accessKeySecret;
    private NlsClient client;
    private boolean isProcessing = false;
    
    // 私有构造函数，自动从ConfigLoader和TokenManager获取参数
    private StartAliyunASR() {
        // 从ConfigLoader获取appKey
        this.appKey = ConfigLoader.getAppKey();
        this.accessKeyId = ConfigLoader.getAccessKeyId();
        this.accessKeySecret = ConfigLoader.getAccessKeySecret();
        // 从TokenManager获取token
        TokenManager tokenManager = TokenManager.getInstance();
        this.accessToken = tokenManager.getToken();
        String url = "wss://nls-gateway.cn-shanghai.aliyuncs.com/ws/v1";

        // 创建NlsClient实例,应用全局创建一个即可
        if(url.isEmpty()) {
            client = new NlsClient(accessToken);
        }else {
            client = new NlsClient(url, accessToken);
        }
    }
        
    /**
     * 获取单例实例，自动初始化
     */
    public static StartAliyunASR getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new StartAliyunASR();
                }
            }
        }
        return instance;
    }
    
    /**
     * 刷新Token（当Token过期时调用）
     */
    public void refreshToken() {
        TokenManager tokenManager = TokenManager.getInstance();
        this.accessToken = tokenManager.getToken();
        // 如果需要重新创建client
        if (client != null) {
            client.shutdown();
        }
        client = new NlsClient(accessToken);
    }
    
    /**
     * 释放资源
     */
    public void shutdown() {
        isProcessing = false;
        if (client != null) {
            client.shutdown();
            client = null;
        }
        instance = null;
    }

    public SpeechTranscriberListener getTranscriberListener() {
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {
            //识别出中间结果.服务端识别出一个字或词时会返回此消息.仅当setEnableIntermediateResult(true)时,才会有此类消息返回
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                // 重要提示： task_id很重要，是调用方和服务端通信的唯一ID标识，当遇到问题时，需要提供此task_id以便排查
                Log.d(TAG, "name: " + response.getName() +
                    //状态码 20000000 表示正常识别
                    ", status: " + response.getStatus() +
                    //句子编号，从1开始递增
                    ", index: " + response.getTransSentenceIndex() +
                    //当前的识别结果
                    ", result: " + response.getTransSentenceText() +
                    //当前已处理的音频时长，单位是毫秒
                    ", time: " + response.getTransSentenceTime());
            }

            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {
                Log.d(TAG, "task_id: " + response.getTaskId() +
                    "name: " + response.getName() +
                    ", status: " + response.getStatus());
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                Log.d(TAG, "task_id: " + response.getTaskId() +
                    "name: " + response.getName() +
                    ", status: " + response.getStatus());
            }

            //识别出一句话.服务端会智能断句,当识别到一句话结束时会返回此消息
            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                Log.d(TAG, "name: " + response.getName() +
                    //状态码 20000000 表示正常识别
                    ", status: " + response.getStatus() +
                    //句子编号，从1开始递增
                    ", index: " + response.getTransSentenceIndex() +
                    //当前的识别结果
                    ", result: " + response.getTransSentenceText() +
                    //置信度
                    ", confidence: " + response.getConfidence() +
                    //开始时间
                    ", begin_time: " + response.getSentenceBeginTime() +
                    //当前已处理的音频时长，单位是毫秒
                    ", time: " + response.getTransSentenceTime());
            }

            //识别完毕
            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                Log.d(TAG, "task_id: " + response.getTaskId() +
                    ", name: " + response.getName() +
                    ", status: " + response.getStatus());
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                // 重要提示： task_id很重要，是调用方和服务端通信的唯一ID标识，当遇到问题时，需要提供此task_id以便排查
                Log.e(TAG, 
                    "task_id: " + response.getTaskId() +
                        //状态码 20000000 表示识别成功
                        ", status: " + response.getStatus() +
                        //错误信息
                        ", status_text: " + response.getStatusText());
            }
        };

        return listener;
    }

    public void process() {
        if (isProcessing) {
            Log.d(TAG, "Already processing, ignoring call");
            return;
        }
        
        isProcessing = true;
        
        // 在后台线程中执行音频处理
        new Thread(new Runnable() {
            @Override
            public void run() {
                processAudio();
            }
        }).start();
    }
    
    private void processAudio() {
        SpeechTranscriber transcriber = null;
        AudioRecord audioRecord = null;
        try {
            // 创建实例,建立连接
            transcriber = new SpeechTranscriber(client, getTranscriberListener());
            transcriber.setAppKey(appKey);
            // 输入音频编码方式
            transcriber.setFormat(InputFormatEnum.PCM);
            // 输入音频采样率
            transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
            // 是否返回中间识别结果
            transcriber.setEnableIntermediateResult(true);
            // 是否生成并返回标点符号
            transcriber.setEnablePunctuation(true);
            // 是否将返回结果规整化,比如将一百返回为100
            transcriber.setEnableITN(false);

            //此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
            transcriber.start();

            // 初始化 AudioRecord
            int minBufferSize = AudioRecord.getMinBufferSize(
                16000, // 采样率
                AudioFormat.CHANNEL_IN_MONO, // 单声道
                AudioFormat.ENCODING_PCM_16BIT // 16位PCM编码
            );
            
            // 确保缓冲区大小足够
            int bufferSize = Math.max(minBufferSize, 3200);
            
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, // 麦克风音频源
                16000, // 采样率
                AudioFormat.CHANNEL_IN_MONO, // 单声道
                AudioFormat.ENCODING_PCM_16BIT, // 16位PCM编码
                bufferSize // 缓冲区大小
            );
            
            audioRecord.startRecording();
            Log.d(TAG, "开始录音，您可以说话了！");
            
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            
            // 读取麦克风数据并发送到识别服务
            while (isProcessing && (bytesRead = audioRecord.read(buffer, 0, bufferSize)) > 0) {
                // 发送麦克风数据到识别服务
                if (bytesRead > 0) {
                    transcriber.send(buffer, bytesRead);
                }
            }
            
            Log.d(TAG, "停止录音");
            transcriber.stop();
            
        } catch (Exception e) {
            Log.e(TAG, "语音识别异常: " + e.getMessage(), e);
        } finally {
            isProcessing = false;
            
            // 释放资源
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
            
            if (transcriber != null) {
                transcriber.close();
            }
        }
    }
    
    public void stopProcess() {
        isProcessing = false;
    }

    public static void main(String[] args) throws Exception {
        StartAliyunASR asr = StartAliyunASR.getInstance();
        asr.process();
        asr.shutdown();
    }
}
