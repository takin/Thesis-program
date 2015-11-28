package SemanticQA.controllers;

public class Tester {

	public static void main(String[] args) {
		
		String t = "ini adalah string immutable";
		StringBuffer sb = new StringBuffer("ini adalah string buffer");
		
		long start1 = System.currentTimeMillis();
		
		for (int i = 0; i < 50000; i++) {
			t = t + " tambahan";
		}
		
		long end1 = System.currentTimeMillis();
		System.out.println("immutable -> " + ((end1 - start1)/60) + "s");
		
		long start2 = System.currentTimeMillis();
		
		for (int j = 0; j < 50000; j++) {
			sb = sb.append("tambahan");
		}
		
		long end2 = System.currentTimeMillis();
		System.out.println("mutable -> " + ((end2 - start2) / 60) + "s");
	}
	
}
