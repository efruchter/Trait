package efruchter.tp.learning.database;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import efruchter.tp.learning.GeneVector;

public class CSVDatabase implements Database {
	
	@Override
	public void init() {

	}
	
	@Override
	public boolean storeVector(final SessionInfo userInfo, final GeneVector vector) {

        final String[] headers;

		try {
			CsvReader r = new CsvReader("database.csv");
            r.readHeaders();
            headers = r.getHeaders();
            r.close();
		} catch (FileNotFoundException e1) {
			return false;
		} catch (IOException e) {
			return false;
		}

        final CsvWriter write;

        try {
            write = new CsvWriter(new FileWriter("database.csv", true), ',');
        } catch (Exception e) {
            return false;
        }


        final String[] record = new String[headers.length];
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
	
	public static void main(final String[] args) {
        final Database d = new CSVDatabase();
		d.init();
	}
	
}
