package com.syamsul.qa.controller;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.syamsul.qa.models.nlp.Tokenizer;


@Path("/qa")
public class Main {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("q") String question) throws JSONException{
		
		List<String> t = new Tokenizer().tokenize(question);
		JSONArray arr = new JSONArray();
		
		for(String a: t){
			arr.put(a);
		}
		
		JSONObject res = buildResult(question, arr);
		return Response.status(200).entity(res).build();
	}
	
	private JSONObject buildResult(String question, JSONArray answer) throws JSONException{
		JSONObject res = new JSONObject();
		
		res.put("status", 200);
		res.put("statusText", "OK");
		res.put("question", question);
		res.put("answer", answer);
		
		return res;
	}
	
}
