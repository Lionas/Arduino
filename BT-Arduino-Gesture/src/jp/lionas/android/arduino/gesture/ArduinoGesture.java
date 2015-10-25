package jp.lionas.android.arduino.gesture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;
import backport.android.bluetooth.protocols.BluetoothProtocols;
import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ArduinoGesture extends Activity implements OnGesturePerformedListener {

	static final String ARDUINO_NAME = "ARDUINO Duemilanove";
    private static final String TAG = "Dron-kun_Gesture";

    private static final int REQUEST_DISCOVERY = 0x1;
	private Handler _handler = new MainHandler();

	// Bluetooth定義
    private static final int CONNECT = 1;
    private static final int DISCONNECT = 2;
    private static final int READ = 3;
    private static final int SEND = 4;

    // Bluetoothメンバ変数
    private BluetoothSocket socket = null;
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

	// ジェスチャー
    private GestureLibrary mLibrary;
    private final double precision = 2.0;	// 精度

	private String inputResult = null;
	private TextView statusView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        statusView = (TextView) findViewById(R.id.status);
        statusView.setText("");

        // Bluetooth使用可否チェック
        if (!_bluetooth.isEnabled()) {
            finish();
            return;
        }

        // ジェスチャーライブラリ取得
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {	// ロード失敗
        	finish();
        	return;
        }

        // ジェスチャーオーバーレイビュー設定
        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);

        // Bluetoothデバイス探索アクティビティ起動
        Intent intent = new Intent(this, DiscoveryActivity.class);
        Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();
        startActivityForResult(intent, REQUEST_DISCOVERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	// 処理続行不可
        if (requestCode != REQUEST_DISCOVERY || resultCode != RESULT_OK) {	return;	}

        // Bluetooth接続開始
        final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        new Thread() {
            public void run() {
            	connect(device);
            };
        }.start();
}
    // ジェスチャー検知後の処理
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			if (prediction.score > precision) {
	            Log.d(TAG, "GesturePerformed:Score" + predictions.get(0).score);
	        	Message msg = new Message();
	        	msg.what = SEND;
	        	String sendStr = predictions.get(0).name + "\n";
	        	Log.d(TAG,"format:"+sendStr);
	        	byte[] sendData = sendStr.getBytes();
	        	msg.obj = sendData;
	        	_handler.sendMessage(msg);
			}
		}
	}

    @Override
	protected void onDestroy() {
		super.onDestroy();
    	Message msg = new Message();
    	msg.what = DISCONNECT;
    	_handler.sendMessage(msg);
	}

	protected void connect(BluetoothDevice device) {
    	Message msg = new Message();
    	msg.what = CONNECT;
    	msg.obj = device;
    	_handler.sendMessage(msg);
        Log.d(TAG, "send CONNECT handler.");
    }

// ---------------------------- Handler classes ---------------------------------

    /** This Handler is used to post message back onto the main thread of the application */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT: {
		            Log.d(TAG, "in CONNECT handler.");
                    if (msg.obj != null && msg.obj instanceof BluetoothDevice) {
                        Log.d(TAG, "createRfcommSocketToServiceRecord.");
                    	BluetoothDevice device = (BluetoothDevice)msg.obj;
                        try {
							socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.RFCOMM_PROTOCOL_UUID, 1);
	                        socket.connect();
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
                    break;
                }
                case DISCONNECT: {
		            Log.d(TAG, "in DISCONNECT handler.");
                	if(socket == null) {
                		return;
                	}
                	try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
	                	socket = null;
					}
                    break;
                }
                case READ: {
		            Log.d(TAG, "in READ handler.");
                	if(socket == null) {
                		return;
                	}
    	    		byte[] b;
    				try {
    					InputStream in;
    					StringBuilder inStr = new StringBuilder();
    					boolean finish = true;
    					int readLen = 0;
    					do {
    						b = new byte[1024];
        					in = socket.getInputStream();
        					readLen = in.read(b);
        		            inStr.append(new String(b, 0 , readLen));
    						if( inStr.indexOf("\n") != -1 ) {
    							String results[] = inStr.toString().split("\n");
    							for(int i = 0; i < results.length; i++ ) {
       								if( results[i].length() == 10 ) {
    									inputResult = results[i];
//    									procFromUUID(inputResult);
    						        	Log.d(TAG, "read:" + inputResult);
    			    		            finish = false;
    								}
    							}
    						}
    					}while(finish);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
                    break;
                }
                case SEND: {
		            Log.d(TAG, "in SEND handler.");
                	if(socket == null) {
                		return;
                	}
                    if (msg.obj != null && msg.obj instanceof byte[]) {
                        byte[] sendData = (byte[])msg.obj;
	    				try {
	    					OutputStream out;
	    					out = socket.getOutputStream();
	    					out.write(sendData);
	    		            Log.d(TAG, "send:" + new String(sendData));
	    		            statusView.setText(new String(sendData).replace('*',' '));
	    				} catch (IOException e) {
	    					e.printStackTrace();
	    				}
                    }
                    break;
                }
            }
        }

    }

}