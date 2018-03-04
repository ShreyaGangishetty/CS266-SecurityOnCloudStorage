package rsa_crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import javax.crypto.Cipher;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAEncryption {

	public static byte[] encryptSecretKey(String encodedSecretKey,String userName) throws Exception {
		PublicKey publicKey=null;
		KeyPair keyPair;
		if(userName.equals("userA"))
		{
			if(new File("src/main/resources/userA/publicB.key").exists())
			{
				//getting public key
				FileInputStream fi = new FileInputStream("src/main/resources/userA/publicB.key");
				byte[] publickeyByte= new byte[(int) new File("src/main/resources/userA/publicB.key").length()];
				fi.read(publickeyByte);
				fi.close();
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publickeyByte);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				publicKey = keyFactory.generatePublic(keySpec);
			}		
			else
			{
				//System.out.println("building key pairs");
				keyPair = buildKeyPair(userName);
				publicKey = (PublicKey) keyPair.getPublic();
			}

		}
		
		/*if(userName.equals("userB"))
		{
			if(new File("src/main/resources/userB/publicA.key").exists())
			{
				//getting public key
				FileInputStream fi = new FileInputStream("src/main/resources/userB/publicA.key");
				byte[] publickeyByte= new byte[(int) new File("src/main/resources/userB/publicA.key").length()];
				fi.read(publickeyByte);
				fi.close();
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publickeyByte);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				publicKey = keyFactory.generatePublic(keySpec);
			}		
			else
			{
				keyPair = buildKeyPair(userName);
				publicKey = (PublicKey) keyPair.getPublic();
			}
		}*/
		// encrypt the message
		byte [] encryptedSecretKey = encrypt(publicKey, encodedSecretKey);     
		//System.out.println(new String(encryptedSecretKey)); 
		return encryptedSecretKey;
	}


	private static KeyPair buildKeyPair(String userName) throws NoSuchAlgorithmException, IOException {
		final int keySize = 2048;
		String publicKeyPath="";
		String privateKeyPath="";
		KeyPair keyPair=null;
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(keySize);
		keyPair=keyPairGenerator.genKeyPair();
		//write public key in file
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				keyPair.getPublic().getEncoded());
		//write private key in file
				PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
						keyPair.getPrivate().getEncoded());
		if(userName.equals("userA"))
		{
			publicKeyPath="src/main/resources/userA/publicB.key";
			privateKeyPath="src/main/resources/userB/privateB.key";
		}
		/*if(userName.equals("userB"))
		{
			publicKeyPath="src/main/resources/userB/publicA.key";
			privateKeyPath="src/main/resources/userA/privateA.key";
		}*/
		FileOutputStream fos1 = new FileOutputStream(publicKeyPath);
		fos1.write(x509EncodedKeySpec.getEncoded());
		fos1.close();		
		FileOutputStream fos2 = new FileOutputStream(privateKeyPath);
		fos2.write(pkcs8EncodedKeySpec.getEncoded());
		fos2.close();
		return keyPair;
	}

	private static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");  
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
		return cipher.doFinal(message.getBytes());  
	}

}
