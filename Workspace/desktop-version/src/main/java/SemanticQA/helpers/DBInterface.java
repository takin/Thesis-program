package SemanticQA.helpers;

public interface DBInterface {
	
	public String query(String word);
	public void connect();
	public void close();
}
