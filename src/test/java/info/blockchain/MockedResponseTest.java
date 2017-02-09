package info.blockchain;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.FrameworkInterface;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.api.PersistentUrls.Environment;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.BeforeClass;
import org.junit.Ignore;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Ignore
public abstract class MockedResponseTest {

    public static MockInterceptor mockInterceptor = MockInterceptor.getInstance();

    @BeforeClass
    public static void init() {

        //Set Environment
        PersistentUrls.getInstance().setCurrentEnvironment(Environment.PRODUCTION);
        PersistentUrls.getInstance().setCurrentApiUrl("https://api.blockchain.info/");
        PersistentUrls.getInstance().setCurrentServerUrl("https://blockchain.info/");

        //Initialize framework
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return getRetrofit(PersistentUrls.getInstance().getCurrentBaseApiUrl(), getOkHttpClient());
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return getRetrofit(PersistentUrls.getInstance().getCurrentBaseServerUrl(), getOkHttpClient());
            }

            @Override
            public String getApiCode() {
                return null;
            }
        });
    }

    private static OkHttpClient getOkHttpClient() {
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