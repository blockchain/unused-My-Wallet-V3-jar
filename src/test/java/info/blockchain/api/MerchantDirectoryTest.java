package info.blockchain.api;

import info.blockchain.test_data.MerchantDirectoryTestData;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MerchantDirectoryTest {

    @Test
    public void testParseMerchants() throws Exception {

        ArrayList<MerchantDirectory.Merchant> merchantList = new MerchantDirectory().parse(MerchantDirectoryTestData.testData);

        assertThat(merchantList.size(), is(3));

        assertThat(merchantList.get(0).id, is(MerchantDirectoryTestData.id_0));
        assertThat(merchantList.get(0).name, is(MerchantDirectoryTestData.name_0));
        assertThat(merchantList.get(0).address, is(MerchantDirectoryTestData.address_0));
        assertThat(merchantList.get(0).city, is(MerchantDirectoryTestData.city_0));
        assertThat(merchantList.get(0).description, is(MerchantDirectoryTestData.description_0));
        assertThat(merchantList.get(0).phone, is(MerchantDirectoryTestData.phone_0));
        assertThat(merchantList.get(0).postal_code, is(MerchantDirectoryTestData.postal_code_0));
        assertThat(merchantList.get(0).website, is(MerchantDirectoryTestData.website_0));
        assertThat(merchantList.get(0).category_id, is(MerchantDirectoryTestData.category_id_0));
        assertThat(merchantList.get(0).featured_merchant, is(MerchantDirectoryTestData.featured_merchant_0));
        assertThat(merchantList.get(0).latitude, is(MerchantDirectoryTestData.latitude_0));
        assertThat(merchantList.get(0).longitude, is(MerchantDirectoryTestData.longitude_0));

        assertThat(merchantList.get(1).name, is("Empty string test"));
        assertThat(merchantList.get(1).address, is(""));
        assertThat(merchantList.get(1).city, is(""));
        assertThat(merchantList.get(1).description, is(""));
        assertThat(merchantList.get(1).phone, is(""));
        assertThat(merchantList.get(1).postal_code, is(""));
        assertThat(merchantList.get(1).website, is(""));
        assertThat(merchantList.get(1).category_id, is(1));
        assertThat(merchantList.get(1).featured_merchant, is(false));
        assertThat(merchantList.get(1).latitude, is(0.0));
        assertThat(merchantList.get(1).longitude, is(0.0));

        assertThat(merchantList.get(2).name, is("Null Test"));
        assertThat(merchantList.get(2).address, is(""));
        assertThat(merchantList.get(2).city, is(""));
        assertThat(merchantList.get(2).description, is(""));
        assertThat(merchantList.get(2).phone, is(""));
        assertThat(merchantList.get(2).postal_code, is(""));
        assertThat(merchantList.get(2).website, is(""));
        assertThat(merchantList.get(2).category_id, is(1));
        assertThat(merchantList.get(2).featured_merchant, is(false));
        assertThat(merchantList.get(2).latitude, is(0.0));
        assertThat(merchantList.get(2).longitude, is(0.0));
    }
}