package com.cos765.client;

import java.util.LinkedList;

import com.cos765.common.Segment;

// O papel do InputBuffer é receber todos os dados NO cliente, 
//gerar o atraso necessário, reordenar os pacotes 
//e só então enviar os mesmos para a aplicação 
public class InputBuffer {

	private static InputBuffer instance = null; //singleton para o buffer
	private static LinkedList<Segment> buffer = new LinkedList<Segment>();
	private static int MAX_SIZE = 10; // tamanho default, apenas um chute.
	private static BufferReader consumer;

	private InputBuffer() {

	}

	public static InputBuffer getInstance() {
		if (instance == null)
			instance = new InputBuffer();
		return instance;
	}

	public static int getSize() {
		return buffer.size();
	}

	public static void registerConsumer(BufferReader consumer) {
		InputBuffer.consumer = consumer;
	}

	public static void add(Segment segment) {
		buffer.add(segment);

		// "vocˆe deve esperar o buffer encher para come¸car a ler os pacotes."	
		if (buffer.size() == MAX_SIZE)
			consumer.consumeBuffer();
	}

	public static Segment consume() { 
		return buffer.removeFirst();
	}

}
