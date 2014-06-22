package com.cos765.client;

import java.util.LinkedList;

import com.cos765.common.Segment;

public class InputBuffer {

	private LinkedList<Segment> buffer = new LinkedList<Segment>();
	private int MAX_SIZE = 5; // tamanho default, apenas um chute.
	private BufferConsumer consumer;

	public void add(Segment segment) {
		System.out.println("Adicionei: " + segment.getOrder());
		this.buffer.add(segment);
		if (this.buffer.size() == MAX_SIZE)
			this.consumer.startConsuming();
	}

	public void consume() {
		System.out.println("Removi: " + buffer.getFirst().getOrder());
		this.buffer.removeFirst();
	}

	// o consumidor que será avisado quando puder ler o buffer
	public void registerConsumer(BufferConsumer consumer) {
		this.consumer = consumer;
	}

	public int getSize() {
		return buffer.size();
	}
}
