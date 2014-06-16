package com.cos765.client;

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import com.cos765.common.Segment;

// O papel do InputBuffer é receber todos os dados NO cliente, 
//gerar o atraso necessário, reordenar os pacotes 
//e só então enviar os mesmos para a aplicação 
public class InputBuffer {

	private byte length = 0;
	private LinkedList<Segment> buffer = new LinkedList<Segment>();

	public byte getLength() {
		return length;
	}

	public void setLength(byte length) {
		this.length = length;
	}

	public boolean add(Segment segment) {
		// O que fazer quando o buffer ficar cheio?
		//
		buffer.add(segment);
		length++;
		return true;
	}
	
	public Segment remove(int index) {
		return buffer.remove(index);		
	}

}
