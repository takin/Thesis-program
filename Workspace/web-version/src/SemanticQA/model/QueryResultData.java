package SemanticQA.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class QueryResultData extends QueryResultModel {

	private String about;
	private LinkedHashMap<String, String> data;
	
	public void setSubject(String about) {
		this.about = about;
	}
	
	public void addData(LinkedHashMap<String, String> data) {
		this.data = data;
	}
	
	public void putData(String key, String value) {
		if (this.data == null ) {
			this.data = new LinkedHashMap<String, String>();
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
