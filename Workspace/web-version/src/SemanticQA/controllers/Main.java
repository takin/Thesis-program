package SemanticQA.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import SemanticQA.constant.Ontology;
import SemanticQA.helpers.AnswerBuilder;
import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.nlp.Parser;
import SemanticQA.module.nlp.Tokenizer;
import SemanticQA.module.sw.OntologyMapper;
import SemanticQA.module.sw.OntologyQuery;
import sun.net.www.protocol.http.HttpURLConnection;


@SuppressWarnings("restriction")
@Path("/qa")
public class Main {
	
	private static JSONObject query(String question) throws Exception {
		
		JSONObject finalResult = new JSONObject();
		String[] ontologies = new String[]{
				Ontology.Path.ONTOPAR, 
				Ontology.Path.ONTOGEO, 
				Ontology.Path.ONTOGOV,
				Ontology.Path.DATASET
		};
		
		Tokenizer tokenizer = new Tokenizer(new MySQLDatabase());
		Parser parser = new Parser();
		OntologyMapper ontologyMapper = new OntologyMapper(ontologies, Ontology.Path.MERGED_URI);
		
		OWLOntology ontology = ontologyMapper.getOntology();
		OWLReasoner reasoner = new Reasoner(ontology);
		OntologyQuery queryEngine = new OntologyQuery(ontologyMapper, reasoner);
		
		List<SemanticToken> tokenizerResult = tokenizer.tokenize(question);
		List<Sentence> parsingResult = parser.parse(tokenizerResult);
		List<Sentence> bufferPrseResult = clone(parsingResult);
		List<Sentence> mappingResult = ontologyMapper.map(parsingResult);
		
		Map<String, Object> queryResult = queryEngine.execute(mappingResult);
		finalResult = AnswerBuilder.json(bufferPrseResult, queryResult);
		
		return finalResult;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@HeaderParam("Access-Control-Request-Headers") String header, @QueryParam("q") String question) throws JSONException{
		
		Response responseObject = null;
		
		if ( question == null || question.matches("/[^a-z0-9 ]+/i") ) {
			responseObject = question == null ? 
					createResponse("Tidak ada pertanyaan untuk di proses!", header, HttpURLConnection.HTTP_NO_CONTENT) :
					createResponse("Kalimat tanya tidak valid!", header, HttpURLConnection.HTTP_NOT_ACCEPTABLE);
			
			return responseObject;
		}
		
		try {
			JSONObject finalResult = query(question);
			responseObject = createResponse(finalResult, header, HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			responseObject = createResponse(e.getMessage(), header, HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		
		return responseObject;
	}
	
	private static Response createResponse(JSONObject res, String requestHeader, int code) {
		
		JSONObject response = new JSONObject();
		try {
			response.put("code", code);
			
			///////////////
			// Jika proses error
			// maka isi field message dengan pesan error
			/////////////////
			if ( res.has("message") ) {
				response.put("message", res.get("message"));
			} else {
				response.put("message", "OK");
				response.put("answer", res);				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ResponseBuilder rb = Response.ok()
								.header("Access-Control-Allow-Origin", "*")
								.header("Access-Control-Allow-Methods", "GET");
		
		if(!"".equals(requestHeader)) {
			rb.header("Access-Control-Allow-Headers", requestHeader);
		}
		
		return rb.status(200).entity(response).build();
	}
	
	private static Response createResponse(String errorMessage, String requestHeader, int code) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("message", errorMessage);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return createResponse(obj, requestHeader, code);
	}
	
	private static List<Sentence> clone(List<Sentence> sentence){
		
		List<Sentence> clone = new ArrayList<Sentence>(sentence.size());
		
		for ( Sentence s:sentence ) {
			Sentence newSentence = new Sentence();
			List<SemanticToken> constituents = s.getConstituents();
			List<SemanticToken> newTokens = new ArrayList<SemanticToken>();
			for ( SemanticToken t: constituents ) {
				SemanticToken newToken = new SemanticToken();
				newToken.setToken(t.getToken());
				newToken.setType(t.getType());
				newTokens.add(newToken);
			}
			
			newSentence.setConstituent(newTokens);
			newSentence.setFunction(s.getFunction());
			newSentence.setType(s.getType());
			clone.add(newSentence);
		}
		return clone;
	}
	
}
