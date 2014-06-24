package com.cos765.client;

import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.cos765.common.Common;
import com.cos765.common.Segment;

public class BufferConsumer implements Runnable {

	private LinkedList<Segment> buffer;
	private final int SIZE;
	private byte nextSegmentToPlay = 1;
	private byte segmentsNotPlayed = 0;

	public BufferConsumer(LinkedList<Segment> buffer, int size) {	
		this.buffer = buffer;
		this.SIZE = size;
	}

	@Override
	public void run() {
		while (true) {
			try {
				//"A partir deste momento voc�e deve consumir os pacotes a intervalos fixos de 20ms."
				Thread.sleep(20);
				consume();
			} catch (InterruptedException ex) {
				
			}
		}
	}

	private Segment consume() throws InterruptedException {
		while (Common.bufferFull == false) {
			synchronized (buffer) {
				System.out.println("Buffer ainda n�o (re)encheu. " + Thread.currentThread().getName() + " esperando, size: " + buffer.size());
				buffer.wait();
			}
		}

		synchronized (buffer) {
			while (buffer.getFirst().getSequenceNumber() != nextSegmentToPlay) {
				// "Se no momento da leitura do pacote i ele n�ao estiver no buffer, deve-se tocar o pacote com n�umero
				//de sequencia i + 1 (ou o de menor n�umero de sequencia armazenado no buffer) e o pacote i nunca ser�a
				//tocado."					
				// "O pacote i deve ser contabilizado como pacote perdido."
				segmentsNotPlayed++;
				nextSegmentToPlay++;				
			}			
			
			Segment s = (Segment) buffer.removeFirst();	
			nextSegmentToPlay++;
			System.out.println("C: " + buffer.toString());
			if (buffer.size() == 0)
				Common.bufferFull = false;
//			buffer.notifyAll(); 
			return s;
		}
	}
}
