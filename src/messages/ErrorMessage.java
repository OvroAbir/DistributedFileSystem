package messages;

public class ErrorMessage extends MessageType 
{
	private String errorMessage;
	public ErrorMessage(String errorMessage) 
	{
		super(ERROR_MESSAGE);
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
