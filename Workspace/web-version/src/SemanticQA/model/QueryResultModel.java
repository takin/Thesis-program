package SemanticQA.model;

import java.util.HashMap;
import java.util.Map;

public class QueryResultModel {

	private String about;
	private Map<String, String> data;
	
	public void setSubject(String about) {
		this.about = about;
	}
	
	public void addData(Map<String, String> data) {
		this.data = data;
	}
	
	public void putData(String key, String value) {
		if (this.data == null ) {
			this.data = new HashMap<String, String>();
		}
		
		this.data.put(key, value);
	}
	
	public String getSubject() {
		return this.about;
	}
	
	public Map<String, String> getData() {
		return this.data;
	}
	
	public String getData(String key) {
		return this.data.get(key);
	}
}
