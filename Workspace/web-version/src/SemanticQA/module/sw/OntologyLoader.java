/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.module.sw;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

/**
 *
 * @author syamsul
 */
public class OntologyLoader {
	
	protected OWLOntology ontology;
	protected OWLOntologyManager manager;
	protected OWLDataFactory dataFactory; 
	
	public OntologyLoader(String path) throws Exception {
		this.manager = OWLManager.createOWLOntologyManager();
		
		try {
			IRI ontologyIRI = IRI.create(path);
			manager.loadOntology(ontologyIRI);
			ontology = manager.getOntology(ontologyIRI);
			dataFactory = manager.getOWLDataFactory();
			
		} catch (OWLOntologyCreationException e) {
			throw new Exception("Load Ontologi gagal!");
		}
	}
	
	public OntologyLoader(String[] paths, String mergedURI) throws Exception {
		this.manager = OWLManager.createOWLOntologyManager();
	
		try {
			for (String path: paths) {
				IRI ontologyIRI = IRI.create(path);
				manager.loadOntology(ontologyIRI);
			}
			
			OWLOntologyMerger merger = new OWLOntologyMerger(manager);
			ontology = merger.createMergedOntology(manager, IRI.create(mergedURI));
			dataFactory = manager.getOWLDataFactory();
			
		} catch (OWLOntologyCreationException e) {
			throw new Exception("Load Ontologi gagal!");
		}
	}
	
	public OWLOntology getOntology() {
		return this.ontology;
	}
	
	public OWLOntologyManager getOntologyManager() {
		return this.manager;
	}
}
