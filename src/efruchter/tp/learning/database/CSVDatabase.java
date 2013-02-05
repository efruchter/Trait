package efruchter.tp.learning.database;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;

public class CSVDatabase implements Database {
	
	private final String file;
	
	public CSVDatabase(final String dataFile) {
		file = dataFile;
	}
	
	@Override
	public void init() {

	}
	
	@Override
	public boolean storeVector(final SessionInfo sessionInfo) {

        final String[] headers;

		try {
			CsvReader r = new CsvReader(file);
            r.readHeaders();
            headers = r.getHeaders();
            r.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

        final CsvWriter write;

        try {
            write = new CsvWriter(new FileWriter(file, true), ',');
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }

        try {
            final String[] record = new String[headers.length];
            GeneVector v = new GeneVector(sessionInfo.get("vector"));
            for (int i = 0; i < headers.length; i++) {
            	if (sessionInfo.containsKey(headers[i])) {
            		record[i] = sessionInfo.get(headers[i]);
            	} else {
                    record[i] = "" + v.getGene(headers[i]).getValue();
                }
            }
            try {
                write.writeRecord(record);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (final NullPointerException e) {
        	e.printStackTrace();
            return false;
        }

		write.close();
		return true;
	}
}
