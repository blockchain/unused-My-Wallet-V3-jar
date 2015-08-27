package info.blockchain.wallet;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

import info.blockchain.wallet.util.PrivateKeyFactory;
import info.blockchain.wallet.util.CharSequenceX;

//import org.spongycastle.jce.provider;

public class Main {
	
	/*
	static {
	    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}
	*/

    public static void main(String[] args) {
		
		String wifc = "L2mtLC9eXFiHn8Jm786F3zsss6bjjriZKfk3nokuqhVQBpXJm5Rt";				// 1NsNdZhUJwavDhJosxbFi5Hy6DGndGyroS
		String wifu = "5K5HALHuucxaFkoAYJ1jc7mc61N1czUzcwznVWAx24jgh6ezckN";				// 1Esdw1pmQZhD6XXVsdHUUeab2ddWXeKT5F
		String hex = "A5BD2EDC1DFFA28F333ABCC07AEFE1A0CE1929F97DEF0DD4D008C4D82FBA3E70";	// 1Esdw1pmQZhD6XXVsdHUUeab2ddWXeKT5F
		String base64 = "pb0u3B3/oo8zOrzAeu/hoM4ZKfl97w3U0AjE2C+6PnA=";						// 1NsNdZhUJwavDhJosxbFi5Hy6DGndGyroS
		String mini = "S6c56bnXQiBjk9mqSYE7ykVQ7NzrRy";										// 1CciesT23BNionJeXrbxmjc7ywfiyM4oLW, 5JPy8Zg7z4P7RSLsiqcqyeAF1935zjNUdMxcDeVrtU1oarrgnB7
		String bip38 = "6PRVWUbkzzsbcVac2qwfssoUJAN1Xhrg6bNk8J7Nzm5H7kxEbn2Nh2ZoGg";		// 1Jq6MksXQVWzrznvZzxkV6oY57oWXD9TXB, 5KN7MzqK5wt2TP1fQCYyHBtDrXdJuXbUzm4A9rKAteGu3Qi5CVR
		
		ECKey ecKey = null;
		
		try	{
			ecKey = PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.getInstance().getFormat(wifc), wifc);
			System.out.println(wifc + ":" + ecKey.toAddress(MainNetParams.get()).toString());
			ecKey = PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.getInstance().getFormat(wifu), wifu);
			System.out.println(wifu + ":" + ecKey.toAddress(MainNetParams.get()).toString());
			ecKey = PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.getInstance().getFormat(base64), base64);
			System.out.println(base64 + ":" + ecKey.toAddress(MainNetParams.get()).toString());
			ecKey = PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.getInstance().getFormat(hex), hex);
			System.out.println(hex + ":" + ecKey.toAddress(MainNetParams.get()).toString());
			ecKey = PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.getInstance().getFormat(mini), mini);
			System.out.println(mini + ":" + ecKey.toAddress(MainNetParams.get()).toString());
		}
		catch(Exception e)	{
			System.out.println(e.getMessage());
		}

		try	{
			ecKey = PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.BIP38, bip38, new CharSequenceX("TestingOneTwoThree"));
			System.out.println(bip38 + ":" + ecKey.toAddress(MainNetParams.get()).toString());
		}
		catch(Exception e)	{
			System.out.println(e.getMessage());
		}

	}

}
