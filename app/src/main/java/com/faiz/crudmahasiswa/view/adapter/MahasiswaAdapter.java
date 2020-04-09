package com.faiz.crudmahasiswa.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.faiz.crudmahasiswa.R;
import com.faiz.crudmahasiswa.api.ApiClient;
import com.faiz.crudmahasiswa.model.Mahasiswa;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MahasiswaAdapter extends RecyclerView.Adapter<MahasiswaAdapter.ViewHolder> {

    private ArrayList<Mahasiswa> listMahasiswa;
    private OnCLickListener onCLickListener;

    public MahasiswaAdapter() {
        listMahasiswa = new ArrayList<>();
    }

    public void setListMahasiswa(ArrayList<Mahasiswa> listMahasiswa) {
        this.listMahasiswa = listMahasiswa;
        notifyDataSetChanged();
    }

    public void setOnCLickListener(OnCLickListener onCLickListener) {
        this.onCLickListener = onCLickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mahasiswa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mahasiswa mahasiswa = listMahasiswa.get(position);
        holder.tvNama.setText(mahasiswa.getNama());
        holder.tvAlamat.setText(mahasiswa.getAlamat());
        Glide.with(holder.itemView)
                .load(ApiClient.getImageUrl(mahasiswa.getFoto()))
                .placeholder(R.drawable.ic_user_512dp)
                .into(holder.imgMahasiswa);

        holder.itemView.setOnClickListener(v -> onCLickListener.onClick(mahasiswa));
    }

    @Override
    public int getItemCount() {
        return listMahasiswa.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_mahasiswa) CircleImageView imgMahasiswa;
        @BindView(R.id.tv_nama) TextView tvNama;
        @BindView(R.id.tv_alamat) TextView tvAlamat;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnCLickListener {
        void onClick(Mahasiswa mahasiswa);
    }
}
