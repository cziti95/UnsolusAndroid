package rft.unideb.unsolus.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rft.unideb.unsolus.entities.AccessToken;
import rft.unideb.unsolus.entities.User;

public interface ApiService {

    @POST("api/register")
    @FormUrlEncoded
    Call<AccessToken> register(@Field("Username") String name, @Field("Email") String email, @Field("Password") String password);

    @POST("api/signin")
    @FormUrlEncoded
    Call<AccessToken> signin(@Field("Email") String email, @Field("Password") String password);

    @GET("api/user?token=")
    Call<User> getUser(@Query("token") String token);

    @GET("api/users?token=")
    Call<User> getUsers(@Query("token") String token);

}
