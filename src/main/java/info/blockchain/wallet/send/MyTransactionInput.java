package info.blockchain.wallet.send;

import java.math.BigInteger;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;

public class MyTransactionInput extends TransactionInput {

	private static final long serialVersionUID = 1L;
	
	public String address;
	public BigInteger value;
	public NetworkParameters params;
	
	public MyTransactionInput(NetworkParameters params, Transaction parentTransaction, byte[] scriptBytes, TransactionOutPoint outpoint) {
		super(params, parentTransaction, scriptBytes, outpoint);
		this.params = params;
	}
 
	@Override
	public Address getFromAddress() {

		try {
			return new Address(params, address);
		} catch (AddressFormatException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public Coin getValue() {
		return Coin.valueOf(value.longValue());
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}

}