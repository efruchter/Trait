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
	public boolean storeVector(final SessionInfo sessionInfo) {

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

        try {
            final String[] record = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equals("username")) {
                    record[i] = sessionInfo.username;
                } else if (headers[i].equals("date")) {
                    record[i] = sessionInfo.date;
                } else if (headers[i].equals("score")) {
                    record[i] = sessionInfo.score;
                } else {
                    record[i] = "" + new GeneVector(sessionInfo.vector).getGene(headers[i]).getValue();
                }
            }
            try {
                write.writeRecord(record);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (final NullPointerException e) {
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
