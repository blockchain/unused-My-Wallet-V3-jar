package info.blockchain.wallet;

import info.blockchain.wallet.api.Environment;
import info.blockchain.wallet.api.PersistentUrls;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.internal.schedulers.TrampolineScheduler;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;


public abstract class BaseIntegTest {

    @BeforeClass
    public static void init() {

        //Initialize framework
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return getRetrofit(PersistentUrls.API_URL, getOkHttpClient());
            }

            @Override
            public Retrofit getRetrofitExplorerInstance() {
                return getRetrofit(PersistentUrls.EXPLORER_URL, getOkHttpClient());
            }

            @Override
            public Environment getEnvironment() {
                return Environment.PRODUCTION;
            }

            @Override
            public AbstractBitcoinNetParams getNetworkParameters() {
                return MainNetParams.get();
            }

            @Override
            public String getApiCode() {
                return null;
            }

            @Override
            public String getDevice() {
                return "Android Integration test";
            }

            @Override
            public String getAppVersion() {
                return "1.0";
            }
        });
    }

    @Before
    public void setupRxCalls() {
        RxJavaPlugins.reset();

        RxJavaPlugins.setInitIoSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(Callable<Scheduler> schedulerCallable) throws Exception {
                return TrampolineScheduler.instance();
            }
        });
        RxJavaPlugins.setInitComputationSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(Callable<Scheduler> schedulerCallable) throws Exception {
                return TrampolineScheduler.instance();
            }
        });
        RxJavaPlugins.setInitNewThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(Callable<Scheduler> schedulerCallable) throws Exception {
                return TrampolineScheduler.instance();
            }
        });
    }

    @After
    public void tearDownRxCalls() {
        RxJavaPlugins.reset();
    }

    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
            .addInterceptor(new ApiInterceptor())//Extensive logging
                .build();
    }

    private static Retrofit getRetrofit(String url, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}