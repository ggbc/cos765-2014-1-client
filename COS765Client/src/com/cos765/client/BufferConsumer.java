package com.cos765.client;

import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.cos765.common.Common;
import com.cos765.common.Common.Metrics;
import com.cos765.common.Segment;

public class BufferConsumer implements Runnable {

	private LinkedList<Segment> buffer;
	private final int SIZE;
	private int nextSegmentToPlay = 1;

	public BufferConsumer(LinkedList<Segment> buffer, int size) {	
		this.buffer = buffer;
		this.SIZE = size;
	}

	@Override
	public void run() {
		while (true) {
			try {
				//"A partir deste momento vocˆe deve consumir os pacotes a intervalos fixos de 20ms."
				Thread.sleep(Common.SLEEP_TIME);
				consume();				
			} catch (InterruptedException ex) {
				
			}
		}
	}

	private Segment consume() throws InterruptedException {

		while (Common.bufferFull == false) {
			synchronized (buffer) {
//				System.out.println("Buffer ainda não (re)encheu. " + Thread.currentThread().getName() + " esperando, size: " + buffer.size());
				if (Common.Metrics.pauseCount == 1) Metrics.pauseStartTime = (new Date()).getTime(); // quando o programa começa o buffer tb está vazio 
				buffer.wait();
			}
		}

		if (Common.returnedFromPause == true) {
			// Quando voltar a consumir, marca o fim da pausa
			Common.returnedFromPause = false;			
			Metrics.pauseEndTime = (new Date()).getTime();		
			Metrics.totalPauseTime += (Metrics.pauseEndTime - Metrics.pauseStartTime);
		}	
		
		synchronized (buffer) {
			while (buffer.getFirst().getSequenceNumber() != nextSegmentToPlay) {
				// "Se no momento da leitura do pacote i ele n˜ao estiver no buffer, deve-se tocar o pacote com n´umero
				//de sequencia i + 1 (ou o de menor n´umero de sequencia armazenado no buffer) e o pacote i nunca ser´a
				//tocado."					
				// "O pacote i deve ser contabilizado como pacote perdido."
				nextSegmentToPlay++;				
			}
		
			Segment s = (Segment) buffer.removeFirst();
			Metrics.playedSegments++;			
			nextSegmentToPlay++;
//			System.out.println("C: " + buffer.toString());
			if (buffer.size() == 0) {
				Common.bufferFull = false;
				Metrics.pauseStartTime = (new Date()).getTime();
				Metrics.pauseCount++;					
			}
			return s;
		}
	}
}
