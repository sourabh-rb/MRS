package org.openmrs.mobile.api;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.openmrs.mobile.R;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.User;
import org.openmrs.mobile.utilities.ToastUtil;
import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sharon on 22/04/18.
 */

public class UploadService {

    public static void upload(File file, String uuid) {
        RestApi restApi = RestServiceBuilder.createService2(RestApi.class,"admin","Admin123");
 //     File file = new File(Environment.getExternalStorageDirectory().getPath()+"/Lead_data_3200.csv");
//        RequestBody formBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("patient", "74e4ef11-6093-49df-81ac-e325b40f76ec")
//                .addFormDataPart("fileCaption","trial2")
//                .addFormDataPart("task_file", "ecg_normal.csv", RequestBody.create(MediaType.parse("text/csv"), file))
//                .build();
        RequestBody requestfile = RequestBody.create(MediaType.parse("text/csv"), file);
        MultipartBody.Part uploadfile = MultipartBody.Part.createFormData("file", file.getName(), requestfile);
        RequestBody patient = RequestBody.create(MediaType.parse("text/plain"), uuid);
        RequestBody fileCaption = RequestBody.create(MediaType.parse("text/plain"), "ECG"+System.currentTimeMillis());

        Call<ResponseBody> call = restApi.uploadFile(patient, fileCaption, uploadfile);
        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v("Upload", "success");
              //  Toast.makeText(UploadService.class, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
               ToastUtil.notify("Uploaded Successfully");
                // ResponseBody resp = response.body();
               // System.out.println(resp);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                t.printStackTrace();
                ToastUtil.notify("Upload Failed");
            }

            });

    }
    }
