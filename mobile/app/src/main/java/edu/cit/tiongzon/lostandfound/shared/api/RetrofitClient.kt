package edu.cit.tiongzon.lostandfound.shared.api

import edu.cit.tiongzon.lostandfound.feature.admin.data.api.AdminApi
import edu.cit.tiongzon.lostandfound.feature.claims.data.api.ClaimApi
import edu.cit.tiongzon.lostandfound.feature.items.data.api.ItemApi
import edu.cit.tiongzon.lostandfound.feature.chat.data.api.MessageApi
import edu.cit.tiongzon.lostandfound.feature.payments.data.api.PaymentApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val adminApi: AdminApi = retrofit.create(AdminApi::class.java)
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val claimApi: ClaimApi = retrofit.create(ClaimApi::class.java)
    val itemApi: ItemApi = retrofit.create(ItemApi::class.java)
    val messageApi: MessageApi = retrofit.create(MessageApi::class.java)
    val paymentApi: PaymentApi = retrofit.create(PaymentApi::class.java)
}
