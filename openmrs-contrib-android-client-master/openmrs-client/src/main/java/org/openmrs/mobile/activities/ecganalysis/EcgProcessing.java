package org.openmrs.mobile.activities.ecganalysis;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.ecganalysis.OSEAFactory;
import org.openmrs.mobile.activities.ecganalysis.QRSDetector2;
import lib.folderpicker.FolderPicker;

import static java.lang.Thread.interrupted;
import static org.openmrs.mobile.api.UploadService.upload;

public class EcgProcessing extends ACBaseActivity{

    private static final String TAG = "Debug tag";
    private static int pulse=0;
    LineChart lineChart;
    static float[] datas=new float[3];
    int sampleRate=250;
    static int j=0;
    static int z=0;
    static int line_num = 0;
    final DecimalFormat df = new DecimalFormat("#.##");
    int skipline = 2;
    static int state=1,bno=0,vfno=0;
    int flag=0;
    int flag1=0;
    static boolean indicator=false;
    static boolean stop = true;
    static int temp1=0,temp2=0;
    static int[] episode=new int[6];
    static ArrayList<String> beat_ann = new ArrayList<String>();
    static ArrayList<Float> rr_interval= new ArrayList<Float>();
    ArrayList<String> dataAL = new ArrayList<String>();
    ArrayList<Integer> qrs_index= new ArrayList<Integer>();
    ArrayList<Float> hrate_samples= new ArrayList<Float>();
    static int k=0;
    private static final int FILE_PICKER_CODE = 786;
    Thread th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_processing);
        isStoragePermissionGranted();
        pulse=0;
        j=0;z=0;k=0;
        line_num = 0;
        skipline = 2;
        state=1;bno=0;vfno=0;
        flag=0;
        flag1=0;
        stop = true;
        indicator=false;
        temp1=0;temp2=0;
        beat_ann.clear();
        rr_interval.clear();
        dataAL.clear();
        qrs_index.clear();hrate_samples.clear();
        init();
        final TextView bigem=(TextView)findViewById(R.id.bigem);
        final TextView trigem=(TextView)findViewById(R.id.trigem);
        final TextView tach=(TextView)findViewById(R.id.tach);
        final TextView flutter=(TextView)findViewById(R.id.flutter);
        final TextView couplet=(TextView)findViewById(R.id.couplet);
        final TextView b2block=(TextView)findViewById(R.id.h2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bigem.setText((df.format(episode[0])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trigem.setText((df.format(episode[1])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tach.setText((df.format(episode[2])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flutter.setText((df.format(episode[3])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                couplet.setText((df.format(episode[4])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b2block.setText((df.format(episode[5])));
            }
        });


    }

    @Override
    protected void onPause() {
        System.out.print("In Pause");

        super.onPause();

        // unbindService(mServiceConnection);
        // mBluetoothLeService = null;
    }


    @Override
    protected void onDestroy() {
        System.out.print("In Destroy");
        stop = false;
        super.onDestroy();

        // unbindService(mServiceConnection);
        // mBluetoothLeService = null;
    }



    private void init() {

        lineChart = (LineChart) findViewById(R.id.main_lineChart);
        Legend l = lineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.GREEN);
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[]{"Set1", "Set2", "Set3", "Set4", "Set5"});
        lineChart.setViewPortOffsets(0, 20, 0, 0);

        //lineChart.setDrawGridBackground(true);
        lineChart.setScaleEnabled(true);

        lineChart.setData(new LineData());
        lineChart.setData(new LineData());
        lineChart.setBackgroundColor(Color.WHITE);
        // lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getXAxis().setDrawGridLines(true);
        lineChart.getXAxis().setTextColor(Color.BLACK);
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getXAxis().setTextColor(Color.BLACK);
        lineChart.getXAxis().setDrawLabels(true);
        // lineChart.getAxisRight().setDrawGridLines(true);
        //lineChart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setZeroLineWidth(5);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setAxisLineWidth(10);
        yAxis.setDrawLabels(true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        // lineChart.getAxisLeft().setAxisMinValue(-3);
        // lineChart.getAxisLeft().setAxisMaxValue(3);

        yAxis.setDrawGridLines(true);
        //lineChart.moveViewToX(1000);

        //lineChart.animateXY(100, 100) ;


        dataInit();

        feedMultiple();

    }


    private void dataInit() {
        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntryA(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // add a new x-value first
            for (int i = 0; i < 601; i++) {
                data.addXValue(String.valueOf(data.getXValCount()));
                data.addEntry(new Entry(0, set.getEntryCount()), 0);
            }
            //Log.d(TAG, "addEntryA: " + val);


            lineChart.notifyDataSetChanged();

            lineChart.setVisibleXRangeMaximum(600);
            lineChart.moveViewToX(data.getXValCount() - 601);
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private void feedMultiple() {

        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", "Select file to upload");
        intent.putExtra("location", Environment.getExternalStorageDirectory());
        intent.putExtra("pickFiles", true);

        //Optional

        startActivityForResult(intent, FILE_PICKER_CODE);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == FILE_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            final String folderLocation = intent.getExtras().getString("data");
            System.out.println(folderLocation);
            if(folderLocation.contains("ecg_time")){
                File file = new File(Environment.getExternalStorageDirectory().getPath()+"/ecg_time.csv");
                upload(file,"74e4ef11-6093-49df-81ac-e325b40f76ec");
                flag1=1;
            }
            else {
                File file = new File(Environment.getExternalStorageDirectory().getPath()+"/ecg_normal.csv");
                upload(file,"74e4ef11-6093-49df-81ac-e325b40f76ec");
                flag1=0;
            }

            System.out.println("Flag1: "+flag1);
            th = new Thread(new Runnable() {

                @Override
                public void run() {


                    try {
                        readCSV(folderLocation);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                }
            });
            th.start();
        }
    }
    public void readCSV(String csvPath) throws FileNotFoundException {


        int index1=0,index2=0;
        Float hratesum= (float) 0;
        Float hrateavg = (float)0;
        double hrv = (float)0;
        String item[] = new String[0];
        String qrsitem[] = new String[0];
        final TextView hrate = (TextView) findViewById(R.id.hr);
        final TextView hrvfield = (TextView) findViewById(R.id.hrv);
        final TextView analysis = (TextView) findViewById(R.id.analysis);
        final TextView analysis2 = (TextView) findViewById(R.id.analysis2);
        int qrspeaks = 0;
        double sumhrate = 0;
        BufferedReader reader;
        QRSDetector2 qrsDetector = null;
        Double ecgval=0.0;
        try {
            reader = new BufferedReader(new FileReader(csvPath));

            String line = null;//


            while ((line = reader.readLine()) != null && stop) {

                if (line_num < skipline) {
                    if(line_num == 1){
                        item = line.split(",");
                        sampleRate= (int) (1/Float.parseFloat(item[0]));
                        System.out.println("Sample Rate: "+sampleRate);
                        qrsDetector = OSEAFactory.createQRSDetector2(sampleRate);
                    }
                    line_num++;
                    continue;
                }
                item = line.split(",");

                dataAL.add(item[1]);
                // System.out.println(dataAL.get(line_num - skipline));

                qrs_index.add(line_num-skipline,0);

                int result = qrsDetector.QRSDet(Integer.parseInt(item[1]));
                if (result != 0) {
                    System.out.println("A QRS-Complex was detected at sample: " + (line_num-skipline-result) +" "+result);
                    qrs_index.add(line_num-skipline-result, 1);
                }
                if (flag1 ==1) {
                    ecgval = (Float.parseFloat(item[1]) * 0.00244);
                }
                else{
                    ecgval = (Float.parseFloat(item[1]) * 0.005);
                }
                addCustomEntry(String.valueOf(ecgval),"0");
                //addCustomEntry(item[0],String.valueOf(qrs_index.get(line_num-skipline-result)));
                if (qrs_index.get(line_num-skipline-result).equals(1)) {
                    index2 = index1;
                    index1 = line_num-skipline-result;
                    if (index2 == 0) {
                        System.out.println("First input");
                    } else {
                        final Float temp0 = (float) ((60 * sampleRate) / (index1 - index2));
                        rr_interval.add((float) (index1 - index2) / sampleRate);
                        hrate_samples.add(temp0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hrate.setText((df.format(temp0)
                                ));
                            }
                        });
                        System.out.println(temp0);
                        hratesum = hratesum + temp0;

                    }

                }
                if(rr_interval.size()>10 && z<rr_interval.size()-1){
                    System.out.println(z);
                    System.out.println(rr_interval.size());
                    hrv=(rr_interval.get(z)-rr_interval.get(z+1))*(rr_interval.get(z)-rr_interval.get(z+1))+hrv;
                    System.out.println("Heart rate variabilty: "+Math.sqrt(hrv/(rr_interval.size()-1)));
                    final double finalHrv = Math.sqrt(hrv/(rr_interval.size()-1));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hrvfield.setText((df.format(finalHrv)));
                        }
                    });
                    if((rr_interval.size()-j)>10) {
                        classifyBeat();
                        episode_detection(beat_ann);
                        // j++;
                    }
                    z++;
                }
                line_num++;
                Thread.sleep(1);
            }
            for(;z<rr_interval.size()-1;z++){
                hrv=(rr_interval.get(z)-rr_interval.get(z+1))*(rr_interval.get(z)-rr_interval.get(z+1))+hrv;

            }
            hrv=hrv/(rr_interval.size()-1);
            hrv=Math.sqrt(hrv);
            System.out.println("Heart rate variabilty: "+hrv);
            final double finalHrv = hrv;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hrvfield.setText((df.format(finalHrv)));
                }
            });
            hrateavg = hratesum/hrate_samples.size();
            System.out.println("Average heart rate: "+hrateavg);
            final Float finalHrateavg = hrateavg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hrate.setText((df.format(finalHrateavg)
                    ));
                }
            });
            if(hrateavg>100) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        analysis2.setText("Tachycardia - Fast Heart rate");
                    }
                });
            }
            else if(hrateavg<60){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        analysis2.setText("Bradycardia - Slow Heart rate");
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        analysis2.setText("Normal Heart rate");
                    }
                });
            }
            classifyBeat();
            episode_detection(beat_ann);


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int large=0;
        boolean check=false;
        int ind=0;
        for(int a=0;a<6;a++){
            if(episode[a]>large){
                large=episode[a];
                check=true;
                ind=a;
            }
        }
        if(check) {
            switch (ind) {
                case 0:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Ventricular Bigeminy Detected");
                        }
                    });
                    break;

                case 1:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Ventricular Trigeminy Detected");
                        }
                    });
                    break;

                case 2:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Ventricular Tachycardia Detected");
                        }
                    });
                    break;

                case 3:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Ventricular Flutter Detected");
                        }
                    });
                    break;

                case 4:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Ventricular couplets Detected");
                        }
                    });
                    break;

                case 5:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Second Degree Heart Block Detected ");
                        }
                    });
                    break;

                default:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis.setText("Normal");
                        }
                    });
                    break;
            }
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    analysis.setText("Normal");
                }
            });
        }


        System.out.println("Finished detection");
//        try {
//            FileWriter fw = new FileWriter(this.getFilesDir().getPath() + "/QRS1.csv", true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            System.out.println("Writing to file");
//            for (int i = 0; i < QRS.length; i++) {
//                bw.write(String.valueOf(QRS[i]));
//                bw.newLine();
//            }
//            bw.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Log.d("Completed qrs detection", "Parsing files");
     //   File file = new File(Environment.getExternalStorageDirectory().getPath()+"/ecg_normal.csv");

    }

    private void episode_detection(ArrayList<String> beat_ann) {

        System.out.println("Episode Detection");
        final TextView bigem=(TextView)findViewById(R.id.bigem);
        final TextView trigem=(TextView)findViewById(R.id.trigem);
        final TextView tach=(TextView)findViewById(R.id.tach);
        final TextView flutter=(TextView)findViewById(R.id.flutter);
        final TextView couplet=(TextView)findViewById(R.id.couplet);
        final TextView b2block=(TextView)findViewById(R.id.h2);
        String ch=null;
        for(;k<beat_ann.size();k++) {
            ch=beat_ann.get(k);
            //	System.out.println(state);
            System.out.println(ch);
            switch(state) {

                case 1:
                    flag=0;
                    bno=0;
                    vfno=0;
                    temp1=0;
                    temp2=0;
                    if(ch.equals("V")) {
                        state=7;
                        vfno=1;
                    }
                    else if (ch.equals("P")) {
                        state=2;
                        temp1=1;
                        temp2=1;
                    }
                    else if (ch.equals("B")) {
                        state=8;
                        bno=1;
                    }
                    else {
                        state=1;
                    }
                    break;

                case 2:
                    if(ch.equals("N")) {
                        state=3;
                        temp1++;
                        temp2++;

                    }
                    else if (ch.equals("P")) {
                        state=5;
                        flag=0;
                        temp1=0;
                        temp2=0;
                    }
                    else {
                        state=1;
                    }
                    break;

                case 3:
                    if(ch.equals("P")) {
                        state=2;
                        flag=1;
                        temp1++;
                        if (temp1==5) {
                            episode[0]=episode[0]+1;
                        }
                    }
                    else if (ch.equals("N")) {
                        state=4;
                        flag=2;
                        temp2++;
                    }
                    else {
                        state=1;
                    }
                    break;

                case 4:
                    if(ch.equals("P")) {
                        state=2;
                        flag=2;
                        temp2++;
                        if (temp2==7) {
                            episode[1]=episode[1]+1;
                        }
                    }
                    else {
                        state=1;
                        flag=0;
                    }
                    break;

                case 5:
                    if(ch.equals("P")) {
                        state=6;
                        episode[2]=episode[2]+1;

                    }
                    else {
                        state=1;
                        episode[4]=episode[4]+1;
                    }
                    break;

                case 6:
                    if(ch.equals("P")) {
                        state=6;
                    }
                    else if (ch.equals("V")) {
                        state=7;
                        vfno=1;
                    }
                    else if (ch.equals("B")) {
                        state=8;
                        bno=1;
                    }
                    else {
                        state=1;
                    }
                    break;

                case 7:
                    if(ch.equals("V")) {
                        state=7;
                        vfno++;
                        if(vfno==3) {
                            episode[3]=episode[3]+1;
                        }
                    }
                    else if (ch.equals("P")) {
                        state=2;
                        temp1=1;
                        temp2=1;
                    }
                    else if (ch.equals("B")) {
                        state=8;
                        bno=1;

                    }
                    else {
                        state=1;
                    }
                    break;

                case 8:
                    if(ch.equals("B")) {
                        state=8;
                        bno++;
                        if(bno==2) {
                            episode[5]=episode[5]+1;
                        }
                    }

                    else if (ch.equals("P")) {
                        state=2;
                        temp1=1;
                        temp2=1;
                    }
                    else if (ch.equals("V")) {
                        state=7;
                        vfno=1;
                    }
                    else {
                        state=1;
                    }
                    break;



            }

        }

        System.out.println(episode[0]+ ","+episode[1]+ ","+
                episode[2]+ ","+episode[3]+ ","+episode[4]+ ","+episode[5]);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bigem.setText((df.format(episode[0])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trigem.setText((df.format(episode[1])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tach.setText((df.format(episode[2])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flutter.setText((df.format(episode[3])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                couplet.setText((df.format(episode[4])));
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b2block.setText((df.format(episode[5])));
            }
        });

    }


    private static void classifyBeat() {

        int temp=0;
        int index=0;
        System.out.println("Classify Beat");
        System.out.println(j);
        System.out.println(rr_interval.size());
        if(j >= (rr_interval.size()-2)) {

            return;
        }

        for(;j<rr_interval.size()-2;j++) {
            System.out.println("Classification");
            window(j, rr_interval);
            // beat_ann.add("N");

            if(indicator){
                if (((datas[0] < 0.7) && (datas[1] < 0.7) && (datas[2] < 0.7)) || ((datas[0] + datas[1] + datas[2]) < 1.7)){
                    beat_ann.add("V");
                    pulse++;
                }
                else{
                    indicator=false;
                    index=j;
                    if (pulse < 4) {
                        temp = pulse;
                        System.out.println("in pulse ");
                        while (pulse != 0) {

                            beat_ann.set(j - pulse, "N");
                            pulse--;
                        }
                        j = j - temp;
                        window(j,rr_interval);

                    }
                    if (((datas[0] > (1.15 * datas[1])) && (datas[2] > 1.15 * datas[1])) ||
                            ((Math.abs(datas[0] - datas[1]) < 0.3) && ((datas[0] < 0.8) && (datas[1] < 0.8)) && (datas[2] > (1.2 * avg(datas[0], datas[1])))) ||
                            ((Math.abs(datas[1] - datas[2]) < 0.3) && ((datas[1] < 0.8) && (datas[2] < 0.8)) && (datas[0] > (1.2 * avg(datas[1], datas[2]))))) {

                        System.out.println("PVC detected " + j);
                        pulse=0;
                        if(j<index){
                            beat_ann.set(j, "P");
                        }
                        else{
                            beat_ann.add("P");
                        }

                    }

                    //THIRD RULE
                    else if (((datas[1] < 3) && (datas[1] > 2.2)) &&
                            ((Math.abs(datas[0] - datas[1]) < 0.2) ||
                                    (Math.abs(datas[1] - datas[2]) < 0.2))) {

                        System.out.println("Block detected");
                        if(j<index) {
                            beat_ann.set(j, "B");
                        }
                        else{
                            beat_ann.add("B");
                        }
                        pulse=0;
                    }
                    // }
                    else{
                        if(j<index) {
                            beat_ann.set(j, "N");
                        }
                        else{
                            beat_ann.add("N");
                        }
                        pulse=0;
                    }

                }
            }
            else{
                if(j>=index){
                    beat_ann.add("N");
                }
                if ((datas[1] < 0.6) && (datas[0] > (1.8 * datas[1]))) {
                    beat_ann.set(j, "V");
                    pulse=1;
                    indicator=true;
                }
                else if (((datas[0] > (1.15 * datas[1])) && (datas[2] > 1.15 * datas[1])) ||
                        ((Math.abs(datas[0] - datas[1]) < 0.3) && ((datas[0] < 0.8) && (datas[1] < 0.8)) && (datas[2] > (1.2 * avg(datas[0], datas[1])))) ||
                        ((Math.abs(datas[1] - datas[2]) < 0.3) && ((datas[1] < 0.8) && (datas[2] < 0.8)) && (datas[0] > (1.2 * avg(datas[1], datas[2]))))) {

                    System.out.println("PVC detected " + j);
                    pulse=0;
                    beat_ann.set(j, "P");

                }

                //THIRD RULE
                else if (((datas[1] < 3) && (datas[1] > 2.2)) &&
                        ((Math.abs(datas[0] - datas[1]) < 0.2) ||
                                (Math.abs(datas[1] - datas[2]) < 0.2))) {

                    System.out.println("Block detected");
                    beat_ann.set(j, "B");
                    pulse=0;
                }
                // }
                else{
                    beat_ann.set(j,"N");
                    pulse=0;
                }
            }
        }
    }


    private static float avg(float a,float b) {
        return ((a+b)/2);
    }


    private static void window(int index,ArrayList<Float> rr_interval) {
        //System.out.println(rr_interval.get(index));
        datas[0]=rr_interval.get(index);
        datas[1]=rr_interval.get(index+1);
        datas[2]=rr_interval.get(index+2);
        //System.out.println(datas[0]+" ,"+datas[1]+" ,"+datas[2]);
    }

    private void addCustomEntry(String ecgVal, String movingVal) {

        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            // set.addEntryA(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            if (set2 == null) {
                set2 = createSetB();
                data.addDataSet(set2);
            }

            // add a new x-value first
            data.addXValue(String.valueOf(data.getXValCount()));
            //Log.d(TAG, "addEntryA: " + ecgVal + " movWindVal: " + movingVal);
            data.addEntry(new Entry(Float.parseFloat(ecgVal), set.getEntryCount()), 0);
            data.addEntry(new Entry(Float.parseFloat(movingVal), set.getEntryCount()), 1);


            lineChart.notifyDataSetChanged();

            lineChart.setVisibleXRangeMaximum(600);
            lineChart.moveViewToX(data.getXValCount() - 601);
        }
    }


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(getResources().getColor(R.color.colorPrimaryDark));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0.1f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSetB() {
        LineDataSet setB = new LineDataSet(null, "Next Level Vals");
        setB.setAxisDependency(YAxis.AxisDependency.LEFT);
        setB.setColor(getResources().getColor(R.color.colorAccent));
        setB.setCircleColor(Color.BLACK);
        setB.setLineWidth(2f);
        setB.setCircleRadius(0.1f);
        setB.setFillAlpha(65);
        setB.setFillColor(ColorTemplate.getHoloBlue());
        setB.setHighLightColor(Color.rgb(244, 117, 117));
        setB.setValueTextSize(9f);
        setB.setDrawValues(false);
        return setB;
    }



    private void addEntry(double val, int type) {
        LineData data = lineChart.getData();
        ILineDataSet set = data.getDataSetByIndex(type);

        Log.d(TAG, "addEntry: " + val + " Type: " + type + " EntryCount:" + set.getEntryCount());


        data.addEntry(new Entry((float) val, set.getEntryCount()), type);

        lineChart.notifyDataSetChanged();

        lineChart.setVisibleXRangeMaximum(600);
        lineChart.moveViewToX(data.getXValCount() - 601);
    }

    @Override
    public void onBackPressed(){

        stop=false;
        System.out.print("Back Pressed");
        super.onBackPressed();
        this.finish();

    }


}
