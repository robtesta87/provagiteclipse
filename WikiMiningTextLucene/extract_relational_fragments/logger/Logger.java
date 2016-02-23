package logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;

public class Logger {
	
	private PrintWriter logFile;
	private String log_file;
	public Logger(String log_file) {
		this.log_file = log_file;
		try {
			setLogFile(new PrintWriter(new BufferedWriter(new FileWriter(log_file, true))));
		} catch (IOException e) {
			System.out.println("errore nella creazione del file di log");
			e.printStackTrace();
		}
 
	}


	
	public synchronized void addResult (Queue<String> logQueue){
		try {
			setLogFile(new PrintWriter(new BufferedWriter(new FileWriter(log_file, true))));
		} catch (IOException e) {
			System.out.println("errore nella creazione del file di log");
			e.printStackTrace();
		}
		String line = null;
		while ((line=logQueue.poll())!=null){
			logFile.println(line);
		}
		
		logFile.close();
	}
	public PrintWriter getLogFile() {
		return logFile;
	}

	public void setLogFile(PrintWriter logFile) {
		this.logFile = logFile;
	}
}
