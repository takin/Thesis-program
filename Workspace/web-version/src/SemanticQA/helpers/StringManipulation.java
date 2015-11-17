package SemanticQA.helpers;

public abstract class StringManipulation {

	public static final String MODEL_UNDERSCORE = "underscore";
	public static final String MODEL_CAMELCASE = "camel case";
	
	public static String concate(String str, String model){
		
		str.toLowerCase();
		
		switch (model) {
		case MODEL_UNDERSCORE :
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
			str = str.replace(" ", "_");
			break;
		case MODEL_CAMELCASE :
			str = makeCamelCase(str);
			break;
		}
		
		return str;
	}
	
	private static String makeCamelCase(String str){
		
		String[] arrStr = str.split(" ");
		
		str = arrStr[0];
		
		if(arrStr.length > 1){
			
			for(int i = 1; i < arrStr.length; i++){
				str += arrStr[i].substring(0, 1).toUpperCase() + arrStr[i].substring(1).toLowerCase();
			}

		}
		
		return str;
	}
	
	public static String split(String stringToSplit) {
		
		return stringToSplit.replaceAll("[_]", " ");
		
	}
	
	public static String capitalize(String str) {
		String[] strings = str.split(" ");
		String[] capitalizedStrings = new String[strings.length];
		
		for ( int i = 0; i < strings.length; i++ ) {
			capitalizedStrings[i] = strings[i].substring(0, 1).toUpperCase() + strings[i].substring(1, strings[i].length());
		}
		
		return String.join(" ", capitalizedStrings);
	}
}
