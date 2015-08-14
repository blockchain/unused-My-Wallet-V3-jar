package info.blockchain.wallet.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.crypto.generators.SCrypt;
import org.spongycastle.util.encoders.Hex;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.codec.binary.Base64;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

public class PrivateKeyFactory	{

	public final static String BASE58 = "base58";
	public final static String BASE64 = "base64";
	public final static String BIP38 = "bip38";
	public final static String HEX_UNCOMPRESSED = "hex_u";
	public final static String HEX_COMPRESSED = "hex_c";
	public final static String MINI = "mini";
	public final static String WIF_COMPRESSED = "wif_c";
	public final static String WIF_UNCOMPRESSED = "wif_u";
	
    private static PrivateKeyFactory instance = null;

    private PrivateKeyFactory()	 { ; }

    public static PrivateKeyFactory getInstance()	 {

    	if(instance == null)	 {
    		instance = new PrivateKeyFactory();
    	}

    	return instance;
    }

	public String getFormat(String key) throws Exception {
		// 51 characters base58, always starts with a '5'
		if(key.matches("^5[1-9A-HJ-NP-Za-km-z]{50}$")) {
			return WIF_UNCOMPRESSED;
		}
		// 52 characters, always starts with 'K' or 'L'
		else if(key.matches("^[LK][1-9A-HJ-NP-Za-km-z]{51}$")) {
			return WIF_COMPRESSED;
		}
		else if(key.matches("^[1-9A-HJ-NP-Za-km-z]{44}$") || key.matches("^[1-9A-HJ-NP-Za-km-z]{43}$")) {
			return BASE58;
		}
		// assume uncompressed for hex (secret exponent)
		else if(key.matches("^[A-Fa-f0-9]{64}$")) {
			return HEX_UNCOMPRESSED;
		}
		else if(key.matches("^[A-Za-z0-9/=+]{44}$")) {
			return BASE64;
		}
		else if(key.matches("^6P[1-9A-HJ-NP-Za-km-z]{56}$")) {
			return BIP38;
		}
		else if(key.matches("^S[1-9A-HJ-NP-Za-km-z]{21}$") ||
				key.matches("^S[1-9A-HJ-NP-Za-km-z]{25}$") ||
				key.matches("^S[1-9A-HJ-NP-Za-km-z]{29}$") ||
				key.matches("^S[1-9A-HJ-NP-Za-km-z]{30}$")) {

			byte[] testBytes = null;
			String data = key + "?";
			try {
				Hash hash = new Hash(MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8")));
				testBytes = hash.getBytes();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

//			if(testBytes[0] == 0x00 || testBytes[0] == 0x01) {
			if(testBytes[0] == 0x00) {
				return MINI;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	public ECKey getKey(String format, String data) throws Exception { 
		return getKey(format, data, null); 
	}

	public ECKey getKey(String format, String data, CharSequenceX password) throws Exception { 
		if(format.equals(WIF_UNCOMPRESSED) || format.equals(WIF_COMPRESSED)) {
			DumpedPrivateKey pk = new DumpedPrivateKey(MainNetParams.get(), data);
			return pk.getKey();
		}
		else if(format.equals(BASE58)) {
			return decodeBase58PK(data);
		}
		else if(format.equals(BASE64)) {
			return decodeBase64PK(data);
		}
		else if(format.equals(HEX_UNCOMPRESSED)) {
			return decodeHexPK(data, false);
		}
        else if(format.equals(HEX_COMPRESSED)) {
            return decodeHexPK(data, true);
        }
		else if(format.equals(BIP38)) {
			return parseBIP38(data, password);
		}
		else if(format.equals(MINI)) {

			try {
				Hash hash = new Hash(MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8")));
				return decodeHexPK(hash.toString(), false);	// assume uncompressed
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}
		else {
			return null;
		}
	}

	private ECKey decodeBase58PK(String base58Priv) throws Exception {
		byte[] privBytes = Base58.decode(base58Priv);
		// Prepend a zero byte to make the biginteger unsigned
		byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
		ECKey ecKey = new ECKey(new BigInteger(appendZeroByte), null, true);
		return ecKey;
	}

	private ECKey decodeBase64PK(String base64Priv) throws Exception {
		byte[] privBytes = Base64.decodeBase64(base64Priv);
		// Prepend a zero byte to make the biginteger unsigned
		byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
		ECKey ecKey = new ECKey(new BigInteger(appendZeroByte), null, true);
		return ecKey;
	}

    private ECKey decodeHexPK(String hex, boolean compressed) throws Exception {
        byte[] privBytes = Hex.decode(hex);
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        ECKey ecKey = new ECKey(new BigInteger(appendZeroByte), null, compressed);
        return ecKey;
    }

	private String decryptPK(String base58Priv) throws Exception {

		/*
		if (this.isDoubleEncrypted()) {
			if (this.temporySecondPassword == null || !this.validateSecondPassword(temporySecondPassword))
				throw new Exception("You must provide a second password");

			base58Priv = decryptPK(base58Priv, getSharedKey(), this.temporySecondPassword, this.getDoubleEncryptionPbkdf2Iterations());
		}
		*/

		return base58Priv;
	}

	private ECKey decodePK(String base58Priv) throws Exception {
		return decodeBase58PK(decryptPK(base58Priv));
	}

	/*
	// Decrypt a double encrypted private key
	public static String decryptPK(String key, String sharedKey, String password, final int PBKDF2Iterations) throws Exception {
		return decrypt(key, sharedKey + password, PBKDF2Iterations);
	}

	// Encrypt a double encrypted private key
	public static String encryptPK(String key, String sharedKey, String password, final int PBKDF2Iterations) throws Exception {
		return encrypt(key, sharedKey + password, PBKDF2Iterations);
	}
	*/

	private ECKey parseBIP38(String input, CharSequenceX password) throws Exception	{
		byte[] store = Base58.decode(input);

		if(store.length != 43)	{
			throw new Exception ("invalid key length for BIP38");
		}
		boolean ec = false;
		boolean compressed = false;
		boolean hasLot = false;
		if((store[1] & 0xff) == 0x42)	{
			if((store[2] & 0xff) == 0xc0)	{
				// non-EC-multiplied keys without compression (prefix 6PR)
			}
			else if((store[2] & 0xff) == 0xe0)	{
				// non-EC-multiplied keys with compression (prefix 6PY)
				compressed = true;
			}
			else	{
				throw new Exception("invalid key");
			}
		}
		else if((store[1] & 0xff) == 0x43)	{
			// EC-multiplied keys without compression (prefix 6Pf)
			// EC-multiplied keys with compression (prefix 6Pn)
			ec = true;
			compressed = (store[2] & 0x20) != 0;
			hasLot = (store[2] & 0x04) != 0;
			if((store[2] & 0x24) != store[2])	{
				throw new Exception("invalid key");
			}
		}
		else	{
			throw new Exception("invalid key");
		}

		byte[] checksum = new byte[4];
		System.arraycopy(store, store.length - 4, checksum, 0, 4);
		byte[] ekey = new byte[store.length - 4];
		System.arraycopy(store, 0, ekey, 0, store.length - 4);
		byte[] hash = hash(ekey);
		for(int i = 0; i < 4; i++)	{
			if(hash[i] != checksum[i])	{
				throw new Exception("checksum mismatch");
			}
		}

		if(ec == false)	{
			return parseBIP38NoEC(store, password, compressed);
		}
		else	{
			return parseBIP38EC(store, password, compressed, hasLot);
		}
	}

	private ECKey parseBIP38NoEC(byte[] store, CharSequenceX passphrase, boolean compressed) throws Exception	{
		byte[] addressHash = new byte[4];
		System.arraycopy(store, 3, addressHash, 0, 4);
		byte[] derived = SCrypt.generate(passphrase.toString().getBytes("UTF-8"), addressHash, 16384, 8, 8, 64);
		byte[] key = new byte[32];
		System.arraycopy(derived, 32, key, 0, 32);
		SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
		cipher.init(Cipher.DECRYPT_MODE, keyspec);
		byte[] decrypted = cipher.doFinal(store, 7, 32);
		for(int i = 0; i < 32; i++)	{
			decrypted[i] ^= derived[i];
		}

		byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], decrypted);

//		ECKey kp = new ECKey (new BigInteger(appendZeroByte));
		ECKey kp = new ECKey(new BigInteger(appendZeroByte), null, compressed);

		String address = null;
		/*
		if(compressed) {
			address = kp.toAddressCompressed(MainNetParams.get()).toString();
		} else {
			address = kp.toAddressUnCompressed(MainNetParams.get()).toString();
		}
		*/
		address = kp.toAddress(MainNetParams.get()).toString();

		byte[] acs = hash(address.toString().getBytes ("US-ASCII"));
		byte[] check = new byte[4];
		System.arraycopy(acs, 0, check, 0, 4);
		if(!Arrays.equals(check, addressHash))	{
			throw new Exception ("failed to decrpyt");
		}

		return kp;
	}

	private ECKey parseBIP38EC (byte[] store, CharSequenceX passphrase, boolean compressed, boolean hasLot) throws Exception	{
		byte[] addressHash = new byte[4];
		System.arraycopy(store, 3, addressHash, 0, 4);

		byte[] ownentropy = new byte[8];
		System.arraycopy(store, 7, ownentropy, 0, 8);

		byte[] ownersalt = ownentropy;
		if(hasLot)	{
			ownersalt = new byte[4];
			System.arraycopy(ownentropy, 0, ownersalt, 0, 4);
		}

		byte[] passfactor = SCrypt.generate(passphrase.toString().getBytes ("UTF-8"), ownersalt, 16384, 8, 8, 32);
		if(hasLot)	{
			byte[] tmp = new byte[40];
			System.arraycopy(passfactor, 0, tmp, 0, 32);
			System.arraycopy(ownentropy, 0, tmp, 32, 8);
			passfactor = hash(tmp);
		}

		byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], passfactor);

//		ECKey kp = new ECKey(new BigInteger(appendZeroByte));
		ECKey kp = new ECKey(new BigInteger(appendZeroByte), null, true);

		byte[] salt = new byte[12];
		System.arraycopy(store, 3, salt, 0, 12);
//		byte[] derived = SCrypt.generate(kp.getPubKey(Compressed()), salt, 1024, 1, 1, 64);
		byte[] derived = SCrypt.generate(kp.getPubKey(), salt, 1024, 1, 1, 64);
		byte[] aeskey = new byte[32];
		System.arraycopy(derived, 32, aeskey, 0, 32);

		SecretKeySpec keyspec = new SecretKeySpec(aeskey, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
		cipher.init(Cipher.DECRYPT_MODE, keyspec);

		byte[] encrypted = new byte[16];
		System.arraycopy(store, 23, encrypted, 0, 16);
		byte[] decrypted2 = cipher.doFinal(encrypted);
		for(int i = 0; i < 16; i++)	{
			decrypted2[i] ^= derived[i + 16];
		}

		System.arraycopy(store, 15, encrypted, 0, 8);
		System.arraycopy(decrypted2, 0, encrypted, 8, 8);
		byte[] decrypted1 = cipher.doFinal(encrypted);
		for(int i = 0; i < 16; i++)	{
			decrypted1[i] ^= derived[i];
		}

		byte[] seed = new byte[24];
		System.arraycopy(decrypted1, 0, seed, 0, 16);
		System.arraycopy(decrypted2, 8, seed, 16, 8);
		BigInteger priv = new BigInteger(1, passfactor).multiply(new BigInteger(1, hash (seed))).remainder(SECNamedCurves.getByName("secp256k1").getN());

//		kp = new ECKey(priv);
		kp = new ECKey(priv, null, true);

		String address = null;
		/*
		if(compressed) {
			address = kp.toAddressCompressed(MainNetParams.get()).toString();
		} else {
			address = kp.toAddressUnCompressed(MainNetParams.get()).toString();
		}
		*/
		address = kp.toAddress(MainNetParams.get()).toString();

		byte[] acs = hash(address.getBytes ("US-ASCII"));
		byte[] check = new byte[4];
		System.arraycopy(acs, 0, check, 0, 4);
		if(!Arrays.equals(check, addressHash))	{
			throw new Exception ("failed to decrpyt");
		}

		return kp;
	}

	private byte[] hash(byte[] data, int offset, int len)	{
		try	{
			MessageDigest a = MessageDigest.getInstance("SHA-256");
			a.update(data, offset, len);
			return a.digest(a.digest());
		}
		catch(NoSuchAlgorithmException e)	{
			throw new RuntimeException(e);
		}
	}

	private byte[] hash(byte[] data)	{
		return hash(data, 0, data.length);
	}

}
