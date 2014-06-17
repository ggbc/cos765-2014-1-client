package com.cos765.client;

import java.util.LinkedList;

import com.cos765.common.Segment;

// O papel do InputBuffer é receber todos os dados NO cliente, 
//gerar o atraso necessário, reordenar os pacotes 
//e só então enviar os mesmos para a aplicação 
public class InputBuffer {

	private LinkedList<Segment> buffer = new LinkedList<Segment>();

	public boolean add(Segment segment) {
		// O que fazer quando o buffer ficar cheio?
		buffer.add(segment);
		java.util.Collections.sort(buffer);		
		return true;
	}
	
	public Segment remove(int index) {
		return buffer.remove(index);		
	}

}
