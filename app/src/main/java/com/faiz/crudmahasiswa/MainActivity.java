package com.faiz.crudmahasiswa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;

    EditText editTextId, editTextNama, editTextAlamat;
    ProgressBar progressBar;
    ListView listView;
    Button buttonAddUpdate;

    List<Mahasiswa> mahasiswaList;
    boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextId = findViewById(R.id.etMahasiswaId);
        editTextNama = findViewById(R.id.etMahasiswaNama);
        editTextAlamat = findViewById(R.id.etMahasiswaAlamat);
        buttonAddUpdate = findViewById(R.id.buttonAddUpdate);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.lvMahasiswa);
        mahasiswaList = new ArrayList<>();

        buttonAddUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdating) {
                    updateMahasiswa();
                } else {
                    createMahasiswa();
                }
            }
        });
        readMahasiswa();
    }

    private void createMahasiswa() {
        String nama = editTextNama.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        if (TextUtils.isEmpty(nama)) {
            editTextNama.setError("Please Enter Nama");
            editTextNama.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(alamat)) {
            editTextAlamat.setError("Please enter alamat");
            editTextAlamat.requestFocus();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("nama", nama);
        params.put("alamat", alamat);

        PerformNetworkRequest request = new PerformNetworkRequest(ApiMahasiswa.URL_CREATE, params, CODE_POST_REQUEST);
        request.execute();

        editTextNama.setText("");
        editTextAlamat.setText("");
    }

    public class PerformNetworkRequest extends AsyncTask<Void, Void, String> {

        String url;
        HashMap<String, String> params;
        int requestCode;

        public PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);

            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_LONG).show();
                    refreshMahasiswaList(object.getJSONArray("mahasiswa"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);

            if (requestCode == CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);

            return null;
        }
    }

    public class MahasiswaAdapter extends ArrayAdapter<Mahasiswa> {
        List<Mahasiswa> mahasiswaList;

        public MahasiswaAdapter(List<Mahasiswa> mahasiswaList) {
            super(MainActivity.this, R.layout.layout_mahasiwa_list, mahasiswaList);
            this.mahasiswaList = mahasiswaList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View listViewItem = inflater.inflate(R.layout.layout_mahasiwa_list, null, true);
            TextView textViewNama = listViewItem.findViewById(R.id.textViewNama);
            TextView textViewUpdate = listViewItem.findViewById(R.id.textViewUpdate);
            TextView textViewDelete = listViewItem.findViewById(R.id.textViewDelete);
            final Mahasiswa mahasiswa = mahasiswaList.get(position);
            textViewNama.setText(mahasiswa.getNama());
            textViewUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isUpdating = true;
                    editTextId.setText(String.valueOf(mahasiswa.getId()));
                    editTextNama.setText(mahasiswa.getNama());
                    editTextAlamat.setText(mahasiswa.getAlamat());
                    buttonAddUpdate.setText("Update");
                }
            });
            textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Delete " + mahasiswa.getNama())
                            .setMessage("Are you sure want to delete it?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteMahasiswa(mahasiswa.getId());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
            return listViewItem;
        }
    }

    private void readMahasiswa() {
        PerformNetworkRequest request = new PerformNetworkRequest(ApiMahasiswa.URL_READ, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshMahasiswaList(JSONArray mahasiswa) throws JSONException {
        mahasiswaList.clear();
        for (int i = 0; i < mahasiswa.length(); i++) {
            JSONObject obj = mahasiswa.getJSONObject(i);
            mahasiswaList.add(new Mahasiswa(
                    obj.getInt("id"),
                    obj.getString("nama"),
                    obj.getString("alamat")
            ));
        }
        Log.d(MainActivity.class.getSimpleName(), mahasiswaList.toString());
        MahasiswaAdapter adapter = new MahasiswaAdapter(mahasiswaList);
        listView.setAdapter(adapter);
    }

    private void updateMahasiswa() {
        String id = editTextId.getText().toString().trim();
        String nama = editTextNama.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) {
            editTextNama.setError("Please Enter Nama");
            editTextNama.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(alamat)) {
            editTextAlamat.setError("Please enter alamat");
            editTextAlamat.requestFocus();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("nama", nama);
        params.put("alamat", alamat);

        PerformNetworkRequest request = new PerformNetworkRequest(ApiMahasiswa.URL_UPDATE, params, CODE_POST_REQUEST);
        request.execute();

        buttonAddUpdate.setText("Add");
        editTextNama.setText("");
        editTextAlamat.setText("");

        isUpdating = false;
    }

    private void deleteMahasiswa(int id) {
        PerformNetworkRequest request = new PerformNetworkRequest(ApiMahasiswa.URL_DELETE + id, null, CODE_GET_REQUEST);
        request.execute();
    }
}
