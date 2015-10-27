package SemanticQA.controllers;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import SemanticQA.helpers.QATokenModel;
import SemanticQA.models.nlp.Parser;
import SemanticQA.models.nlp.Tokenizer;
import sun.net.www.protocol.http.HttpURLConnection;


@Path("/qa")
public class Main {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("q") String question) throws JSONException{
		
		if ( question == null ) {
			throw new WebApplicationException(
					responseError(HttpURLConnection.HTTP_BAD_REQUEST, "Tidak ada pertanyaan untuk di proses")
			);
		} else if ( question.matches("/[^a-z0-9 ]+/i") ) {
			throw new WebApplicationException(
					responseError(HttpURLConnection.HTTP_NOT_ACCEPTABLE, "pertanyaan mengadung katakter yang tidak valid!")
			);
		} else {
			List<QATokenModel> t = new Tokenizer().tokenize(question);
			
			Map<String, Object> phrase = new Parser().parse(t);
			
			JSONObject res = buildResult(question, t);
			return Response.status(200).entity(res).build();
		}
	}
	
	private static Response responseError(int code, String messsage) {
		return Response.status(code).entity(messsage).build();
	}
	
	private JSONObject buildResult(String question, List<QATokenModel> model) throws JSONException{
		JSONObject response = new JSONObject();
		JSONObject result = new JSONObject();
		JSONArray nlpResults = new JSONArray();
		JSONArray answerResults = new JSONArray();
		
		answerResults.put("No Answer found yet!");
		
		for(QATokenModel m: model){
			nlpResults.put( m.getWord() + " -> " + m.getWordType() );
		}
		
		result.put("nlp", nlpResults);
		result.put("answer", answerResults);
		
		response.put("status", 200);
		response.put("statusText", "OK");
		response.put("question", question);
		response.put("result", result);
		
		return response;
	}
	
}
