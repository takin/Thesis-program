package SemanticQA.interfaces;

public interface DBConnector {
	
	public String query(String token) throws Exception;
	public void connect(String driver, String url, String username, String password) throws Exception;
	public void close();
	
}
