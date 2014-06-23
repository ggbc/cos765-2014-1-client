package com.cos765.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import com.cos765.common.Segment;

public class LossDelayEmulator {
	
	public static LinkedBlockingQueue<Segment> segmentsList = new LinkedBlockingQueue<Segment>();

	// Emula perda e atraso 
	public static void doEmulate(Segment s) {
		s = loseByChance(s); // pacote será perdido?
		if (s == null)
			return;
		s = delay(s); // se não foi perdido, calcule atraso aleatório
		addSorted(s); // coloque na fila na posição correta
//		segmentsList.add(s);
		System.out.println("s: " + s.getOrder() + " colocado na lista de atrasos." + LossDelayEmulator.segmentsList.toString());		
	}

	// Adiciona o segmento à fila ordenada na posição correta de acordo com o tempo
	private static void addSorted(Segment s){
		// Usa uma lista circular temporária para reordenar o conteúdo da fila bloqueante
		LinkedList<Segment> tempList = new LinkedList<Segment>();
		segmentsList.drainTo(tempList);
		tempList.add(s);
		java.util.Collections.sort(tempList); // após inserção do novo elemento, reordena
		segmentsList.addAll(tempList);		
	}	
	
	private static Segment delay(Segment segment) {
		// Atrasar de RTT/2 + X
		segment.setTime(segment.getTime() + (long)(new Random()).nextInt(100)); // TESTE: + 20 tem que virar + (RTT/2 + X)
		return segment;
	}

	private static Segment loseByChance(Segment segment) {
		// Decidir a partir de "p" se um segmento é perdido ou não
		return segment;
	}

}
