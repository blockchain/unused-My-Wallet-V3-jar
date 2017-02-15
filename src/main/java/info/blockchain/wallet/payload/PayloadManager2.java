package info.blockchain.wallet.payload;

import info.blockchain.wallet.payload.data2.WalletBaseBody;

public class PayloadManager2 {

    private WalletBaseBody walletBaseBody;

    private String tempPassword;

    private static PayloadManager2 instance = new PayloadManager2();

    public static PayloadManager2 getInstance() {
        return instance;
    }

    private PayloadManager2() {
        //no-op
    }

    public void setTempPassword(String password) {
        this.tempPassword = password;
    }
}