package com.cos765.client;

import com.cos765.common.Segment;

public class LossDelayEmulator {

	public static Segment doEmulateLossDelay (Segment segment) {
		Segment pSegment = loseByChance(segment);
		
		return delay(segment);
	}
		
	private static Segment delay (Segment segment) {
		// Capturar o tempo atual
		// Atrasar de RTT/2 + X
		// Retornar o Segmento		
		return segment;
	}
	
	private static Segment loseByChance (Segment segment) {
		// Decidir se um segmento é perdido ou não
		return segment;
	}

}
