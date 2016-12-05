package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class AddressInfo extends BaseApi {

    private static final String ADDRESS = "address/";

    public AddressInfo() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + ADDRESS;
    }

    public JSONObject getAddressInfo(String address, String parameter) throws Exception {

        StringBuilder url = new StringBuilder(getRoute());
        url.append(address);
        url.append("?format=json");
        if (parameter != null && !parameter.isEmpty())
            url.append(parameter);

        String response = WebUtil.getInstance().getURL(url.toString());
        return new JSONObject(response);
    }
}
