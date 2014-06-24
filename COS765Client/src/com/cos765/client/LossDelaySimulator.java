package com.cos765.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.uncommons.maths.random.ExponentialGenerator;
import org.uncommons.maths.random.Probability;

import com.cos765.common.Segment;

public class LossDelaySimulator {

	public static LinkedBlockingQueue<Segment> segmentsList = new LinkedBlockingQueue<Segment>();
	private static double E_x = 0.0; // tempo médio entre os eventos. média da v.a. com distribuição exponencial X
	private static double p = 0.0; // probabilidade de perda de pacotes
	private static long RTT = 0; 
	
	public static void configure() {
		Properties prop = new Properties();			
		InputStream input = null;	
		try {
			input = new FileInputStream("config.properties");

			// get the properties value
			prop.load(input);

			RTT = Long.parseLong(prop.getProperty("RTT"));
			p = Double.parseDouble(prop.getProperty("p"));
			E_x = Double.parseDouble(prop.getProperty("E_x"));		

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Emula perda e atraso
	public static void doSimulate(Segment s) {
		s = loseByChance(s); // pacote será perdido?
		if (s == null)
			return;
		s = delay(s); // se não foi perdido, calcule atraso aleatório
		addSorted(s); // coloque na fila na posição correta
		System.out.println("s: " + s.getOrder()
				+ " colocado na lista de atrasos."
				+ LossDelaySimulator.segmentsList.toString());
	}

	// Adiciona o segmento à fila ordenada na posição correta de acordo com o
	// tempo
	private static void addSorted(Segment s) {
		// Usa uma lista circular temporária para reordenar o conteúdo da fila
		// bloqueante
		LinkedList<Segment> tempList = new LinkedList<Segment>();
		segmentsList.drainTo(tempList);
		tempList.add(s);
		java.util.Collections.sort(tempList); // após inserção do novo elemento,
												// reordena
		segmentsList.addAll(tempList);
	}

	private static Segment delay(Segment segment) {
		double lambda = 1/E_x;
		ExponentialGenerator X = new ExponentialGenerator(lambda, new Random()); // v.a. com distribuição exponencial e média E[X]		
//		double x = -1 * Math.log(1.0 - rnd.nextInt(2)) * E_x;
		double x = X.nextValue(); // valor aletório da v.a. X	
		long tn = 0; // atraso simulado	

		tn = segment.getTime() + RTT/2 + Math.round(x);
				
		System.out.println(x + " : "+ (segment.getTime() - tn));
		segment.setTime(tn); 

		return segment;
	}

	private static Segment loseByChance(Segment segment) {
		Probability lossChance = new Probability(p);		
		if (lossChance.nextEvent(new Random()) == true) {
			System.out.println("perda do segmento: " + segment.toString()); 
			return null;
		} else
			return segment;
	}

}
