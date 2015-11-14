package SemanticQA.interfaces;

public interface DBConnector {
	
	public String query(String token);
	public void connect(String driver, String url, String username, String password);
	public void close();
	
}
