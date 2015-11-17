package SemanticQA.model;

public class Token {
	
	private String token;
	private String tokenType;
	
	public void setToken(String token){
		this.token = token;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public void setType(String tt){
		this.tokenType = tt;
	}
	
	public String getType(){
		return this.tokenType;
	}
	
	public void clear(){
		this.token = null;
		this.tokenType = null;
	}
	
	public boolean isEmpty(){
		return token == null && tokenType == null;
	}
}
