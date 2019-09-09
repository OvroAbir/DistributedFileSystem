package messages;

public class ErrorMessage extends MessageType 
{
	private String errorMessage;
	public ErrorMessage(String errorMessage, String messageFrom) 
	{
		super(ERROR_MESSAGE, messageFrom);
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
