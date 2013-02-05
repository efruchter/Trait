package efruchter.tp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneCurve;

public class GeneFileBuilder {

	public static void main(String[] args) {
		final GeneVector geneVector = new GeneVector();
		geneVector.storeGene("player.move.drag", new Gene("Air Drag", "Amount of air drag."), false);
		geneVector.storeGene("player.radius.radius", new Gene("Radius", "Player ship radius", 2, 50, 30), false);
		geneVector.storeGeneCurve("spawner.enemy.radius", new GeneCurve("baseRadius", "Base enemy radius.", 2, 50, 15), false);
		
		try {
			FileWriter fstream = new FileWriter("geneText.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(geneVector.toDataString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getVectorFromFile(String fileName) {
		BufferedReader br = null;
		String geneVec = "";
		try {
			String sCurrentLine;
			
			br = new BufferedReader(new FileReader(fileName));
			
			while ((sCurrentLine = br.readLine()) != null) {
				geneVec += sCurrentLine;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return geneVec;
	}

}
