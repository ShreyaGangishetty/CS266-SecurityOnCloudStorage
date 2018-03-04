package aes_crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import rsa_crypto.RSADecryption;

public class AESDecryption {
	public static String userName="";
	
	public static String aesDecryption(String filePath,String uName) throws Exception {
		
		//read cipher text from src/main/resources/receiver/CipherTextFile.txt
		userName=uName;
		FileInputStream fileReader = new FileInputStream(new File(filePath));
		int fileSize=fileReader.available();
		byte[] cipherText=new byte[fileSize];
		fileReader.read(cipherText);
		fileReader.close();
		//System.out.println("going to decrypt: "+new String(cipherText));
		SecretKey secretKey = getSecretKey(userName);
		//System.out.println("aes key at decrption: "+secretKey);
		//decrypting cipher text with secret key
		String decryptedPlainTextPath = decryptText(cipherText, secretKey);
		//steg of ciphertext in an image: call stego class's lsb method here		
		System.out.println("Decrypted Text is stored at: " + decryptedPlainTextPath);
		return decryptedPlainTextPath;
	}


	private static SecretKey getSecretKey(String userName) throws Exception{
		// get RSA decrypted key //generate AES secretkey
		byte[] decodedKey = Base64.getDecoder().decode(RSADecryption.decryptSecretKey(userName));
		// rebuild key using SecretKeySpec
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); 
		return secretKey;		
	}

	public static String decryptText(byte[] byteCipherText, SecretKey secKey) throws Exception {
		// AES defaults to AES/ECB/PKCS5Padding in Java 7
		String path="";
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.DECRYPT_MODE, secKey);
		//System.out.println("in aes decryption byteciphertext is: "+new String(byteCipherText));
		//byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
		//byte[] bytePlainText = aesCipher.update(byteCipherText);
		//byte[] bytePlainText=Base64.getDecoder().decode(aesCipher.doFinal(byteCipherText));
		//byte[] bytePlainText= aesCipher.doFinal(Base64.getDecoder().decode(byteCipherText));
		//System.out.println("got decoded plain text in aes decryption: "+new String(bytePlainText));

		path="src/main/resources/userB/DecryptedPlainText.txt";
		FileOutputStream fos=new FileOutputStream(path);
		byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
		fos.write(bytePlainText);
		fos.close();
		return path;
	}


}
