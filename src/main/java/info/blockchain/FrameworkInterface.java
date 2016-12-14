package info.blockchain;

import retrofit2.Retrofit;

public interface FrameworkInterface {

    Retrofit getRetrofitApiInstance();

    Retrofit getRetrofitServerInstance();

}
