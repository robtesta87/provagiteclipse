package Printer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;

public class PrinterOutput {
	private PrintWriter output_file;
	private String output_file_path;
	
	public PrinterOutput(String output_file_path) {
		this.output_file_path = output_file_path;
		try {
			setLogFile(new PrintWriter(new BufferedWriter(new FileWriter(output_file_path, true))));
		} catch (IOException e) {
			System.out.println("errore nella creazione del file di log");
			e.printStackTrace();
		}

	}



	public synchronized void addResult (Queue<String> logQueue){
		try {
			setLogFile(new PrintWriter(new BufferedWriter(new FileWriter(output_file_path, true))));
		} catch (IOException e) {
			System.out.println("errore nella creazione del file di log");
			e.printStackTrace();
		}
		String line = null;
		while ((line=logQueue.poll())!=null){
			output_file.println(line);
		}

		output_file.close();
	}
	public PrintWriter getLogFile() {
		return output_file;
	}

	public void setLogFile(PrintWriter output_file) {
		this.output_file = output_file;
	}
}
