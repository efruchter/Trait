package efruchter.tp.learning.database;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import efruchter.tp.learning.GeneVector;

public class CSVDatabase implements Database {
	
	private String[] headers;
	
	@Override
	public void init() {
		try {
			CsvReader r = new CsvReader("database.csv");
			r.readHeaders();
			headers = r.getHeaders();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean storeVector(SessionInfo userInfo, GeneVector vector) {
		
		CsvWriter write = new CsvWriter("database.csv");
		
		try {
			CsvReader r = new CsvReader("database.csv");
			while (r.readRecord()) {
				write.writeRecord(r.getValues());
			}
		} catch (FileNotFoundException e1) {
			return false;
		} catch (IOException e) {
			return false;
		}
		
		String[] record = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].equals("username")) {
				record[i] = userInfo.username;
			} else if (headers[i].equals("date")) {
				record[i] = userInfo.date;
			} else if (headers[i].equals("score")) {
				record[i] = userInfo.score;
			} else {
				record[i] = "" + vector.getGene(headers[i]).getValue();
			}
		}
		
		try {
			write.writeRecord(record);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		write.close();
		return true;
	}
	
	public static void main(String[] args) {
		Database d = new CSVDatabase();
		d.init();
	}
	
}
