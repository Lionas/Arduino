package jp.lionas.android.arduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Arduino_Activity extends Activity {

    private static final String TAG = "Bluetooth";

    private static final int REQUEST_DISCOVERY = 0x1;
	private Handler _handler = new MainHandler();

    private static final int CONNECT = 1;
    private static final int DISCONNECT = 2;
    private static final int READ = 3;
    private static final int SEND = 4;

    private BluetoothSocket socket = null;
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	private String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private ImageView resultImage;
	private String inputResult = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!_bluetooth.isEnabled()) {
            finish();
            return;
        }

        setContentView(R.layout.main);
        resultImage = (ImageView) findViewById(R.id.ImageView01);

        Button read = (Button) findViewById(R.id.Button01);
        read.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
	            Log.d(TAG, "in read onClick.");
	        	Message msg = new Message();
	        	msg.what = READ;
	        	_handler.sendMessage(msg);
			}
		});

        Intent intent = new Intent(this, DiscoveryActivity.class);
        Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();
        startActivityForResult(intent, REQUEST_DISCOVERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != REQUEST_DISCOVERY) {
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }

        final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        new Thread() {
            public void run() {
            	connect(device);
            };
        }.start();
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
							socket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
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
//    								if( results[i].length() == 10 && !results[i].equalsIgnoreCase(inputResult)) {
       								if( results[i].length() == 10 ) {
    									inputResult = results[i];
    									procFromUUID(inputResult);
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
                        Log.d(TAG, "sendData OK.");
                        byte[] sendData = (byte[])msg.obj;
    		            Log.d(TAG, "send:" + new String(sendData));
	    				try {
	    					OutputStream out;
	    					out = socket.getOutputStream();
	    					out.write(sendData);
	    		            Log.d(TAG, "get output.");
	    		            //resultText.append("send:" + new String(sendData));
	    				} catch (IOException e) {
	    					e.printStackTrace();
	    				}
                    }
                    break;
                }
            }
        }

        void procFromUUID(String inputResult) {

        	if( inputResult.equals("220055D24F") ) {
   				resultImage.setImageResource(R.drawable.dai);
        	}
        	else if( inputResult.equals("220056ED83") ) {
        		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.co.jp/"));
                startActivity(intent);
        	}
        	else{
        		resultImage.setImageBitmap(null);
        	}
        }
    }

}