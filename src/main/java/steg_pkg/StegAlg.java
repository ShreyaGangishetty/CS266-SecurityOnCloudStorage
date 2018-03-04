package steg_pkg;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

@SuppressWarnings("unused")
public class StegAlg  {

	private String str1 = "",str2 = "",strr="";
	private String OK="";
	private byte[] hiddenBytes;
	private byte[] encryptedMessage;
	private byte[] encryptedMsg;
	private File encryptedFile;

	public byte[] getHash(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		return digest.digest(password.getBytes("UTF-8"));
	}

	public byte[] encryptMessage(String msgs,String key) {
		try{
			System.out.println(key);
			byte[] encodedkey = getHash(key);
			for(int i = 0; i < encodedkey.length; i++){
				System.out.print(encodedkey[i]);
			}
			System.out.println();

			//convert the byte to hex format
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < encodedkey.length; i++) {
				sb.append(Integer.toString((encodedkey[i] & 0xff) + 0x100, 16).substring(1));
			}
			//System.out.println("Hex format : " + sb.toString());


			SecretKeySpec k = new SecretKeySpec(encodedkey, "AES");
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, k);
			byte[] dataToSend = msgs.getBytes();
			//System.out.println("Length of message : "+dataToSend.length);
			byte[] encryptedData = c.doFinal(dataToSend);
			//System.out.println("hello5");

			str2 = bytesToString(encryptedData);

			//System.out.println("Message is:\n" + msgs + "\n"); 
			//System.out.println("Encoded message is:\n" + str2 + "\n");


			return encryptedData;

		} 

		catch (Exception e) { 
			throw new RuntimeException("Encryption failed!", e); 
		} 

	}     	      


	public static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			sb.append((char) (bytes[i] + 128));
		}
		return sb.toString();
	}

	public static byte[] stringToBytes(String str) {
		int length = str.length();
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			b[i] = (byte) (str.charAt(i) - 128);
		}
		return b;
	}

	// There are two decryption methods written below. In first method byte array is passed as
	// argument and in second method string is passed as argument. Now the point to be Noted is 
	// string passed in second method is generated from the conversion of byte array to string using
	// method written above namely "byteToString()". Similarly we have to convert this string to 
	// byteArray before using it for decryption in second method. Well this whole step is done only to
	// show the encrypted string, so the second method is only used if we want to do something with
	// the encrypted string or else first method is used... their no conversion is done.

	public String decryptMessage(byte[] encodedData, String key) throws BadPaddingException{ 
		try{ 


			byte[] encodedKey = getHash(key);
			//System.out.println("Length of EncodedKey : "+encodedKey.length);
			//System.out.println("Length of EncodedData : "+encodedData.length);

			SecretKeySpec k = new SecretKeySpec(encodedKey, "AES");
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, k);           
			byte[] originalData = c.doFinal(encodedData);

			strr = new String(originalData); 
			//System.out.println(strr); 
			return strr; 
		} 

		catch (BadPaddingException e){
			Component veiw = null;
			JOptionPane.showMessageDialog(veiw,
					"Entered Password is incorrect. Please Try Again.", "Error!",
					JOptionPane.ERROR_MESSAGE);
			throw new BadPaddingException();
		}
		catch (Exception e) { 
			throw new RuntimeException("Decryption failed!", e); 
		} 

	} 


	public String decryptMessage(String str, String key1) {
		try{
			String key = key1;//... secret sequence of bytes

			byte[] encodedKey = getHash(key); 

			byte[] encryptedData = stringToBytes(str);

			//byte[] encodedData = msgs.getBytes();
			Cipher c = Cipher.getInstance("AES");
			SecretKeySpec k = new SecretKeySpec(encodedKey, "AES");
			c.init(Cipher.DECRYPT_MODE, k); 
			byte[] decryptedData = c.doFinal(encryptedData);

			//System.out.println("Decrypted message:\n" + new String(decryptedData) + "\n");

			return str;
		}

		catch (Exception e) {
			throw new RuntimeException("Decryption failed!", e);
		}
	}


	public File hideMessage(byte[] msg, File imgFile, String destinationPath) {
		BufferedImage im = null;
		//int r=filechooser4.showSaveDialog(null);
		File encryptedFile =  new File(destinationPath);

		//File encryptedFile = new File("C:\\Documents and Settings\\abhi\\Desktop\\abhi\\hidden.png");
		try {
			im = ImageIO.read(imgFile);
			WritableRaster raster = im.getRaster();
			DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
			byte[] writableBytes = buffer.getData();
			//System.out.println("msgBytes : "+msg);
			int header = msg.length;
			byte[] lenBytes = intToBytes(header, 4);
			int totalLen = 4 + msg.length;
			byte[] bytesToHide = new byte[totalLen];
			System.arraycopy(lenBytes, 0, bytesToHide, 0, lenBytes.length);
			System.arraycopy(msg, 0, bytesToHide, lenBytes.length,
					msg.length);

			if (bytesToHide.length * 8 > writableBytes.length) {
				throw new RuntimeException("Image too small to hide message");
			}
			//System.out.println("Writing bytes:");
			int offset = 0;
			for (int i = 0; i < bytesToHide.length; i += 1) {
				byte b = bytesToHide[i];
				//System.out.print(b);
				//System.out.print(' ');
				for (int j = 0; j < 8; j += 1) {
					int bit = (b >> j) & 1;
					writableBytes[offset] = (byte) ((writableBytes[offset] & 0xFE) | bit);
					offset += 1;
				}
			}
			//System.out.println();
			//System.out.println("Message length: " + msg.length);
			ImageIO.write(im, "png", encryptedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encryptedFile;
	}

	public byte[] showMessage(File imgFile) {
		BufferedImage im = null;
		try {
			im = ImageIO.read(imgFile);
			WritableRaster raster = im.getRaster();
			DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
			byte[] data = buffer.getData();
			int len = 0;
			int offset = 0;
			//System.out.println("Message header is:");
			for (int i = 0; i < 4; i += 1) { // read length header (4 bytes)
				byte b = 0;
				for (int j = 0; j < 8; j += 1) {
					b |= (data[offset] & 1) << j;
					offset += 1;
				}
				//System.out.print(b);
				//System.out.print(' ');
				len |= b << (8 * i);
			}
			//System.out.println();
			//System.out.println("Decoding message:");
			hiddenBytes = new byte[len];
			for (int i = 0; i < len; i += 1) {
				byte b = 0;
				for (int j = 0; j < 8; j += 1) {
					b |= (data[offset] & 1) << j;
					offset += 1;
				}
				hiddenBytes[i] = b;
				//System.out.print(b);                
				//System.out.print(' ');
			}
			//System.out.println();
			//System.out.println("Encrypted data length is: " + len);
			//System.out.println("hiddenbytes"+hiddenBytes);

		} catch (Exception e) {
			Component veiw = null;
			JOptionPane.showMessageDialog(veiw,
					"Selected file has no hidden meassage", "Error!",
					JOptionPane.ERROR_MESSAGE);
			throw new NegativeArraySizeException("ERROR! Please Try Again");
		}
		return hiddenBytes;
	}


	private static byte[] intToBytes(int num, int numBytes) {
		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < numBytes; i += 1) {
			bytes[i] = (byte) ((num >> (8 * i)) & 0xFF);
		}
		return bytes;
	}

	private static int bytesToInt(byte[] bytes, int numBytes) {
		int num = 0;
		for (int i = 0; i < numBytes; i += 1) {
			num |= (bytes[i] & 0xFF) << (8 * i);
		}
		return num;
	}



}// end of Class


