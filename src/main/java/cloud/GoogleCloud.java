package cloud;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleCloud {
	private static Drive service;
	/** Application name. */
	private static final String APPLICATION_NAME =
			"Drive API Java Quickstart";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), ".credentials/drive-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY =
			JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/** Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials
	 * at ~/.credentials/drive-java-quickstart
	 */
	private static final List<String> SCOPES =
			Arrays.asList(DriveScopes.DRIVE,DriveScopes.DRIVE,DriveScopes.DRIVE_READONLY);


	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = GoogleCloud.class.getResourceAsStream("../userA/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow =
				new GoogleAuthorizationCodeFlow.Builder(
						HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(DATA_STORE_FACTORY)
				.setAccessType("online").setApprovalPrompt("auto")
				.build();
		Credential credential = new AuthorizationCodeInstalledApp(
				flow, new LocalServerReceiver()).authorize("user");
		System.out.println(
				"Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		in.close();
		return credential;
	}

	/**
	 * Build and return an authorized Drive client service.
	 * @return an authorized Drive client service
	 * @throws IOException
	 */
	public static Drive getDriveService() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
	/**
	 * For upload a text file into drive
	 * */
	public static String uploadFile(String filePath, String title, String fileType) throws IOException{
		service = getDriveService();
		File fileMetadata = new File();
		fileMetadata.setName(title);
		java.io.File file1 = new java.io.File(filePath);
		FileContent mediaContent = new FileContent(fileType, file1);
		String fid=getFileDetails(title);
		System.out.println("fid value if the file exists: "+fid+" :");
		if(fid.length()==0)
		{
			File file = service.files().create(fileMetadata, mediaContent)
					.setFields("id")
					.execute();	
			System.out.println("inside create");
			return file.getId();
		}
		else
		{
			System.out.println("inside update");
			File file2=service.files().get(fid).execute();
			service.files().update(fid, fileMetadata, mediaContent).execute();
			return file2.getId();
		}
	}
	/**For downloading a text file from drive
	 * */
	public static void downloadFile(File file) {
		OutputStream outputStream= new ByteArrayOutputStream();
		java.io.File file1 = new java.io.File("src/main/resources/userB/"+file.getName());
		try {
			service.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
			ByteArrayOutputStream abc = (ByteArrayOutputStream) outputStream;
			//java.io.File file1 = new java.io.File(parentDir+file.getName());
			FileOutputStream fos = new FileOutputStream(file1);
			fos.write(abc.toByteArray());
			fos.close();				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**For printing the file details such as name, ID, permissions
	 * */
	public static String getFileDetails(String title) throws IOException
	{
		// Build a new authorized API client service.
		service = getDriveService();
		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list()
				.setPageSize(1000)
				.setFields("nextPageToken, files(id, name,permissions, mimeType)")
				.execute();
		List<File> files = result.getFiles();
		String fileId="";
		if (files == null || files.size() == 0) {
			System.out.println("No files found.");
		}
		else {
			for (File file : files) {
			if(file.getName().equals(title)) fileId=file.getId();
			}
		}
		return fileId;
	}
	public static String uploadToCloud(String filePath) 
	{
		String status="";
		String fileName="";
		String fileType="";
		String[] tempSplit= filePath.split("/");
		fileName=tempSplit[tempSplit.length-1];
		//System.out.println("FIlename in upload to cloud"+fileName);
		fileType=getMimeType(fileName);
		String fileId="";
		if(fileType !=null)
		{
			try {
				 fileId=uploadFile(filePath, fileName, fileType);
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IO EXCEPTION");
				e.printStackTrace();
			}
			if(fileId!=null || fileId.length()>0)
			status="Uploaded to Cloud successfully";
		}
		else
		{
			status="Couldn't upload as file type is not correct";
		}
		return status;
	}
	public static String getMimeType(String fileName)
	{
		HashMap<String, String> mimeMap = new HashMap<String, String>();
		mimeMap.put("txt", "text/plain");
		mimeMap.put("png", "image/png");
		mimeMap.put("jpeg", "image/jpeg");
		mimeMap.put("pdf", "application/pdf");
		String mimeType="";
		String[] mimeArray=fileName.split("\\.");
		for (int i = 0; i < mimeArray.length; i++) {
			System.out.println("inside array"+mimeArray[i]);
		}
		System.out.println("FILE NAME:"+fileName);
		if(mimeMap.get(mimeArray[mimeArray.length-1])!=null)
		{
			mimeType=mimeMap.get(mimeArray[1]);
		}
		return mimeType;
	}
	public static void downloadFiles()
	{
		try {
			service=getDriveService();
			FileList result = service.files().list()
		             .setPageSize(1000)
		       .setFields("nextPageToken, files(id, name,permissions)")
		             .execute();
		        List<File> files = result.getFiles();
		        if (files == null || files.size() == 0) {
		            System.out.println("No files found.");
		        }
		        else {
		            //System.out.println("Files in the drive are:");
		            for (File file : files) {
		            	System.out.println("**************DOWNLOADING***************");
		            	System.out.println(file.getName());
		            	downloadFile(file);
		            	System.out.println("*********************DONE**************");
		            	}
		            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}