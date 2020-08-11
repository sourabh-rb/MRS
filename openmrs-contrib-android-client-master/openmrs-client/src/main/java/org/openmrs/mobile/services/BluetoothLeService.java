package org.openmrs.mobile.services;

import  android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import 	java.util.Timer;
import java.util.TimerTask;
import org.openmrs.mobile.activities.deviceble.*;
import org.openmrs.mobile.utilities.ToastUtil;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static int flag;
    private static int flag1=6;
    private static boolean flagwrite=true;
    private static int count=0;
    private static int temp;
    private static int PRESENT_STATE = 0;
    private static final int FLASH_ERASE = 0;
    private static final int START_CONVERSION = 1;
    private static final int LEADSELECT_1 = 2;

    private static final int LEADSELECT_2 = 3;
    private static final int LEADSELECT_3 = 4;
    private static final int ENDOFCONV = 5;

    private static final int LEADSELECT_1_TMP = 6;
    private static final int LEADSELECT_1_RX = 7;
    private static final int LEADSELECT_2_TMP = 8;
    private static final int LEADSELECT_2_RX = 9;
    private static final int LEADSELECT_2_INIT = 14;
    private static final int LEADSELECT_3_INIT = 10;
    private static final int LEADSELECT_3_TMP = 11;
    private static final int LEADSELECT_3_RX = 12;
    private static final int STATEIDLE = 13;

    private static byte[] ble_data1 = new byte[9620];
    private static byte[] ble_data2 = new byte[9620];
    private static byte[] ble_data3 = new byte[9620];

    public ArrayList<Integer> lead1=new ArrayList<Integer>();
    public ArrayList<Integer> lead2=new ArrayList<Integer>();
    public ArrayList<Integer> lead3=new ArrayList<Integer>();

    public ArrayList<Integer> lead1mod=new ArrayList<Integer>();
    public ArrayList<Integer> lead2mod=new ArrayList<Integer>();
    public ArrayList<Integer> lead3mod=new ArrayList<Integer>();

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    //  public final static UUID UUID_HEART_RATE_MEASUREMENT =
    //       UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("0bd51666-e7cb-469b-8e4d-2742f1ba77cc");

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);                                                                                                                                           // count=-700; temp=count;
                Log.i(TAG, "Connected to `GATT server.");
                PRESENT_STATE = FLASH_ERASE;
                count = 0;
                // Attempts to discover services after successful connection.
                boolean status1 = mBluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery:" + status1);


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                /*check if the service is available on the device*/
                BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("0bd51666-e7cb-469b-8e4d-2742f1ba77cc"));
                if (mCustomService == null) {
                    Log.w(TAG, "Custom BLE Service not found");
                    return;
                }
                /*get the read characteristic from the service*/
                BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("e7add780-b042-4876-aae1-112855353cc1"));
                mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                setCharacteristicNotification(mWriteCharacteristic, true);
                Log.w(TAG, "Successfuly connected to ECG Mote");
                showToast("Connected to ECG Mote");
               // count = 0;
                //PRESENT_STATE = FLASH_ERASE;

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
            count = 0;
            PRESENT_STATE = FLASH_ERASE;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            Log.w(TAG, "Inside onCharacteristicRead ");

            StringBuilder sb = new StringBuilder();
            byte[] a = characteristic.getValue();
            for (byte b : a) {
                sb.append(String.format("%02x", b & 0xff));
                Log.w(TAG, String.format("%02x", b & 0xff));
            }


            Log.w(TAG, "data back: " + sb.toString());
            Log.w(TAG, "data back: " + characteristic.getStringValue(1));

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status)
        {
            Log.w("Write","Present : "+PRESENT_STATE);

        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            StringBuilder sb = new StringBuilder();
            byte[] a = characteristic.getValue();
            flag = 1;
            int i = 0;

            Log.w("Change","Present : "+PRESENT_STATE);
            switch (PRESENT_STATE)
            {
                case FLASH_ERASE:
                    if (a[0] == 'A')
                    {
                        PRESENT_STATE = START_CONVERSION;
                        Log.w(TAG, "Erase done ");
                        writeCustomCharacteristic('S');
                    }
                    break;
                case START_CONVERSION:
                    if (a[0] == 'A')
                    {
                        PRESENT_STATE = START_CONVERSION;
                        Log.w(TAG, "got START_CONVERSION 1st ACK ");
                        //writeCustomCharacteristic(0x49);
                    } else if (a[0] == 'D') {
                        PRESENT_STATE = LEADSELECT_1;
                        Log.w(TAG, "got START_CONVERSION 2nd ACK(Conversion complete)");
                        writeCustomCharacteristic('1');
                    }
                    break;

                case LEADSELECT_1:
                    if (a[0] == 'Q')
                    {
                        showToast("Starting Lead1 Data");
                        PRESENT_STATE = LEADSELECT_1_TMP;
                    }
                    break;

                case LEADSELECT_1_RX:
                    if (count <= 9600)
                    {
                        for (byte b : a)
                        {
                            ble_data1[count] = b;
                            count++;
                            lead1.add((int) Long.parseLong(String.format("%02x", b & 0xff), 16));
                        }
                    }

                    Log.w(TAG, "count " + count );
                    if ( (count >9600))
                    {
                        Log.w(TAG, "Lead1 Data completed" + count);
                        showToast("Lead1 Data completed");
                        PRESENT_STATE = LEADSELECT_2_INIT;
                        count = 0;

                    }
                    else
                    if(count%20 == 0)
                    {
                        Log.w(TAG, "sanood " + count);
                        PRESENT_STATE = LEADSELECT_1_TMP;
                    }
                    break;

                case LEADSELECT_2:
                    if (a[0] == 'Q')
                    {
                        Log.w(TAG, "Entering Lead 2");
                        showToast("Starting Lead2 Data");
                        PRESENT_STATE = LEADSELECT_2_TMP;
                    }
                    break;
                case LEADSELECT_2_RX:
                    for (byte b : a)
                    {
                        ble_data2[count] = b;
                        count++;
                        lead2.add((int) Long.parseLong(String.format("%02x", b & 0xff),16));
                    }

                    Log.w(TAG, "count " + count );
                    if ( (count >9600))
                    {
                        Log.w(TAG, "Lead2 Data completed" + count);
                        showToast("Lead2 Data completed");
                        PRESENT_STATE = LEADSELECT_3_INIT;
                        count = 0;

                    }
                    else
                    if(count%20 == 0)
                    {
                        Log.w(TAG, "sanood " + count);
                        PRESENT_STATE = LEADSELECT_2_TMP;
                    }

                    break;


                case LEADSELECT_3:
                    if (a[0] == 'Q')
                    {
                        Log.w(TAG, "Entering Lead 3");
                        showToast("Starting Lead3 Data");
                        PRESENT_STATE = LEADSELECT_3_TMP;
                    }
                    break;
                case LEADSELECT_3_RX:
                    for (byte b : a)
                    {
                        ble_data3[count] = b;
                        count++;
                        lead3.add((int) Long.parseLong(String.format("%02x", b & 0xff),16));
                    }

                    Log.w(TAG, "count " + count );
                    if ( (count >9600))
                    {
                        Log.w(TAG, "All leads completed" + count);
                        showToast("Lead3 Data completed");
                        PRESENT_STATE = STATEIDLE;
                        count = 0;

                    }
                    else
                    if(count%20 == 0)
                    {
                        Log.w(TAG, "sanood " + count);
                        PRESENT_STATE = LEADSELECT_3_TMP;
                    }

                    break;


                case STATEIDLE:

                    break;
                default:

                    break;
            }

            if(PRESENT_STATE==LEADSELECT_1_TMP)
                Log.w(TAG, "count  " + count + "  From Lead 1");
            if(PRESENT_STATE==LEADSELECT_2_TMP)
                Log.w(TAG, "count  " + count + "  From Lead 2");
            if(PRESENT_STATE==LEADSELECT_3_TMP)
                Log.w(TAG, "count  " + count + "  From Lead 3");




               /* String state;
                state =Environment.getExternalStorageState();
                if(Environment.MEDIA_MOUNTED.equals(state))
                {
                    File  Root= Environment.getExternalStorageDirectory();
                    File Dir= new File(Root.getAbsolutePath()+"/ECG_DATA");
                    if(!Dir.exists())
                    {
                       Dir.mkdir();
                    }
                    File file=new File(Dir,"Lead_data.txt");

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"SD card Not found",Toast.LENGTH_LONG).show();
                }*/

            if(PRESENT_STATE==STATEIDLE) {
//                ToastUtil.notify("Finished Data Capture. Writing to File...");
               showToast("Writing to File");
                Log.w(TAG, "Message Saved");

             //   File file1 = new File(Environment.getExternalStorageDirectory(), "Lead_data_9600.csv");
                File file2 = new File(Environment.getExternalStorageDirectory(), "/Lead_data_3200.csv");

                String csvjoin;
                String[] array = new String[4];
//                try {

                //    FileWriter fw1 = new FileWriter(file1);
//
//                    for (int j = 0; j < lead1.size(); j++)
//                    {
//                        array[0]=String.valueOf(0.00156*i);
//                        array[1]= Integer.toString(lead1.get(j));
//                        array[2]= Integer.toString(lead2.get(j));
//                        array[3]= Integer.toString(lead3.get(j));
//                        csvjoin = TextUtils.join(",",array);
//                        System.out.print(csvjoin);
//                        fw1.write(csvjoin + "\r\n");
//                    }
//                    fw1.close();
//
//                } catch (FileNotFoundException e) {
//                    Log.w(TAG, "FileNotFoundException");
//                    e.printStackTrace();
//
//                } catch (IOException e) {
//                    Log.w(TAG, "IOException");
//                    e.printStackTrace();
//                }

                for(int j=0;j<3200;j++){
                    lead1mod.add(((lead1.get(3*j)*65536)+(lead1.get(3*j+1)*256)+lead1.get(3*j+2)));
                    lead2mod.add(((lead2.get(3*j)*65536)+(lead2.get(3*j+1)*256)+lead2.get(3*j+2)));
                    lead3mod.add(((lead3.get(3*j)*65536)+(lead3.get(3*j+1)*256)+lead3.get(3*j+2)));
                }

                try {


                    FileWriter fw2 = new FileWriter(file2);
                    long total1=0,total2=0,total3=0;
                    long avg1=0,avg2=0,avg3=0;
                    for (int j = 0; j < lead1mod.size(); j++)
                    {
                        total1+=lead1mod.get(j);
                        total2+=lead2mod.get(j);
                        total3+=lead3mod.get(j);
                    }
                    avg1=Math.round(total1/lead1mod.size());
                    avg2=Math.round(total2/lead2mod.size());
                    avg3=Math.round(total3/lead3mod.size());
                 //   System.out.println("Averages "+avg1+" "+avg2+" "+avg3);

                    int k=0;
                    Double time = 0.0;
                    while(k<4) {

                    for (int j = 0; j < lead1mod.size(); j++)
                    {
                        //Zero in time
                        array[0] = Double.toString(time);
                        time+=0.00156;
                        array[1]= Long.toString(lead1mod.get(j)-avg1);
                        array[2]= Long.toString(lead2mod.get(j)-avg2);
                        array[3]= Long.toString(lead3mod.get(j)-avg3);
                        csvjoin = TextUtils.join(",",array);
                        System.out.println(csvjoin);
                        fw2.write(csvjoin + "\r\n");
                    }
                        k++;
                    }
                    showToast("Completed File write");
                    fw2.close();


                } catch (FileNotFoundException e) {
                    Log.w(TAG, "FileNotFoundException");
                    e.printStackTrace();

                } catch (IOException e) {
                    Log.w(TAG, "IOException");
                    e.printStackTrace();
                }

            }
        }

    };

    // Manage data

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        initTimer();

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback, 2);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        //   if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        //  }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void readCustomCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("0bd51666-e7cb-469b-8e4d-2742f1ba77cc"));
        if (mCustomService == null) {
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("e7add780-b042-4876-aae1-112855353cc1"));
        if (mBluetoothGatt.readCharacteristic(mReadCharacteristic) == false) {
            Log.w(TAG, "Failed to read characteristic");
        }
    }

    public void writeCustomCharacteristic(int value)
    {
        Log.w(TAG, "Entering write characteristic");
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        flag = 0;
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("0bd51666-e7cb-469b-8e4d-2742f1ba77cc"));
        if (mCustomService == null)
        {
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("e7add780-b042-4876-aae1-112855353cc1"));
        //Log.w(TAG, "Inside writeCustomCharacteristic");
        //Log.w(TAG, "Value to be written" + value);
        mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        //mBluetoothGatt.setCharacteristicNotification(mWriteCharacteristic, true);
        // setCharacteristicNotification(mWriteCharacteristic,true);
        //  mWriteCharact eristic.setValue(value, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8, 0);

        byte[] bytes = new byte[20];
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.putInt(value);
        bb.flip();
        bytes = bb.array();
        mWriteCharacteristic.setValue(bytes);
        Log.w("in write characteristic",""+value);
        if (mBluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false)
        {
            flagwrite=false;
            if(value==50)
                Log.w(TAG, "Failed to write lead2characteristic ");
            else
                Log.w(TAG, "Failed to write other characteristic");
            if (PRESENT_STATE == LEADSELECT_1_RX)
            {
                PRESENT_STATE = LEADSELECT_1_TMP;
            }

        }
        else if(value==65) {
            Log.w(TAG, "Succesfully writing A");
            flagwrite = true;
        }
        else if(value==49) {
            Log.w(TAG, "Succesfully writing characteristic lead1");
            flagwrite = true;
        }
    }
    //}
    public boolean initTimer() {
        //Declare the timer
        Timer t = new Timer();
//Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      //Log.w(TAG, "timer active" );

                                      switch (PRESENT_STATE) {
                                          case LEADSELECT_1_TMP:
                                              PRESENT_STATE=LEADSELECT_1_RX;
                                              Log.w(TAG, "Sending A in timer" );
                                              writeCustomCharacteristic('A');
                                              break;

                                          case LEADSELECT_2_INIT:
                                              PRESENT_STATE=LEADSELECT_2;
                                              Log.w(TAG, "Sending 2 for lead2 in timer" );
                                              writeCustomCharacteristic('2');
                                              break;
                                          case LEADSELECT_2_TMP:
                                              PRESENT_STATE=LEADSELECT_2_RX;
                                              Log.w(TAG, "Sending A in timer" );
                                              writeCustomCharacteristic('A');
                                              break;

                                          case LEADSELECT_3_INIT:
                                              PRESENT_STATE=LEADSELECT_3;
                                              Log.w(TAG, "Sending 3 for lead3 in timer" );
                                              writeCustomCharacteristic('3');
                                              break;

                                          case LEADSELECT_3_TMP:
                                              PRESENT_STATE=LEADSELECT_3_RX;
                                              Log.w(TAG, "Sending A in timer" );
                                              writeCustomCharacteristic('A');
                                              break;
                                          //   default: Log.w(TAG, "No state inside  timer " );
                                          //      break;
                                          case ENDOFCONV:
                                              break;
                                      }

                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                  }

                              },
//Set how long before to start calling the TimerTask (in milliseconds)
                0,
//Set the amount of time between each execution (in milliseconds)
                100);
        return(true);
    }

    void showToast(String string){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.notify(string);
            }
        });
    }
}

