package TCP;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class UtilityMethods 
{
	public static int FILE_ONE_TRANSFER_BYTE_LEN = 8 * 1024; // 8KB
	
	public static void sendFile(String fileName, final Socket socket)
	{
		System.out.println("Sending file " + fileName + " to " + socket.getInetAddress());
		
		File file = new File(fileName);
		byte[] fileBytes = new byte[FILE_ONE_TRANSFER_BYTE_LEN];
		InputStream inputStream;
		OutputStream outputStream;
		
		try 
		{
			inputStream = new FileInputStream(file);
			outputStream = socket.getOutputStream();
			
			int bytesRead = 1;
			while(true)
			{
				bytesRead = inputStream.read(fileBytes);
				if(bytesRead <= 0)
					break;
				outputStream.write(fileBytes, 0, bytesRead);
			}
			
			inputStream.close();
			outputStream.close();
			
		} catch (IOException e) {
			System.out.println("Can not read file or Can not get output stream from Socket.");
			e.printStackTrace();
		}
	}
	
	public static void receieveFile(String fileName, final Socket socket)
	{
		byte[] fileBytes = new byte[FILE_ONE_TRANSFER_BYTE_LEN];
		InputStream inputStream;
		OutputStream outputStream;
		
		try
		{
			inputStream = socket.getInputStream();
			outputStream = new FileOutputStream(fileName);
			
			int bytesRead = 1;
			while(true)
			{
				bytesRead = inputStream.read(fileBytes);
				if(bytesRead <= 0)
					break;
				outputStream.write(fileBytes, 0, bytesRead);
			}
			
			inputStream.close();
			outputStream.close();
		}
		catch(IOException e)
		{
			System.out.println("Can not receieve file.");
			e.printStackTrace();
		}
	}
	
}
