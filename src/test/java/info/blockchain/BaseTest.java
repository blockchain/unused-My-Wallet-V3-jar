package info.blockchain;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.FrameworkInterface;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.api.PersistentUrls.Environment;
import java.lang.reflect.Type;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.BeforeClass;
import org.junit.Ignore;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Ignore
public class BaseTest {

    public static MockInterceptor mockInterceptor;

    @BeforeClass
    public static void init() {

        //Set up okHttpClient with interceptor for mocking responses
        final OkHttpClient okHttpClient = getOkHttpClient();

        //Set Environment
        PersistentUrls.getInstance().setCurrentEnvironment(Environment.PRODUCTION);
        PersistentUrls.getInstance().setCurrentApiUrl("https://api.blockchain.info/");
        PersistentUrls.getInstance().setCurrentServerUrl("https://blockchain.info/");

        //Initialize framework
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return getRetrofit(PersistentUrls.getInstance().getCurrentBaseApiUrl(), okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return getRetrofit(PersistentUrls.getInstance().getCurrentBaseServerUrl(), okHttpClient);
            }

            @Override
            public String getApiCode() {
                return null;
            }
        });
    }

    private static OkHttpClient getOkHttpClient() {
        mockInterceptor = new MockInterceptor();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
            .addInterceptor(mockInterceptor)//Mock responses
//            .addInterceptor(loggingInterceptor)//Extensive logging
            .build();
    }

    private static Retrofit getRetrofit(String url, OkHttpClient client) {
        return new Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    }
}