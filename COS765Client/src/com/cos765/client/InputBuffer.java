package com.cos765.client;

import java.util.LinkedList;

import com.cos765.common.Segment;

// O papel do InputBuffer é receber todos os dados NO cliente, 
//gerar o atraso necessário, reordenar os pacotes 
//e só então enviar os mesmos para a aplicação 
public class InputBuffer {

	private static InputBuffer instance = null; //singleton para o buffer
	private static LinkedList<Segment> buffer = new LinkedList<Segment>();
	private static int size = 10; // tamanho default, apenas um chute.
	private static BufferReader consumer;

	private InputBuffer() {
		// size = // ler p a partir de arquivo de configuração
	}

	public static InputBuffer getInstance() {
		if (instance == null)
			instance = new InputBuffer();
		return instance;
	}

	public static int getSize() {
		return size;
	}
	
	public static void setSize(int size) {
		size = size;
	}

	public static void registerConsumer(BufferReader consumer) {
		consumer = consumer;
	}

	public static void add(Segment segment) {
		buffer.add(segment);
		java.util.Collections.sort(buffer);

		// Só pode consumir o buffer quando ele fica cheio	
		if (buffer.size() == size)
			consumer.consumeBuffer();
	}

	public static Segment consume() { // remover primeiro Segmento da fila
		return buffer.removeFirst();
	}

}
