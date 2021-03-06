package clases;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

public class Wallet {
	
	//Necesitamos que exista algo para borrar un wallet
	//Necesitamos una funcion para modificar username y password
	
	private String username;
	private String password;
	
	private double dollars = 500;
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	//Constructor
	public Wallet(String username, String password) {
		generateKeyPair();
		setUsername(username);
		setPassword(password);
	}
	
	//Getters/Setters username y password
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public double getDollars() {
		return dollars;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setDollars(double dollars) {
		this.dollars = dollars;
	}
	
	public void getAccountBalance() {
		System.out.println("\nUsername: " + getUsername());
		System.out.println("Pepecoins: " + getBalance());
		System.out.println("Dollars: " + getDollars());
	}

	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			//Inicializa el keyGenerator y su keyPair
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: BlockChain.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	
	public Transaction sendFunds(PublicKey recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("#No hay fondos suficientes. Transaccion declinada.");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}

}
