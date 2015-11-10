/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;


import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import SemanticQA.constant.Ontology;

/**
 *
 * @author syamsul
 */
public class OntologyLoader {
	
	protected OWLOntology ontology;
	protected OWLReasoner reasoner;
	protected OWLOntologyManager manager;
	
	public OntologyLoader() {
		this.manager = OWLManager.createOWLOntologyManager();
		
		try {
			manager.loadOntologyFromOntologyDocument(IRI.create("file:///Users/syamsul/Documents/Thesis-program/Ontologi/ontogeo.owl"));
			manager.loadOntologyFromOntologyDocument(IRI.create("file:///Users/syamsul/Documents/Thesis-program/Ontologi/ontogov.owl"));
			manager.loadOntologyFromOntologyDocument(IRI.create("file:///Users/syamsul/Documents/Thesis-program/Ontologi/ontopar.owl"));
//			manager.loadOntologyFromOntologyDocument(IRI.create("file:///Users/syamsul/Documents/Thesis-program/Ontologi/dataset.owl"));
			
			OWLOntologyMerger merger = new OWLOntologyMerger(manager);
			ontology = merger.createMergedOntology(manager, IRI.create(Ontology.ONTO_MERGED_URI));
			
			ReasonerFactory rf = new Reasoner.ReasonerFactory();
			
			reasoner = rf.createReasoner(ontology, new SimpleConfiguration());
			reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
			reasoner.precomputeInferences(InferenceType.DATA_PROPERTY_ASSERTIONS);
			reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
			reasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES);
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public OWLOntology getOntology() {
		return this.ontology;
	}
	
	public OWLReasoner getReasoner() {
		return this.reasoner;
	}
}
