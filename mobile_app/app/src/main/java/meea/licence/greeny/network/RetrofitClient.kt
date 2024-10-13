package meea.licence.greeny.network

import android.content.Context
import meea.licence.greeny.MyApp
import meea.licence.greeny.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val BASE_URL = "https://meea.duckdns.org:8443"

    private var retrofit: Retrofit? = null
    private var token: String? = null

    fun getRetrofitClient(context: Context, newToken: String): Retrofit {
        if (retrofit == null || token != newToken) {
            token = newToken

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor(newToken))
                .sslSocketFactory(createSSLSocketFactory(context), getX509TrustManager(context))
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    private fun createSSLSocketFactory(context: Context): SSLSocketFactory {
        val keyStore = KeyStore.getInstance("PKCS12")

        // Load the keystore from the raw resources
        val inputStream: InputStream = context.resources.openRawResource(R.raw.keystore_greeny)
        keyStore.load(inputStream, "greeny".toCharArray())
        inputStream.close()

        // Create a KeyManagerFactory with the keystore
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, "greeny".toCharArray())

        // Create a TrustManagerFactory with the keystore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Create an SSLContext with the key and trust managers
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, SecureRandom())

        return sslContext.socketFactory
    }

    private fun getX509TrustManager(context: Context): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        return trustManagerFactory.trustManagers
            .filterIsInstance<X509TrustManager>()
            .first()
    }

    val authService: AuthService
        get() = getRetrofitClient(MyApp.getContext(), "").create(AuthService::class.java)

    val userService: UserService
        get() = getRetrofitClient(MyApp.getContext(), "").create(UserService::class.java)

    val componentService: ComponentService
        get() = getRetrofitClient(MyApp.getContext(), "").create(ComponentService::class.java)
}
