package main_pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;

import aes_crypto.AESDecryption;
import aes_crypto.AESEncryption;
import cloud.GoogleCloud;
import steg_pkg.StegAlg;

public class Client {
	public static Scanner sc = new Scanner(System.in);
	public static void main(String[] args) throws Exception {

		String menuInput = new String();
		
		while (!menuInput.contains("X")) {
			// print menu and pull scanner input
			printMenu();
			menuInput = sc.nextLine().toUpperCase();
			System.out.println("menu input entered: "+menuInput);
			if (menuInput.length() == 1 && !menuInput.contains("X")) {
				switch (menuInput) {
				case "1":
					userA();
					break;
				case "2":
					userB();
					break;
				default:
					System.out.println("Enter Valid Input!");
				}
			} else {
				if (!menuInput.contains("X")) {
					System.out.println("Enter a valid input");
				} else {
					System.out.println("Program closed");
					System.exit(1);
				}
			}
		}
	}


	public static void userA() throws Exception
	{
		String messagePath="src/main/resources/userA/PlainMessage.txt";
		//e1ncrypt and steg
		FileInputStream fileReader = new FileInputStream(new File(messagePath));
		int fileSize=fileReader.available();
		byte[] msg=new byte[fileSize];
		fileReader.read(msg);
		fileReader.close();
		String msg2=new String(msg);
		System.out.println("Plain Message read from File:\t\t"+msg2);
		//encrypt
		String cipherPath=AESEncryption.aesEncryption(msg2, "userA");
		//System.out.println("Message Encrypted and Written to Path:\t\t"+cipherPath);
		//steganography
		StegAlg stegAlg=new StegAlg();
		FileInputStream fis=new FileInputStream(new File(cipherPath));
		byte[] cipherMsg=new byte[(int)fis.available()];
		fis.read(cipherMsg);
		fis.close();		
		String orgImagePath="src/main/resources/userA/slytherin1.png";
		String stegImagePath="src/main/resources/userA/stegImg.png";
		
		stegAlg.hideMessage(cipherMsg, new File(orgImagePath), stegImagePath);
		//System.out.println("Message successfully encrypted and hidden in path:\t "+stegImagePath);
        //call upload to cloud method here	
		String status=GoogleCloud.uploadToCloud(stegImagePath);
		System.out.println("Steg Image status:"+status);
	}

	public static void userB() throws Exception
	{
		//call download from cloud method here
		GoogleCloud.downloadFiles();
		//Steg extract
		StegAlg stegAlg=new StegAlg();
		//System.out.println("going to un-hide: ");
		String stegImgPath="src/main/resources/userB/stegImg.png";
		byte[] gotMsg=stegAlg.showMessage(new File(stegImgPath));
		//System.out.println("got msg from steg: "+gotMsg);
		String interTextPath="src/main/resources/userB/IntermediateCipherText.txt";
		File f=new File(interTextPath);
		FileOutputStream fos=new FileOutputStream(f);
		fos.write(gotMsg);
		fos.close();
		AESDecryption.aesDecryption(interTextPath, "userB");
	}
	private static void printMenu() {

		System.out.println("----------------------------------------------");
		System.out.println("Welcome To CS266 Security on Cloud Storage Steganography project!");
		System.out.println("\n");
		System.out.println("Please Enter UserName 1 or 2: (For Exit Press X) ");
		//System.out.println("");
		System.out.println("\n");
	}
}
