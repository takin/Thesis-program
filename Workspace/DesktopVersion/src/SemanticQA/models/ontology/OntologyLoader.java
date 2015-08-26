/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import SemanticQA.helpers.Constant;

/**
 *
 * @author syamsul
 */
public abstract class OntologyLoader {
	
	protected OWLOntology ontology;
	protected OWLReasoner reasoner;
	
	public OntologyLoader() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.loadOntology(IRI.create("http://local.co/ontologi/mbuh.owl"));
			manager.loadOntology(IRI.create(Constant.ONTOGOV_URL));
			
			OWLOntologyMerger merger = new OWLOntologyMerger(manager);
			ontology = merger.createMergedOntology(manager, IRI.create(Constant.ONTO_MERGED_URI));
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
}
