package SemanticQA.controllers;

import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import SemanticQA.constant.Ontology;
import SemanticQA.module.sw.OntologyLoader;

public class Tester {

	
	public static void main(String args[]) {
		
		String[] paths = new String[]{
			Ontology.Path.ONTOGEO,
			Ontology.Path.ONTOGOV,
			Ontology.Path.ONTOPAR
		};
		
		validateOntologiesClasses(paths);
	}
	
	public static void validateOntologies(String[] paths) {
		try {
			OntologyLoader loader = new OntologyLoader(paths, Ontology.Path.MERGED_URI);
			Set<OWLOntology> ontologies = loader.getOntologyManager().getOntologies();
			for (OWLOntology o : ontologies) {
				OWLReasoner hermitReasoner = new Reasoner(o);
				hermitReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
				hermitReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
				Set<OWLClass> classes = hermitReasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
				
				System.out.println("|==============================================================================|");
				if ( o.getOntologyID().getOntologyIRI().toString() == Ontology.Path.MERGED_URI ) {
					System.out.println("|                    Pengujian ontologi hasil proses merging                   |");
				} else {
					System.out.println("| Pengujian ontologi " + o.getOntologyID().getVersionIRI() + "   |");
				}
				System.out.println("|==============================================================================|");
				System.out.println("| Jumlah Kelas            : " + o.getClassesInSignature().size() + "                                                 |");
				System.out.println("| Jumlah Object Property  : " + o.getObjectPropertiesInSignature().size() + "                                                 |");
				System.out.println("| Jumlah Datatype Property: " + o.getDataPropertiesInSignature().size() + "                                                 |");
				if ( classes.size() > 0 ) {
					System.out.println("| Inkonsisten kelas   :");
					for ( OWLClass c: classes ) {
							System.out.println("| " + c + "");
					}
				} else {
					System.out.println("| Inkonsisten kelas       : Tidak ada!                                         |");
				}
				
				System.out.println("|==============================================================================|");
				System.out.println("\n");
			}
		} catch (Exception e) {
			
		}
	}
	
	public static void validateOntologiesClasses(String[] paths){
		
		try {
			for (String path: paths){
				OntologyLoader loader = new OntologyLoader(path);
				
				Set<OWLOntology> ontology = loader.getOntologyManager().getOntologies();
				for (OWLOntology o : ontology) {
					OWLReasoner hermitReasoner = new Reasoner(o);
					hermitReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
					hermitReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
					Set<OWLClass> classes = hermitReasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
					
					System.out.println("|==============================================================================|");
					System.out.println("| Pengujian ontologi " + o.getOntologyID().getVersionIRI() + "   |");
					System.out.println("|==============================================================================|");
					System.out.println("| Jumlah Kelas            : " + o.getClassesInSignature().size() + "                                                 |");
					System.out.println("| Jumlah Object Property  : " + o.getObjectPropertiesInSignature().size() + "                                                 |");
					System.out.println("| Jumlah Datatype Property: " + o.getDataPropertiesInSignature().size() + "                                                 |");
					if ( classes.size() > 0 ) {
						System.out.println("| Inkonsisten kelas   :");
						for ( OWLClass c: classes ) {
								System.out.println("| " + c + "");
						}
					} else {
						System.out.println("| Inkonsisten kelas       : Tidak ada!                                         |");
					}
					
					System.out.println("|==============================================================================|");
					System.out.println("\n");
				}
			}
		} catch (Exception e) {
			
		}
	}
}
