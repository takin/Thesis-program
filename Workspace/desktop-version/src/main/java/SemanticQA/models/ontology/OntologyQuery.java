/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.InferenceType;

import SemanticQA.constant.Ontology;

/**
 *
 * @author syamsul
 */
public class OntologyQuery extends OntologyMapper {
    
	public OntologyQuery() {
		super();
	}
    
	
	public void find(){
		
		Map<String,Object> o = getOWLObject("camat", Ontology.KEY_TYPE_CLASS);
		OWLClass bupati = (OWLClass) o.get(Ontology.KEY_OBJECT_URI);
		
		reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		System.out.println(reasoner.getInstances(bupati, false));
		
	}
	
}
