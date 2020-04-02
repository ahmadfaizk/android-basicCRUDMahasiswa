package com.faiz.crudmahasiswa;

import androidx.annotation.NonNull;

public class Mahasiswa {
    private int id;
    private String nama;
    private String alamat;

    public Mahasiswa(int id, String nama, String alamat) {
        this.id = id;
        this.nama = nama;
        this.alamat = alamat;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getAlamat() {
        return alamat;
    }

    @NonNull
    @Override
    public String toString() {
        return nama;
    }
}
