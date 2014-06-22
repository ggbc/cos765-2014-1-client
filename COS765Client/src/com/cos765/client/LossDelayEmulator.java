package com.cos765.client;

import java.util.concurrent.LinkedBlockingQueue;

import com.cos765.common.Segment;


public class LossDelayEmulator {

	private LinkedBlockingQueue<Segment> delayList = new LinkedBlockingQueue<Segment>();
	
	public LossDelayEmulator(LinkedBlockingQueue<Segment> delayList) {
		this.delayList = delayList;
	}
	
	public void add(Segment s) {
		Segment segment = new Segment(s.getOrder(), s.getPayload(), s.getTime());
		
		
	}
	
	public Segment remove(){
		return null;
	}
			
	private Segment delay (Segment segment) {
		// Atrasar de RTT/2 + X
		// Retornar o Segmento
		segment.setTime(segment.getTime() + 20); // TESTE: + 20 tem que virar +  (RTT/2  + X)
		return segment;
	}
	
	private static Segment loseByChance (Segment segment) {
		// Decidir a partir de "p" se um segmento é perdido ou não
		return segment;
	}

}
