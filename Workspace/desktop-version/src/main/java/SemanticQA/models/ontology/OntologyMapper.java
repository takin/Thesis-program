package SemanticQA.models.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import SemanticQA.constant.Ontology;
import SemanticQA.constant.Token;
import SemanticQA.helpers.StringManipulation;
import SemanticQA.models.nlp.QuestionModel;
import SemanticQA.models.nlp.TokenModel;


public class OntologyMapper extends OntologyLoader {
	
	private ShortFormProvider shortForm = new SimpleShortFormProvider();
	private List<QuestionModel> questionModel;
	
	public OntologyMapper(List<QuestionModel> model) {
		this.questionModel = model;
	}
	
	public List<QuestionModel> map(){
		
		for ( int i = 0; i < this.questionModel.size(); i++ ) {
			
			QuestionModel m = this.questionModel.get(i);
			
			if (m.getType().equals(Token.TYPE_PRONOMINA) ||
					m.getType().equals(Token.TYPE_FRASA_PRONOMINAL) ||
					m.getType().equals(Token.TYPE_KONJUNGSI) ) {
				continue;
			}
			
			List<TokenModel> originalConstituents = m.getConstituents();
			
			if ( originalConstituents.size() > 0 ) {
				List<TokenModel> constituents = checkType(null, new ArrayList<TokenModel>(), originalConstituents);					
				m.replaceConstituent(constituents);
			}
			
			this.questionModel.set(i, m);
		}
		
		
		return this.questionModel;
	}
	
	private List<TokenModel> checkType(String unmappedToken, List<TokenModel> res, List<TokenModel> data) {
		
		TokenModel m = data.remove(0);
		String token = m.getToken();
		String tipe = getType(token);
		
		/**
		 * Jika token tidak memiliki mapping di dalam ontologi 
		 * maka lakukan langkah:
		 * 1. cek apakah unmappedToken = null atau tidak
		 * 2. Jika null, maka lanjutkan dengan token berikutnya.
		 * 3. Jika tidak, maka gabungkan token saat ini dengan unmappedToken
		 *    kemudian lakukan pengecekan ulang.
		 * 4. Jika hasil penggabungkan ditemukan mappingnya, maka selesai.
		 */
		if ( tipe != null ) {
			
			// jika token berhasil di mapping dengan ontologi
			m.setTokenOWLType(tipe);
			res.add(m);
			
		} else {
			
			/**
			 * Jika unmappedToken tidak kosong dan token tidak berhasil di mapping
			 * maka gabungkan token tersebut dengan token sebelumnya.
			 */
			if ( unmappedToken != null ) {
				token = unmappedToken + "_" + token;
			}
			
			/**
			 * Setelah proses penggabungan token dengan token sebelumnya 
			 * yang sama-sama tidak memiliki mapping di dalam ontologi
			 * maka lakukan pengecekan ulang.
			 */
			tipe = getType(token);
			
			/**
			 * Jika hasil penggabungan berhasil di mapping
			 */
			if ( tipe != null ) {
				
				/**
				 * Masukkan tipe ontologi ke dalam objek TokenModel
				 */
				m.setTokenOWLType(tipe);
				/**
				 * ganti token dengan token yang telah digabungkan
				 */
				m.setToken(token);
				
				res.add(m);
				
				/**
				 * Reset unmappedToken menjadi null sehingga untuk token 
				 * yang selanjutnya akan dimulai dari proses awal dengan 
				 * tanpa ada unmappedToken
				 */
				unmappedToken = null;
			} else {
				/**
				 * Jika mapping untuk token yang bersangkutan tidak ada,
				 * maka set token menjadi nmappedToken. Token ini nantinya
				 * akan dikirimkan ke proses selanjutnya. 
				 */
				unmappedToken = token;
			}
		}
		
		
		if ( data.size() > 0 ) {
			checkType(unmappedToken, res, data);
		}
		
		return res;
	}
	
	public String getShortForm(OWLEntity e){
		return shortForm.getShortForm(e);
	}
	
	public OWLObject getOWLObject(String name, String type){
		
		switch(type){
		case Ontology.TYPE_CLASS:
			for(OWLClass obj: super.ontology.getClassesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_OBJECT_PROPERTY:
			for(OWLObjectProperty obj: super.ontology.getObjectPropertiesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_DATATYPE_PROPERTY:
			for(OWLDataProperty obj: super.ontology.getDataPropertiesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_INDIVIDUAL:
			for(OWLNamedIndividual obj: super.ontology.getIndividualsInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		}
		
		return null;
	}
	
	public String getType(String prop){
		if(isClass(prop)){
			return Ontology.TYPE_CLASS;
		}
		
		if(isDatatypeProperty(prop)){
			return Ontology.TYPE_DATATYPE_PROPERTY;
		}
		
		if(isObjectProperty(prop)){
			return Ontology.TYPE_OBJECT_PROPERTY;
		}
		
		if(isIndividual(prop)){
			return Ontology.TYPE_INDIVIDUAL;
		}
		
		return null;
	}
	
	public boolean isClass(String prop){
		
		if(isURI(prop)){
			for(OWLClass cls: super.ontology.getClassesInSignature()){
				if(cls.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_UNDERSCORE);
			for(OWLClass cls: super.ontology.getClassesInSignature()){
				if(shortForm.getShortForm(cls).equals(prop)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isDatatypeProperty(String prop){
		
		if(isURI(prop)){
			for(OWLDataProperty dp: super.ontology.getDataPropertiesInSignature()){
				if(dp.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_CAMELCASE);
			
			for(OWLDataProperty dp: super.ontology.getDataPropertiesInSignature()){
				if(shortForm.getShortForm(dp).equals(prop)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isObjectProperty(String prop){
		
		if(isURI(prop)){
			for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
				if(op.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_CAMELCASE);
			
			for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
				if(shortForm.getShortForm(op).equals(prop)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isIndividual(String prop){
		
		if(isURI(prop)){
			for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
				if(in.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_UNDERSCORE);
			
			for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
				if(shortForm.getShortForm(in).equals(prop)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Set<OWLClassAxiom> getRestriction(OWLClass cls){
		return ontology.getAxioms(cls);
	}
	
	public Set<OWLDataPropertyAxiom> getRestriction(OWLDataProperty dp){
		return ontology.getAxioms(dp);
	}
	
	public Set<OWLIndividualAxiom> getRestriction(OWLNamedIndividual individu){
		return ontology.getAxioms(individu);
	}
	
	public Set<OWLObjectPropertyAxiom> getRestriction(OWLObjectProperty op){
		return ontology.getAxioms(op);
	}
	
	private boolean isURI(String str){
		return str.matches("^(https?://).*");
	}
	
}
