/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.controllers;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import SemanticQA.models.nlp.Parser;
import SemanticQA.models.nlp.Parser.ParserListener;
import SemanticQA.models.nlp.Tokenizer;
import SemanticQA.models.nlp.Tokenizer.TokenizerListener;
import SemanticQA.models.ontology.OntologyMapper;
import SemanticQA.models.ontology.OntologyQuery;


/**
 *
 * @author syamsul
 */
public class ThesisDesktopVersion implements TokenizerListener, ParserListener {
	
	private static OntologyMapper ontoMapper;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
//    	ontoMapper = new OntologyMapper();
    	OntologyQuery q = new OntologyQuery();
    	q.find();
    	
//        System.out.print("Masukkan pertanyaan: ");
//        Scanner scan = new Scanner(System.in);
        
//        String sentence = scan.nextLine();
//        scan.close();
        
//        new Tokenizer(ontoMapper).tokenize(sentence, new ThesisDesktopVersion());
    }
    
    public static void cetak(String answer){
        System.out.println(answer);
    }

	@Override
	public void onTokenizeSuccess(List<Map<String,String>> taggedToken) {
		new Parser(ontoMapper).parse(taggedToken, this);
	}

	@Override
	public void onParseSuccess(TreeMap<String, String> parseTree) {
		
	}
    
}
