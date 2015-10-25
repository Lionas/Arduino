package jp.co.brilliantservice.bluetooth;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BlueScan extends Activity {

  ListView lvDevices;

  @SuppressWarnings("unchecked")
  private ArrayList deviceArray;

  @SuppressWarnings("unchecked")
  private ArrayAdapter listAdapter;

  /** Called when the activity is first created. */
  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

      setContentView(R.layout.main);

      lvDevices = (ListView) findViewById(R.id.lvDevices);
      deviceArray = new ArrayList();
      listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceArray);
      lvDevices.setAdapter(listAdapter);

      String[] devices = btDeviceScan();

      for (String device: devices){
          deviceArray.add(device);
      }

      listAdapter.notifyDataSetChanged();

//      client();
//      server();
    }

    /* Our native method which receives an array of Strings back from
     * the C++ shared library
     */
    public native String[] btDeviceScan();
    public native void client();
    public native String server();

    static {
        System.loadLibrary("bluescan");
    }
}