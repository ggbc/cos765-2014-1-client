package com.cos765.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.uncommons.maths.random.ExponentialGenerator;
import org.uncommons.maths.random.Probability;

import com.cos765.common.Segment;

public class LossDelayEmulator {

	public static LinkedBlockingQueue<Segment> segmentsList = new LinkedBlockingQueue<Segment>();
	private static double E_x = 0.0;
	private static double p = 0.0;
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
	public static void doEmulate(Segment s) {
		s = loseByChance(s); // pacote ser� perdido?
		if (s == null)
			return;
		s = delay(s); // se n�o foi perdido, calcule atraso aleat�rio
		addSorted(s); // coloque na fila na posi��o correta
		System.out.println("s: " + s.getOrder()
				+ " colocado na lista de atrasos."
				+ LossDelayEmulator.segmentsList.toString());
	}

	// Adiciona o segmento � fila ordenada na posi��o correta de acordo com o
	// tempo
	private static void addSorted(Segment s) {
		// Usa uma lista circular tempor�ria para reordenar o conte�do da fila
		// bloqueante
		LinkedList<Segment> tempList = new LinkedList<Segment>();
		segmentsList.drainTo(tempList);
		tempList.add(s);
		java.util.Collections.sort(tempList); // ap�s inser��o do novo elemento,
												// reordena
		segmentsList.addAll(tempList);
	}

	private static Segment delay(Segment segment) {
		// TODO:
		ExponentialGenerator e = new ExponentialGenerator(0.4, new Random());
		e.nextValue();

		segment.setTime(segment.getTime() + (long) (new Random()).nextInt(100)); // TODO:
																					// tn
																					// =
																					// t
																					// +
																					// RTT/2
																					// +
																					// X
		return segment;
	}

	private static Segment loseByChance(Segment segment) {
		Probability lossChance = new Probability(p);		
		if (lossChance.nextEvent(new Random()) == true)
			return null;
		else
			return segment;
	}

}
