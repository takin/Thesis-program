package SemanticQA.helpers;

public abstract class StringManipulation {

	
	public static String concate(String str, String link){
		str = str.toLowerCase();
		str = str.substring(0, 1).toUpperCase() + str.substring(1).replace(" ", link);
		return str;
	}
	
	public static String makeCamelCase(String str){
		
		String[] arrStr = str.split(" ");
		String result="";
		
		arrStr[0] = arrStr[0].toLowerCase();
		
		if(arrStr.length > 1){
			
			for(int i = 1; i < arrStr.length; i++){
				arrStr[i] = arrStr[i].substring(0, 1).toUpperCase() + arrStr[i].substring(1).toLowerCase();
			}

		}
		
		for(int i = 0; i < arrStr.length; i++){
			result += arrStr[i];
		}
		return result;
	}
	
}
