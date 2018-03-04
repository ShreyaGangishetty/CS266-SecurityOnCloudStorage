package aes_crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import cloud.GoogleCloud;
import rsa_crypto.RSAEncryption;

public class AESEncryption {

	public static String aesEncryption(String messageToEncrypt,String userName) throws Exception {
		// TODO Auto-generated method stub..
		SecretKey secretKey = getSecretEncryptionKey(userName);
		//encrypting plain text with secret key
		//System.out.println("aes key at encryption: "+secretKey);
		String cipherTextPath=	encryptText(messageToEncrypt.getBytes(), secretKey, userName);
		//System.out.println("Message sent from user is stored in path: "+ cipherTextPath);
		return cipherTextPath;
	}

	public static SecretKey getSecretEncryptionKey(String userName) throws Exception{	
		KeyGenerator generator = KeyGenerator.getInstance("AES");		
		generator.init(128); // The AES key size in number of bits
		SecretKey secretKey = generator.generateKey();
		//Encoding the secret key
		String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		//encrypting secret key using public key of receiver
		byte[] encryptedSecretKey=RSAEncryption.encryptSecretKey(encodedSecretKey,userName);
		//storing secret key in a file
		String aesKeyPath="src/main/resources/userA/AESSymmetricKey.txt";
		File skToReceiver=new File("src/main/resources/userA/AESSymmetricKey.txt");	
		FileOutputStream fos=new FileOutputStream(skToReceiver);
		fos.write(encryptedSecretKey);
		fos.close();
		String status=	GoogleCloud.uploadToCloud(aesKeyPath);
		System.out.println("Status of Encrypted Key:  "+status);
		//storing 
		return secretKey;		
	}
	public static String encryptText(byte[] plainText,SecretKey secretKey, String userName) throws Exception{
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] cipherText = aesCipher.doFinal(plainText);
		
		//if sender is user A then encrypted file should be in receiver's folder userB
		String path="src/main/resources/userA/CipherTextFile_tob.txt";
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(cipherText);
		fos.close();
		/*String status=	GoogleCloud.uploadToCloud(path);
		System.out.println("Status of Encrypted File:  "+status);*/
		return path;
	}
}