package jp.lionas.android.arduino.tp.mushroom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;
import backport.android.bluetooth.protocols.BluetoothProtocols;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ArduinoTPMushroom extends Activity {

	static final String ARDUINO_NAME = "ARDUINO Duemilanove";
    private static final String TAG = "TPMushroom";

	private Handler _handler = new MainHandler();

    // Bluetooth用
    private static final int REQUEST_DISCOVERY = 0x1;
    private static final int TEMPERATURE_CMD = 1;
    private static final int DISCONNECT = 2;
    private BluetoothSocket socket = null;
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

	// simejiマッシュルーム用
	private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
	private static final String REPLACE_KEY = "replace_key";
	private String _ReplaceString;
	private static final String TEMPERATURE = "きおん";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Bluetooth使用可否
        if (!_bluetooth.isEnabled()) {
            finish();
            return;
        }

        // インテント取得
		Intent it = getIntent();
		String action = it.getAction();
		if (action != null && ACTION_INTERCEPT.equals(action)) {
			// from Simeji
			_ReplaceString = it.getStringExtra(REPLACE_KEY);

			// TEMPERATURE変換文字列でなかったそのまま返す
			if ( !(_ReplaceString.equalsIgnoreCase(TEMPERATURE)) ) {
	            // simejiへ戻す
	    		Intent data = new Intent();
	    		data.putExtra(REPLACE_KEY, _ReplaceString);
	    		setResult(RESULT_OK, data);
	    		finish();
			}else{
		        // Bluetoothデバイス探索
		        Intent intent = new Intent(this, DiscoveryActivity.class);
		        Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();
		        startActivityForResult(intent, REQUEST_DISCOVERY);
			}
		} else {
			// from others
			finish();
		}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	// 処理続行不可
        if (requestCode != REQUEST_DISCOVERY || resultCode != RESULT_OK) {
            // simejiへ戻す
    		Intent intent = new Intent();
    		intent.putExtra(REPLACE_KEY, _ReplaceString);
    		setResult(RESULT_OK, intent);
    		finish();
        }else{
	        // Bluetooth温度取得開始
	        final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        new Thread() {
	            public void run() {
	            	getTemperature(device);
	            };
	        }.start();
        }
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
    	Message msg = new Message();
    	msg.what = DISCONNECT;
    	_handler.sendMessage(msg);
	}

	protected void getTemperature(BluetoothDevice device) {
    	Message msg = new Message();
    	msg.what = TEMPERATURE_CMD;
    	msg.obj = device;
    	_handler.sendMessage(msg);
    }

// ---------------------------- Handler classes ---------------------------------

    /** This Handler is used to post message back onto the main thread of the application */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case TEMPERATURE_CMD: {
                    if (msg.obj != null && msg.obj instanceof BluetoothDevice) {
                        Log.d(TAG, "createRfcommSocketToServiceRecord.");
                    	BluetoothDevice device = (BluetoothDevice)msg.obj;
                        try {
                        	// Bluetooth接続
							socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.RFCOMM_PROTOCOL_UUID, 1);
	                        socket.connect();

	                        // TEMPERATUREコマンド送信
	                        String arduino_cmd = "00001\n";
	                        byte[] sendData = arduino_cmd.getBytes();
	    					OutputStream out;
	    					out = socket.getOutputStream();
	    					out.write(sendData);
	    		            Log.d(TAG, "send-arduino:" + new String(sendData));

	    		            // 戻り値（気温）取得
	        	    		InputStream in;
	    					StringBuilder inStr = new StringBuilder();
	    					boolean finish = true;
	    		            String result = "";
	    					int readLen;
	        	    		byte[] b = new byte[1024];
	    					do {
	    						b = new byte[1024];
	        					in = socket.getInputStream();
	        					readLen = in.read(b);
	        		            inStr.append(new String(b, 0 , readLen));
	    						if( inStr.indexOf("\n") != -1 ) {
	    							result = inStr.toString().trim();
   			    		            finish = false;
	    						}
	    					}while(finish);
	    		            Log.d(TAG, "send-simeji:" + result);

        		            // simejiへ戻す
        		    		Intent data = new Intent();
        		    		data.putExtra(REPLACE_KEY, result + "℃");
        		    		setResult(RESULT_OK, data);
        		    		finish();
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
            }
        }
    }
}