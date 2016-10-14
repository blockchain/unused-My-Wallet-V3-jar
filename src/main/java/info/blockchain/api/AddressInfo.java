package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class AddressInfo implements BaseApi {

    private static final String ADDRESS = "address/";
    public static final String PROD_ADDRESS_INFO_URL = PROTOCOL + SERVER_ADDRESS + ADDRESS;

    private String addressInfoUrl = PROD_ADDRESS_INFO_URL;

    public AddressInfo() {
        addressInfoUrl = PersistentUrls.getInstance().getAddressInfoUrl();
    }

    public JSONObject getAddressInfo(String address, String parameter) throws Exception {

        StringBuilder url = new StringBuilder(addressInfoUrl);
        url.append(address);
        url.append("?format=json");
        if (parameter != null && !parameter.isEmpty())
            url.append(parameter);

        String response = WebUtil.getInstance().getURL(url.toString());
        return new JSONObject(response);
    }
}
