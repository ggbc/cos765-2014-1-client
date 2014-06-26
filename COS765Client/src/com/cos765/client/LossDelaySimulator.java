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

import com.cos765.common.Common;
import com.cos765.common.Common.Statistics;
import com.cos765.common.Segment;

public class LossDelaySimulator {

	public static LinkedBlockingQueue<Segment> segmentsList = new LinkedBlockingQueue<Segment>();
	private static double E_x = 500.0; // tempo médio E[x] entre os eventos. média da v.a. com distribuição exponencial X
	private static double p = 0.3; // probabilidade de perda de pacotes
	private static long RTT = 100; 	
	
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
			Common.maxBufferSize = Integer.parseInt(prop.getProperty("max_buffer_size"));

		} catch(FileNotFoundException ex) {

			try {
				OutputStream output = new FileOutputStream("config.properties");

				prop.setProperty("RTT", ((Long)RTT).toString());
				prop.setProperty("p", ((Double)p).toString());
				prop.setProperty("E_x", ((Double)E_x).toString());
				prop.setProperty("max_buffer_size", ((Integer)Common.maxBufferSize).toString());
 
				prop.store(output, null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
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
//		System.out.println("s: " + s.getSequenceNumber() + " colocado na lista de atrasos." + LossDelaySimulator.segmentsList.toString());
	}

	// Adiciona o segmento à fila ordenada na posição correta de acordo com o
	// tempo
	private static void addSorted(Segment s) {
		// Usa uma lista circular temporária para reordenar o conteúdo da fila bloqueante
		LinkedList<Segment> tempList = new LinkedList<Segment>();
		segmentsList.drainTo(tempList);
		tempList.add(s);
		java.util.Collections.sort(tempList); // após inserção do novo elemento, reordena
		segmentsList.addAll(tempList);
	}

	private static Segment delay(Segment segment) {
		double lambda = 1/E_x;
		ExponentialGenerator X = new ExponentialGenerator(lambda, new Random()); // v.a. com distribuição exponencial e média E[X]		
		double x = X.nextValue(); // valor aletório da v.a. X	
		long tn = 0; // atraso simulado	

		tn = segment.getTime() + RTT/2 + Math.round(x);				
		segment.setTime(tn);
		return segment;
	}

	private static Segment loseByChance(Segment segment) {
		Probability lossChance = new Probability(p);		
		if (lossChance.nextEvent(new Random()) == true) {
//			System.out.println("perda do segmento: " + segment.toString());
			Statistics.lostSegments++;			
			return null;
		} else
			return segment;
	}

}

class BufferProducer implements Runnable {

	private LinkedList<Segment> buffer;
	private final int SIZE;
	private int lastBufferSegment = 0;
	
	public BufferProducer(LinkedList<Segment> buffer, int size) {
		this.buffer = buffer;
		this.SIZE = size;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1); // a cada 1 ms verifica se está na hora de enviar algo para o buffer

				Segment segment = LossDelaySimulator.segmentsList.peek();
				if (segment != null)
					if (segment.getTime() <= (new Date().getTime())) {
						// "Por outro lado, um pacote que chegar da rede
						// mas j´a estiver expirado nunca deve ser armazenado no buffer."
						if (segment.getSequenceNumber() < lastBufferSegment) {
//							System.out.println("segto EXPIRADO. " + segment.toString());	
							Statistics.expiredSegments++;
							LossDelaySimulator.segmentsList.poll();
						} else {
							synchronized (buffer) {
								if (buffer.size() == SIZE) {								
//									System.out.println("BUFFER JÁ ESTÁ CHEIO!! " + segment.toString() + " substituirá: " + buffer.getFirst());
									Statistics.discardedSegments++;
									buffer.removeFirst();
								}
//								System.out.println("s: " + segment.getSequenceNumber() + " now: " + (new Date().getTime()) + " seg.t:" +  segment.getTime());
								produce(LossDelaySimulator.segmentsList.take());						
//								System.out.println("s: " + segment.getSequenceNumber() + " retirado da lista de atrasos: " + LossDelaySimulator.segmentsList.toString());
							}
						}
					} 
//					else System.out.println("now: " + (new Date().getTime()) + " seg.t:" +  segment.getTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void produce(Segment s) throws InterruptedException {

		// Esta condição não precisa existir porque o buffer já não vai parar de receber dados quando estiver cheio, mas sim vai descartar o mais velho. 
//		while (buffer.size() == SIZE) {
//			synchronized (buffer) {
//				System.out.println("Buffer cheio. " + Thread.currentThread().getName() + " esperando, size: " + buffer.size());			
//				buffer.wait();
//			}
//		}

		// producing element and notify consumers
		synchronized (buffer) {
			if (Statistics.initialTransferTime == 0) Statistics.initialTransferTime = (new Date()).getTime() - 1;				
			
			buffer.add(s);
			if (s.getSequenceNumber() > lastBufferSegment) { 
				lastBufferSegment = s.getSequenceNumber();
			}
						
			if (buffer.size() == SIZE) {
				if (Common.bufferFull == false) { 
					Common.returnedFromPause = true;
				}
				Common.bufferFull = true; 
			}
			System.out.println("P: " + buffer.toString());
			buffer.notifyAll(); // só permite consumir quando esteve cheio em
								// algum momento
						
			//Medir vazão		
			Statistics.totalTransferTime = ((new Date().getTime()) - Statistics.initialTransferTime);						
			Statistics.totalTransferSize += s.getPayload().length;
			Statistics.throughput = (Statistics.totalTransferSize * 8 * 1000) / Statistics.totalTransferTime; 
			
//			System.out.println("Total recebido (bytes): " + Statistics.totalTransferSize + 
//					" Tempo total: " + Statistics.totalTransferTime + 
//					" Vazão: " + Statistics.throughput + " bps");									
			
		}
	}
}
