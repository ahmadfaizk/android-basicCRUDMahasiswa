package com.faiz.crudmahasiswa.view.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faiz.crudmahasiswa.R;
import com.faiz.crudmahasiswa.api.ApiClient;
import com.faiz.crudmahasiswa.api.Services;
import com.faiz.crudmahasiswa.model.Mahasiswa;
import com.faiz.crudmahasiswa.model.Response;
import com.faiz.crudmahasiswa.view.adapter.MahasiswaAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.rv_mahasiswa) RecyclerView rvMahasiswa;
    @BindView(R.id.fab_add) FloatingActionButton fabAdd;

    private ProgressDialog progressDialog;
    private MahasiswaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading ...");

        rvMahasiswa.setHasFixedSize(true);
        rvMahasiswa.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MahasiswaAdapter();
        rvMahasiswa.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rvMahasiswa.addItemDecoration(decoration);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddUpdateActivity.class);
            startActivityForResult(intent, AddUpdateActivity.REQUEST_ADD);
        });

        adapter.setOnCLickListener(mahasiswa -> {
            Intent intent = new Intent(MainActivity.this, AddUpdateActivity.class);
            intent.putExtra(AddUpdateActivity.EXTRA_MAHASISWA, mahasiswa);
            startActivityForResult(intent, AddUpdateActivity.REQUEST_UPDATE);
        });

        refreshData();
    }

    private void refreshData() {
        showLoading(true);
        Retrofit retrofit = ApiClient.getClient();
        retrofit.create(Services.class).getMahasiswa()
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        showLoading(false);
                        boolean error = response.body().isError();
                        if (!error) {
                            ArrayList<Mahasiswa> list = response.body().getMahasiswa();
                            if (list.isEmpty()) {
                                showMessage("Data Kosong");
                            } else {
                                adapter.setListMahasiswa(list);
                            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddUpdateActivity.REQUEST_ADD && resultCode == AddUpdateActivity.RESULT_ADD) {
            showMessage("Berhasil Menambahkan Mahasiswa");
            refreshData();
        } else if (requestCode == AddUpdateActivity.REQUEST_UPDATE) {
            if (resultCode == AddUpdateActivity.RESULT_UPDATE) {
                showMessage("Berhasil Mengubah Data Mahasiswa");
                refreshData();
            } else if (resultCode == AddUpdateActivity.RESULT_DELETE) {
                showMessage("Berhasil Mengapus Mahasiswa");
                refreshData();
            }
        }
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
