package com.example.navigationhiltroom.data.remote.di

import com.example.navigationhiltroom.BuildConfig
import com.example.navigationhiltroom.data.remote.api.RickMortyApiService
import com.example.primerxmlmvvm.data.remote.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

private const val RICK_MORTY = "rickMorty"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor,
                            ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            //.addInterceptor(AuthInterceptor(BuildConfig.API_KEY))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named(RICK_MORTY)
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)  // Ahora usa BuildConfig
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())

//            .converterFactories {
//                add(GsonConverterFactory.create())
//            }

            .build()
    }

    @Provides
    @Singleton
    @Named("jsonPlaceholder")
    fun provideRetrofitJsonPlaceHolder(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://jsonplaceholder.com/api")  // Ahora usa BuildConfig
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())

//            .converterFactories {
//                add(GsonConverterFactory.create())
//            }

            .build()
    }

    @Provides
    @Singleton
    fun provideRickMortyApiService(@Named(RICK_MORTY) retrofit: Retrofit): RickMortyApiService {
        return retrofit.create(RickMortyApiService::class.java)
    }
}
