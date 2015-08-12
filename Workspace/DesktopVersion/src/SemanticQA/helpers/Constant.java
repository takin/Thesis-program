/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author syamsul
 */
public abstract class Constant {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/";
    public static final String DB_NAME = "kamuskata";
    public static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "akinaja";
    
    public static final String ONTOPAR_URL = "http://local.co/ontologi/ntbpar.owl";
    public static final String ONTOGOV_URL = "http://local.co/ontologi/ntbgov.owl";
    public static final String ONTOGEO_URL = "http://local.co/ontologi/ntbgeo.owl";
    
    public static final String ONTO_MERGED_URI = "http://ntbprov.go.id/semweb/resource/";
    
    public static final boolean USE_REASONER = true;
    
    @SuppressWarnings({ "serial", "unchecked", "rawtypes" })
	public static final List<String> ONTOLOGIES = new ArrayList(){{add(ONTOGEO_URL);add(ONTOGOV_URL); add(ONTOPAR_URL);}};
}
