package com.faiz.crudmahasiswa.model;

import java.util.ArrayList;

public class Response {
    private boolean error;
    private String message;
    private ArrayList<Mahasiswa> mahasiswa;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Mahasiswa> getMahasiswa() {
        return mahasiswa;
    }

    public void setMahasiswa(ArrayList<Mahasiswa> mahasiswa) {
        this.mahasiswa = mahasiswa;
    }
}
