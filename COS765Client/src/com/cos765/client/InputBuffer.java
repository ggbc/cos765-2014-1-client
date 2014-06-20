package com.cos765.client;

import java.util.LinkedList;

import com.cos765.common.Segment;

// O papel do InputBuffer � receber todos os dados NO cliente, 
//gerar o atraso necess�rio, reordenar os pacotes 
//e s� ent�o enviar os mesmos para a aplica��o 
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

		// S� pode consumir o buffer quando ele fica cheio	
		if (buffer.size() == MAX_SIZE)
			consumer.consumeBuffer();
	}

	public static Segment consume() { // remover primeiro Segmento da fila
		return buffer.removeFirst();
	}

}
