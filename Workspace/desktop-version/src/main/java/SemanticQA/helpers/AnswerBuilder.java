package SemanticQA.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import SemanticQA.constant.Type;
import SemanticQA.model.QueryResultData;
import SemanticQA.model.QueryResultModel;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.sw.OntologyQuery.ResultKey;

public class AnswerBuilder {
	
	private static List<String> questionConstituents;
	private static String questionString;
	
	@SuppressWarnings("unchecked")
	public static JSONObject json(List<Sentence> question, Map<String, List<? extends QueryResultModel>> results) throws Exception {
		
		JSONObject jsonObject = new JSONObject();
		JSONArray inferedFacts = new JSONArray();
		
		StringBuffer summryText = getSubject(question);
		
		List<QueryResultModel> queryResultObject = (List<QueryResultModel>) results.get(ResultKey.OBJECT);
		List<QueryResultData> queryResultData = (List<QueryResultData>) results.get(ResultKey.DATA);
		
		if ( questionString.matches("^di.*") ) {
			summryText.append("adalah di");
		} else {
			summryText.append("adalah");
		}
		
		for ( QueryResultData resultData:queryResultData ) {
			JSONObject item = new JSONObject();
			JSONObject itemData = new JSONObject();
			
			String subject = shorten(resultData.getSubject());
			subject = normalize(subject);
			
			Map<String, String> props = resultData.getData();
			
			for( String key : props.keySet() ) {
				String value = props.get(key);
				String shortenedKey = shorten(key);
				//////////////////////////////////////////////////////////////////////
				// jika key adalah http://id.dbpedia.org/property/web atau 			//
				// value dari item berekstensi jpeg/jpg/gif, jangan di shorten		//
				// karena alamat aslinya dibutuhkan									//
				//////////////////////////////////////////////////////////////////////
				String shortnedValue  = ( shortenedKey.matches("(web|depiction)") || value.matches("(jpe?g|gif|png)$") ) ?
					value : normalize(shorten(value));
				
				if ( !shortenedKey.matches("(type)") ) {
					try {
					itemData.put(shortenedKey, shortnedValue);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			item.put("about", subject);
			item.put("data", itemData);
			inferedFacts.put(item);
		}
		
		for ( int i = 0 ; i < queryResultObject.size(); i++ ) {
			QueryResultModel resultObject = queryResultObject.get(i);
			
			String shortendAboutURI = shorten(resultObject.getObject());
			shortendAboutURI = normalize(shortendAboutURI);
			
			summryText.append(" " + shortendAboutURI);
		}
		
		try {
			jsonObject.put("text", summryText.toString());
			jsonObject.put("inferedFacts", inferedFacts);
		} catch (JSONException e) {
			throw new Exception("Proses pembentukan objek jawaban gagal!");
		}
		
		return jsonObject;
	}
	/*
	@SuppressWarnings("unchecked")
	public static JSONObject json(List<Sentence> question, Map<String, Object> result) {
		
		JSONObject res = new JSONObject();
		JSONArray inferedFacts = new JSONArray();
		StringBuffer summryText = getSubject(question);
		
		boolean classNotBeenAdded = true;
		boolean subjectNotBeenAdded = true;
		boolean objectNotBeenAdded = true;
		List<String> orderOfBoundedVars = new ArrayList<String>();
		
		QueryResult query = (QueryResult) result.get(ResultKey.SPARQLDL);
		List<QueryResultData> inferedObjects = (List<QueryResultData>) result.get(ResultKey.INFERED_DATA);
		
		if ( query.size() > 0 ) {
			if ( questionString.matches("^di.*") ) {
				summryText.append("adalah di");
			} else {
				summryText.append("adalah");
			}
		}
		
		for ( int queryBindingIndex = 0; queryBindingIndex < query.size(); queryBindingIndex++ ) {
			
			QueryBinding binding = query.get(queryBindingIndex);
			
			QueryArgument cls = new QueryArgument(QueryArgumentType.VAR, "class");
			QueryArgument sub = new QueryArgument(QueryArgumentType.VAR, "subject");
			QueryArgument obj = new QueryArgument(QueryArgumentType.VAR, "object");
			
			if ( binding.isBound(sub)) {
				String subject = binding.get(sub).toString();
				String s = shorten(subject);
				s = normalize(s);
				
				/////////
				// pertimbangkan implikasi kejamakan
				/////////
				if ( AnswerBuilder.questionConstituents.contains("saja") || subjectNotBeenAdded) {
					if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex < query.size() - 1 ) {
						summryText.append(", " + s);
					} else if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex == query.size() - 1 ) {
						summryText.append(" dan " + s);
					} else {
						summryText.append(" " + s);
					}
					orderOfBoundedVars.add(subject);
					subjectNotBeenAdded = false;
				}
			}
			
			if ( binding.isBound(cls) && classNotBeenAdded ) {
				String c = shorten(binding.get(cls).toString());
				c = normalize(c);
				summryText.append(" " + c);
				classNotBeenAdded = false;
			}
			
			if ( binding.isBound(obj) ){
				String object = binding.get(obj).toString();
				String i = shorten(object);
				i = normalize(i);
				//////
				// pertimbangkan implikasi kejamakan
				//////
				if ( AnswerBuilder.questionConstituents.contains("saja") || objectNotBeenAdded) {
					if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex < query.size() - 1 ) {
						summryText.append(", " + i);
					} else if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex == query.size() - 1 ) {
						summryText.append(" dan " + i);
					} else {
						summryText.append(" " + i);
					}
					orderOfBoundedVars.add(object);
//					objectNotBeenAdded = false;
				}
			}
		}
		
		
		for ( QueryResultData resultModel : inferedObjects ) {
			
			JSONObject item = new JSONObject();
			JSONObject itemData = new JSONObject();
			
			Map<String,String> props = resultModel.getData();
			
			for( String key : props.keySet() ) {
				String value = props.get(key);
				String shortenedKey = shorten(key);
				//////////////////////////////////////////////////////////////////////
				// jika key adalah http://id.dbpedia.org/property/web atau 			//
				// value dari item berekstensi jpeg/jpg/gif, jangan di shorten		//
				// karena alamat aslinya dibutuhkan									//
				//////////////////////////////////////////////////////////////////////
				String shortnedValue  = ( shortenedKey.matches("(web|depiction)") || value.matches("(jpe?g|gif|png)$") ) ?
					value : normalize(shorten(value));
				
				if ( !shortenedKey.matches("(type)") ) {
					try {
					itemData.put(shortenedKey, shortnedValue);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			if ( itemData.length() > 0 ) {
				try {
					String topic = shorten(resultModel.getSubject());
					topic = normalize(topic);
					item.put("about", topic);
					item.put("data", itemData);
					
					////////
					// pertimbangkan order list sesuai dengan urutan jawaban 
					////
					int pos = orderOfBoundedVars.indexOf(resultModel.getSubject());
					if ( pos != -1 ) {
						inferedFacts.put(pos, item);
					} else {
						inferedFacts.put(item);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		try {
			
			////////////////
			// ada kemungkinan summry text memiliki kata dobel akibat dari nama kelas dan 
			// individual yang mirip misalnya kelas Kabupaten dengan individual Kabupaten_Lombok_Timur
			// akan menghasilkan summry text "Kabupaten Kabupaten Lombok Timur". Untuk mencegah
			// masalah ini, maka lakukan pemeriksaan terhadap teks, jika dua kata yang berdekatan 
			// sama, maka buang salah satunya!
			///////////////
			
			String str = summryText.toString();
			StringBuffer cleanString = new StringBuffer();
			
			String[] arr_str = str.split(" ");
			for ( int i = 0; i < arr_str.length; i++ ) {
				String current = arr_str[i];
				String prev = i > 0 ? arr_str[i - 1] : null;
				if ( i == 0 || !current.equals(prev)) {
					if ( i == arr_str.length ) {
						cleanString.append(current);
					} else {
						cleanString.append(current + " ");
					}
				}
			}
			res.put("text", cleanString.toString());
			res.put("inferedFacts", inferedFacts);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	*/
	private static String shorten(String uri) {
		String sf = uri.replaceAll("^[a-z].*.(#|/)", "");
		return sf;
	}
	
	private static String normalize(String str) {
		
		String arrNormalized[] = str.split("_");
		String normalized[] = new String[arrNormalized.length];
		
		for (int i = 0; i < arrNormalized.length; i++) {
			String n = arrNormalized[i];
			normalized[i] = n.substring(0, 1).toUpperCase() + n.substring(1, n.length());
		}
		String concatinatedNormalized = String.join(" ", normalized);
		return concatinatedNormalized.replaceAll("(,)", "");
	}
	
	private static StringBuffer getSubject(List<Sentence> question) {
		
		StringBuffer returnedString = new StringBuffer();
		
		for ( Sentence s:question ) {
			
			if ( s.getType().equals(Type.Token.PRONOMINA) || s.getType().equals(Type.Phrase.PRONOMINAL) ) {
				List<SemanticToken> constituents = s.getConstituents();
				StringBuffer sb = new StringBuffer(constituents.size());
				for ( SemanticToken t:constituents ) {
					sb.append(t.getToken());
				}
				
				questionString = sb.toString();
			}
			
			if ( !s.getType().equals(Type.Token.PRONOMINA) && 
					!s.getType().equals(Type.Phrase.PRONOMINAL) ) {
				
				List<SemanticToken> constituents = s.getConstituents();
				
				for ( SemanticToken c : constituents ) {
					String token = c.getToken();
					if ( returnedString.length() == 0 || c.getType().equals(Type.Token.NOMINA) ) {
						token = normalize(token);
					}
					returnedString.append(token + " "); 
				}	
			}
			
			if ( s.getType().matches("(" + Type.Token.PRONOMINA + "|" + Type.Phrase.PRONOMINAL + ")") ) {
				List<SemanticToken> qConstituents = s.getConstituents();
				AnswerBuilder.questionConstituents = new ArrayList<String>(qConstituents.size());
				for ( int qsize = 0; qsize < qConstituents.size(); qsize++ ) {
					AnswerBuilder.questionConstituents.add(qConstituents.get(qsize).getToken());
				}
			}
			
		}
		
		return returnedString;
	}
}
