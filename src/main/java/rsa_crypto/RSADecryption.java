package rsa_crypto;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

public class RSADecryption {

	public static byte[] decryptSecretKey(String userName) throws Exception {
		PrivateKey privateKey=null;
		/*
		if(userName.equals("userA"))
		{
			//if receiver is userA then get private key of userA
			FileInputStream fi = new FileInputStream("src/main/resources/userA/privateA.key");
			byte[] privatekeyByte= new byte[(int) new File("src/main/resources/userA/privateA.key").length()];
			fi.read(privatekeyByte);
			fi.close();
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privatekeyByte);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(privateKeySpec);
		}*/
		//if receiver is userB then get private key of userB
		FileInputStream fi = new FileInputStream("src/main/resources/userB/privateB.key");
		byte[] privatekeyByte= new byte[(int) new File("src/main/resources/userB/privateB.key").length()];
		fi.read(privatekeyByte);
		fi.close();
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privatekeyByte);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		privateKey = keyFactory.generatePrivate(privateKeySpec);
		// decrypt the secret key in file AESSymmetricKey
		String aesFilePath="src/main/resources/userB/AESSymmetricKey.txt";
		byte [] decryptedSecretKey = decrypt(privateKey, aesFilePath);     
		//		System.out.println(new String(decryptedSecretKey)); 
		return decryptedSecretKey;
	}

	//receiver's side
	public static byte[] decrypt(PrivateKey privateKey, String aesFilePath) throws Exception {
		FileInputStream fin = new FileInputStream(aesFilePath);
		byte[] decryptedSymKey = new byte[fin.available()];
		fin.read(decryptedSymKey);
		fin.close();
		Cipher cipher = Cipher.getInstance("RSA");  
		cipher.init(Cipher.DECRYPT_MODE, (Key) privateKey);
		return cipher.doFinal(decryptedSymKey);
	}

}
