package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TestNAT {
	public static void main(String[] args) throws IOException {
		FileReader f = new FileReader("./Seconda_fase/util/nationality.tsv");
		BufferedReader br = new BufferedReader(f);
		String line = "";
		ArrayList<String> nationality_list = new ArrayList<String>(); 
		while((((line = br.readLine())!=null))){
			nationality_list.add(line);
		}
		String fragment = "is an Italian";
		for (String nationality : nationality_list) {
			fragment = fragment.replace(nationality, "NAT");
		}
		System.out.println(fragment);
	}
}
