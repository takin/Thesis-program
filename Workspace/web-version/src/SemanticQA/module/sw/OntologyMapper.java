package SemanticQA.module.sw;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
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

import SemanticQA.constant.Type;
import SemanticQA.helpers.StringManipulation;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;


public class OntologyMapper extends OntologyLoader {
	
	private ShortFormProvider shortForm;
	
	public OntologyMapper(String ontology) {
		super(ontology);
		shortForm = new SimpleShortFormProvider();
	}
	
	public OntologyMapper(String[] ontologies, String mergedURI) {
		super(ontologies, mergedURI);
		shortForm = new SimpleShortFormProvider();
	}
	
	public List<Sentence> map(List<Sentence> models){
		return doMapping(new ArrayList<SemanticToken>(), models, new ArrayList<Sentence>());
	}

	private List<Sentence> doMapping(List<SemanticToken> prevToken, List<Sentence> models, List<Sentence> result) {
		
		Sentence currentPhrase = models.remove(0);
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Lakukan proses mapping dengan ontologi hanya terhadap frasa selain frasa pronominal 	//
		// atau pronomina karena frasa ini tidak mungkin memiliki mapping di dalam ontologi 	//
		// sedangkan kehadirannya tetap dibutuhkan pada saat proses pembentukan query.			//
		// Jika dilakukan proses mapping maka konstituennya akan hilang!						//
		//////////////////////////////////////////////////////////////////////////////////////////
		if ( !currentPhrase.getConstituents().isEmpty() && 
				!currentPhrase.getType().equals(Type.Phrase.PRONOMINAL) && 
				!currentPhrase.getType().equals(Type.Token.PRONOMINA) ) {
			
			List<SemanticToken> constituents = checkType(new ArrayList<String>(), currentPhrase.getConstituents(), new ArrayList<SemanticToken>() );
			//////////////////////////////////////////////////////////////////////////////////////////
			// Ganti konstituen frasa yang bersangkutan dengan konstituen yang sudah di mapping!	//
			// 																						//
			// Note:																				//
			// Konstituen harus diganti (bukan ditambahkan), karena ada kemungkinan beberapa		//
			// konstituen akan berubah setelah mengalami proses mapping, misalanya lombok dan timur	//
			// akan berubah menjadi lombok_timur													//
			//////////////////////////////////////////////////////////////////////////////////////////
			currentPhrase.replaceConstituent(constituents);
		}
		
		result.add(currentPhrase);
		
		if ( models.size() > 0 ) {
			doMapping(currentPhrase.getConstituents(), models, result);
		}
		
		return result;
	}
	
	private List<SemanticToken> checkType(List<String> prevTokens, List<SemanticToken> tokensToProcess, List<SemanticToken> result) {
		
		SemanticToken currentToken = tokensToProcess.remove(0);
		
		String currentTokenWord = currentToken.getToken(); 
		String tokenOWLType = getType(currentTokenWord);
		
		if ( tokenOWLType != null ) {
			
			SemanticToken token = setToken(currentToken, currentTokenWord, tokenOWLType);
			result.add(token);
			
			// jika prevTokens tidak kosong, maka coba untuk melakukan konkatinasi
			if ( !prevTokens.isEmpty() ) {
				List<SemanticToken> concatinatedLists = doConcatination(prevTokens, currentToken);
				if ( concatinatedLists.size() > 0 ) {
					result.addAll(concatinatedLists);
				}
			}
		}
		
		if ( tokenOWLType == null && !prevTokens.isEmpty()) {
			
			List<SemanticToken> concatinatedLists = doConcatination(prevTokens, currentToken);
			if ( concatinatedLists.size() > 0 ) {
				result.addAll(concatinatedLists);
			}
		}
		
		//////////////////////////////////////////////////////////////////
		// Batasi jumlah stack history token hanya 5					//
		// sehingga proses konkatinasi hanya maksimal 5 suku kata		//
		// jika sudah 5, maka buang token yang paling awal				//
		//////////////////////////////////////////////////////////////////
		if ( prevTokens.size() == 4 ){
			prevTokens.remove(0);
		}
		prevTokens.add(currentTokenWord);
		
		if ( tokensToProcess.size() > 0 ) {
			checkType(prevTokens, tokensToProcess, result);
		}
		
		return result;
	}
	
	private List<SemanticToken> doConcatination(List<String> tokenToConcate, SemanticToken currentToken) {
		
		List<SemanticToken> result = new ArrayList<SemanticToken>();
		int tokenSize = tokenToConcate.size() - 1;
		//////////////////////////////////////////////////////////////////////////////////////////
		// simpan token asli ke dalam variabel cadangan.										//
		// Tujuannya adalah untuk mempertahankan bentuk asli token								//
		// Jika token diambil langsung dari objek current token, maka ada kemungkinan			//
		// token tersebut sudah berubah menjadi token yang sudah di konkatinasi 				//
		// sehingga ketika masih ada isi array tokenToConcat maka hasil konkatinasi				//
		// tidak akan sempurna (akan ada redundansi)											//
		//																						//
		// contoh:																				//
		// [dinas, pendidikan, kabupaten, lombok] dengan timur sebagai currentToken 			//
		// Ketika proses pengecekan konkatinasi lombok_timur akan menghasilkan mapping			//
		// jika menggunakan token yang berasal dari currentToken, maka konkatinasi berikutnya	//
		// akan menjadi kabupaten_lombok_lombok_timur karena currentToken sudah berubah 		//
		// menjadi lombok_timur																	//
		//////////////////////////////////////////////////////////////////////////////////////////
		String originalCurrentToken = currentToken.getToken();
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Untuk masing-masing isi array dari tokenToConcate lakukan proses konkatinasi 		//
		// dengan originalCurrentToken dimulai dari isi array yang paling akhir hingga semua 	//
		// isi array di concate dengan originalCurrentToken										//
		//																						//
		// Note:																				//
		// Batas maksimal jumlah element array tokenToConcate di batasi hanya 4 elemen 			//
		// pembatasan di lakukan di dalam method checkType()									//
		//////////////////////////////////////////////////////////////////////////////////////////
		for ( int i = tokenSize; i >= 0; i-- ) {
			
			String concatinatedWord = null;
			
			//////////////////////////////////////////////////////////////////////////////
			// Proses konkatinasi isi array dimulai dari elemen pertama dari posisi i	//
			// misalnya:																//
			// [dinas, pendidikan, kabupaten, lombok] 									//
			// posisi i berada di kabupaten (i = 2), maka konkatinasinya menjadi:		//
			// kabupaten_lombok															//
			//////////////////////////////////////////////////////////////////////////////
			for ( int j = i; j <= tokenSize; j++) {
				//////////////////////////////////////////////////////////////////////////
				// Untuk menghindari hasil konkatinasi menjadi null_kabupaten_lombok	//
				//////////////////////////////////////////////////////////////////////////
				if ( concatinatedWord == null ){
					concatinatedWord = tokenToConcate.get(j);
				} else {
					concatinatedWord += "_" + tokenToConcate.get(j);
				}
			}
			
			//////////////////////////////////////////////////////////////////////////
			// Setelah token dari array tokenToConcate di sambungkan				//
			// maka sambung di bagian akhir dengan token original					//
			// sehingga menjadi kata yang utuh.										//
			//																		//
			// contoh:																//
			// [dinas, kabupaten, lombok] dg originalToken = timur					//
			// misalnya j berada di posisi kabupaten, maka hasil konkatinasinya		//
			// adalah kabupaten_lombok, dan setelah proses di bawah ini menjadi 	//
			// kabupaten_lombok_timur												//
			//////////////////////////////////////////////////////////////////////////
			concatinatedWord += "_" + originalCurrentToken;
			
			// cek apakah token hasil konkatinasi memiliki mapping di dalam ontologi
			String concatinatedOWLType = getType(concatinatedWord);
			
			//////////////////////////////////////////////////////////////////////////
			// Jika token hasil konkatinasi memiliki mapping di dalam ontologi		//
			// maka buat objek SemantoicToken yang baru dan masukkan ke dalam array	//
			//////////////////////////////////////////////////////////////////////////
			if ( concatinatedOWLType != null ) {
				SemanticToken token = setToken(currentToken, concatinatedWord, concatinatedOWLType); 				
				result.add(token);
			}
		}
		
		return result;
	}
	
	private SemanticToken setToken(SemanticToken tokenObject, String word, String type) {
		
		//////////////////////////////////////////////////////////////////////////
		// Oleh karena objek berbentuk pointer, jadi harus membuat objek baru	//
		// Agar objek sebelumnya tidak ter-override oleh objek yang baru		//
		//////////////////////////////////////////////////////////////////////////
		SemanticToken newToken = new SemanticToken();
		OWLObject OWLPath = getOWLPath(word, type);
		
		newToken.setToken(word);
		newToken.setType(tokenObject.getType());
		newToken.setOWLPath(OWLPath);
		newToken.setOWLType(type);
		
		return newToken;
	}
	
	public String getShortForm(OWLEntity e){
		return shortForm.getShortForm(e);
	}
	
	public String getShortForm(String uri) {
		IRI entityUri = IRI.create(uri);
		OWLEntity entity = super.dataFactory.getOWLClass( entityUri);
		return getShortForm(entity);
	}
	
	public OWLObject getOWLPath(String name, String type){
		
		switch(type){
		case Type.Ontology.CLASS:
			for(OWLClass obj: super.ontology.getClassesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Type.Ontology.OBJECT_PROPERTY:
			for(OWLObjectProperty obj: super.ontology.getObjectPropertiesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Type.Ontology.DATATYPE_PROPERTY:
			for(OWLDataProperty obj: super.ontology.getDataPropertiesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Type.Ontology.INDIVIDUAL:
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
			return Type.Ontology.CLASS;
		}
		
		if(isDatatypeProperty(prop)){
			return Type.Ontology.DATATYPE_PROPERTY;
		}
		
		if(isObjectProperty(prop)){
			return Type.Ontology.OBJECT_PROPERTY;
		}
		
		if(isIndividual(prop)){
			return Type.Ontology.INDIVIDUAL;
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
	
	private boolean isURI(String str){
		return str.matches("^(https?://).*");
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

}
