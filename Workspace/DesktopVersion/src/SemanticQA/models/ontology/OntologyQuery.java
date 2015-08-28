/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
		
		Map<String,Object> o = getOWLObject("bupati");
		OWLClass bupati = (OWLClass) o.get(Ontology.KEY_OBJECT_URI);
		
		reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		
		Set<OWLClassAxiom> aa = ontology.getAxioms(bupati);
		
		for(OWLClassAxiom a: aa){
			System.out.println(a.getAxiomType().getName());
			if(a.getAxiomType().getName() == "EquivalentClasses"){
				System.out.println("taraaa");
			}
		}
		
	}
	
}
