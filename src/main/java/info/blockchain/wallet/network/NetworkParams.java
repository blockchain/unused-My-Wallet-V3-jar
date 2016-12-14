package info.blockchain.wallet.network;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;

public class NetworkParams {

    // Current Params
    private AbstractBitcoinNetParams currentParams;

    // Instance
    private static NetworkParams instance;

    private NetworkParams() {
        // Default to MainNet on first invocation
        setMainNetParams();
    }

    public static NetworkParams getInstance() {
        if (instance == null) {
            instance = new NetworkParams();
        }

        return instance;
    }

    /**
     * Resets network params to MainNet.
     */
    public void setMainNetParams() {
        currentParams = MainNetParams.get();
    }

    /**
     * Allows users to set the network Params of the application. You'll probably want to do this on
     * startup or follow up immediately afterwards with a reboot of the application.
     */
    public void setCurrentEnvironment(AbstractBitcoinNetParams currentParams) {
        this.currentParams = currentParams;
    }

    /**
     * Returns the current Params in use by the application.
     */
    public AbstractBitcoinNetParams getCurrentParams() {
        return currentParams;
    }
}