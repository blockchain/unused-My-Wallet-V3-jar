package info.blockchain.wallet.util;

public class Util	{

    private static Util instance = null;

    private Util() { ; }

    public static Util getInstance() {

        if(instance == null) {
            instance = new Util();
        }

        return instance;
    }

    public byte[] xor(byte[] a, byte[] b) {

        if(a.length != b.length)    {
            return null;
        }

        byte[] ret = new byte[a.length];

        for(int i = 0; i < a.length; i++)   {
            ret[i] = (byte)((int)b[i] ^ (int)a[i]);
        }

        return ret;
    }

}
