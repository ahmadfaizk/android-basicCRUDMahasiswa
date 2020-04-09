package com.faiz.crudmahasiswa.view.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.faiz.crudmahasiswa.R;
import com.faiz.crudmahasiswa.api.ApiClient;
import com.faiz.crudmahasiswa.api.Services;
import com.faiz.crudmahasiswa.model.Mahasiswa;
import com.faiz.crudmahasiswa.model.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class AddUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.img_mahasiswa) CircleImageView imgMahasiswa;
    @BindView(R.id.et_nama) EditText etNama;
    @BindView(R.id.et_alamat) EditText etAlamat;
    @BindView(R.id.btn_save) Button btnSave;

    public static final String EXTRA_MAHASISWA = "extra_mahasiswa";
    public static final int REQUEST_ADD = 100;
    public static final int RESULT_ADD = 101;
    public static final int REQUEST_UPDATE = 200;
    public static final int RESULT_UPDATE = 201;
    public static final int RESULT_DELETE = 301;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_MEDIA = 2;

    private Mahasiswa mahasiswa;
    private boolean isUpdate = false;
    private String currentPhotoPath;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading ...");

        String title = "Tambah";

        mahasiswa = getIntent().getParcelableExtra(EXTRA_MAHASISWA);
        if (mahasiswa != null) {
            isUpdate = true;
            title = "Ubah";
            btnSave.setText("Update");
            setData();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSave.setOnClickListener(this);
        imgMahasiswa.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                checkInput();
                break;
            case R.id.img_mahasiswa:
                showDialogSelectImage();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isUpdate) {
            getMenuInflater().inflate(R.menu.menu_form, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteMahasiswa();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setData() {
        Glide.with(this)
                .load(ApiClient.getImageUrl(mahasiswa.getFoto()))
                .placeholder(R.drawable.ic_user_512dp)
                .into(imgMahasiswa);
        etNama.setText(mahasiswa.getNama());
        etAlamat.setText(mahasiswa.getAlamat());
    }

    private void checkInput() {
        boolean ready = true;
        String nama = etNama.getText().toString();
        String alamat = etAlamat.getText().toString();

        if (nama.isEmpty()) {
            etNama.setError("Nama masih kosong");
            ready = false;
        }
        if (alamat.isEmpty()) {
            etAlamat.setError("Alamat masih kosong");
            ready = false;
        }

        if (currentPhotoPath == null && !isUpdate) {
            showMessage("Gambar belum dipilih");
            ready = false;
        }

        Mahasiswa mahasiswa = new Mahasiswa(nama, alamat);

        if (ready) {
            if (isUpdate) {
                updateMahasiswa(mahasiswa);
            } else {
                createMahasiswa(mahasiswa);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File file = new File(currentPhotoPath);
            Glide.with(this)
                    .load(file)
                    .placeholder(R.drawable.ic_user_512dp)
                    .into(imgMahasiswa);
        }
        if (requestCode == REQUEST_IMAGE_MEDIA && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            currentPhotoPath = getRealPathFromUri(uri);
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_user_512dp)
                    .into(imgMahasiswa);
        }
    }

    private void showDialogSelectImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Gambar dari")
                .setItems(R.array.arr_media, (dialog, which) -> {
                    if (which == 0) {
                        dispatchTakePictureIntent();
                    } else if (which == 1) {
                        getImageFromMedia();
                    }
                });
        builder.create().show();
    }

    private void createMahasiswa(Mahasiswa mahasiswa) {
        showLoading(true);
        File photo = new File(currentPhotoPath);
        RequestBody requestBody = RequestBody.create(photo, MediaType.parse("image/*"));
        MultipartBody.Part foto = MultipartBody.Part.createFormData("foto", photo.getName(), requestBody);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("nama", createPartFromString(mahasiswa.getNama()));
        map.put("alamat", createPartFromString(mahasiswa.getAlamat()));

        Retrofit retrofit = ApiClient.getClient();
        retrofit.create(Services.class).createMahasiswa(foto, map)
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        showLoading(false);
                        boolean error = response.body().isError();
                        if (!error) {
                            setResult(RESULT_ADD);
                            finish();
                        } else {
                            showMessage(response.body().getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        showLoading(false);
                        t.printStackTrace();
                        showMessage(t.getMessage());
                    }
                });
    }

    private void updateMahasiswa(Mahasiswa mahasiswa) {
        showLoading(true);
        MultipartBody.Part foto = null;
        if (currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/*"));
            foto = MultipartBody.Part.createFormData("foto", file.getName(), requestBody);
        }

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("id", createPartFromString(this.mahasiswa.getId()));
        map.put("nama", createPartFromString(mahasiswa.getNama()));
        map.put("alamat", createPartFromString(mahasiswa.getAlamat()));

        Retrofit retrofit = ApiClient.getClient();
        retrofit.create(Services.class).updateMahasiswa(foto, map)
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        showLoading(false);
                        boolean error = response.body().isError();
                        if (!error) {
                            setResult(RESULT_UPDATE);
                            finish();
                        } else {
                            showMessage(response.body().getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        showLoading(false);
                        t.printStackTrace();
                        showMessage(t.getMessage());
                    }
                });
    }

    private void deleteMahasiswa() {
        showLoading(true);
        Retrofit retrofit = ApiClient.getClient();
        retrofit.create(Services.class).deleteMahassiswa(mahasiswa.getId())
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        showLoading(false);
                        boolean error = response.body().isError();
                        if (!error) {
                            setResult(RESULT_DELETE);
                            finish();
                        } else {
                            showMessage(response.body().getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        showLoading(false);
                        t.printStackTrace();
                        showMessage(t.getMessage());
                    }
                });
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                showMessage(ex.getMessage());
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                        "com.faiz.crudmahasiswa.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void getImageFromMedia() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_MEDIA);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private String getRealPathFromUri(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getBaseContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(descriptionString, okhttp3.MultipartBody.FORM);
    }

    private void showLoading(Boolean state) {
        if (state)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
