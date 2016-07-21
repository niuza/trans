package com.niuza.trans.p2p;

// Copyright 2011 Google Inc. All Rights Reserved.

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.niuza.trans.MainActivity;
import com.niuza.trans.ui.DeviceDetailFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
//创建一个处理异步请求的服务，接收intent
public class FileTransferService extends IntentService {


    //定义socket传输过程中的一些常量
    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    //实现虚函数，根据intent的不同处理事务
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        //intent是发送文件，则获取相关信息
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            //获取intent中包含的文件地址
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);



            //开始socket传输，先获取intent中的端口
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(MainActivity.TAG, "正在打开客户端socket - ");


                //给socket命名
                socket.bind(null);

                //socket连接，设置timeout
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(MainActivity.TAG, "客户端socket状态:" + socket.isConnected());

                //获取输出流
                OutputStream stream = socket.getOutputStream();

                //获取当前应用的ContentResolver
                ContentResolver cr = context.getContentResolver();


                //创建输入流
                InputStream is = null;
                try {
                    //TODO wifi直连底层,通过Socket传输数据
                    //通过当前应用的ContentResolover进行数据读取
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(MainActivity.TAG, e.toString());
                }
                DeviceDetailFragment.copyFile(is, stream);
                Log.d(MainActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
