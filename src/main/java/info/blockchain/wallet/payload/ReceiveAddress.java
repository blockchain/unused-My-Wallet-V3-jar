package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceiveAddress {

    private String strAddress = null;
    private Long amount = null;
    private long paid = 0L;
    private String strLabel = null;
    private int index = 0;
    private int nbTx = 0;

    private boolean useThis = true;

    public ReceiveAddress() {
    }

    public ReceiveAddress(int index, long amount, long paid, String label, int nbTx) {
        this.index = index;
        this.amount = amount;
        this.paid = paid;
        this.strLabel = label;
        this.nbTx = nbTx;
    }

    public ReceiveAddress(long amount, long paid) {
        this.amount = amount;
        this.paid = paid;
    }

    public ReceiveAddress(String address, long amount, long paid) {
    	this.strAddress = address;
        this.amount = amount;
        this.paid = paid;
    }

    public ReceiveAddress(String address, int index) {
    	this.strAddress = address;
        this.index = index;
        this.amount = 0L;
        this.paid = 0L;
        this.strLabel = "";
        this.nbTx = 0;
    }

    public ReceiveAddress(String address, int index, String json) {
    	this.strAddress = address;
        this.index = index;
        this.amount = 0L;
        this.paid = 0L;
        this.strLabel = "";
        this.nbTx = 0;

        initJSON(json);

    }

    public String getAddress() {
        return strAddress;
    }

    public void setAddress(String address) { this.strAddress = address; }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isComplete() {
        return this.paid >= this.amount;
    }

    public long getPaid() {
        return paid;
    }

    public void setPaid(long paid) {
        this.paid = paid;
    }

    public String getLabel() {
        return strLabel;
    }

    public void setLabel(String label) { this.strLabel = label; }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNbTx() {
        return nbTx;
    }

    public void setNbTx(int nb) {
        this.nbTx = nb;
    }

    public void addAmount(long amount) {

        paid += amount;

    }
/*
    public void checkTX() {
        new AddressInfo().execute("");
    }
*/
    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put("index", index);
        obj.put("amount", amount == null ? null : amount);
        obj.put("paid", paid);
        obj.put("label", strLabel == null ? "" : strLabel);

        return obj;
    }

    public boolean notPreviouslyUsed() {
    	return useThis;
    }
/*
    private class AddressInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

        	String result = null;

        	try {
    			result = WebUtil.getInstance().getURL("https://blockchain.info/address/" + strAddress + "?format=json");
        	}
        	catch(Exception e) {
				e.printStackTrace();
        	}

        	return result;
        }

        @Override
        protected void onPostExecute(String result) {

        	if(result != null) {
    			try {
    	            JSONObject jsonObject = new JSONObject(result);

    	            if(jsonObject.has("n_tx")) {
    	            	int val = (Integer)jsonObject.get("n_tx");
    	            	nbTx = val;
    	            }
    	            else {
    	            	nbTx = 0;
    	            }

    	            if(jsonObject.has("final_balance")) {
    	            	long val = jsonObject.getLong("final_balance");
    	            	amount = val;
    	            }
    	            else {
    	            	amount = 0L;
    	            }

    			} catch(JSONException je) {
    				je.printStackTrace();
                	nbTx = 0;
                	amount = 0L;
    			}
        	}

//			Log.i("ReceiveAddress", "Index:" + index + "/" + strAddress + "/" + nbTx + "/" + amount);

			if(amount == 0L && nbTx == 0) {
				useThis = true;
			}
			else {
				useThis = false;
			}

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
*/
    private void initJSON(String json)	 {

    	if(json != null) {
			try {
	            JSONObject jsonObject = new JSONObject(json);

	            if(jsonObject.has("n_tx")) {
                    nbTx = (int) (Integer)jsonObject.get("n_tx");
	            }
	            else {
	            	nbTx = 0;
	            }

	            if(jsonObject.has("final_balance")) {
                    amount = jsonObject.getLong("final_balance");
	            }
	            else {
	            	amount = 0L;
	            }

			} catch(JSONException je) {
				je.printStackTrace();
            	nbTx = 0;
            	amount = 0L;
			}
    	}

        useThis = amount == 0L && nbTx == 0;

    }

}
