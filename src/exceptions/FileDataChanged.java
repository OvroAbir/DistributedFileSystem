package exceptions;

public class FileDataChanged extends Exception {

	public FileDataChanged() 
	{
		super("File data has been changed. File integrity compromised.");
	}


}
