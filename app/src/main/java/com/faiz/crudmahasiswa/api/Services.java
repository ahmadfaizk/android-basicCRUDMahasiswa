package com.faiz.crudmahasiswa.api;

import com.faiz.crudmahasiswa.model.Response;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface Services {
    @GET("api/apiMahasiswa.php?apicall=get_mahasiswa")
    Call<Response> getMahasiswa();

    @Multipart
    @POST("api/apiMahasiswa.php?apicall=create_mahasiswa")
    Call<Response> createMahasiswa(@Part MultipartBody.Part image,
                                   @PartMap Map<String, RequestBody> params);

    @Multipart
    @POST("api/apiMahasiswa.php?apicall=update_mahasiswa")
    Call<Response> updateMahasiswa(@Part MultipartBody.Part image,
                                   @PartMap Map<String, RequestBody> params);

    @GET("api/apiMahasiswa.php?apicall=delete_mahasiswa")
    Call<Response> deleteMahassiswa(@Query("id") String id);
}
