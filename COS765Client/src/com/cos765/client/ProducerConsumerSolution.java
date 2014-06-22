package com.cos765.client;
import java.util.Vector;

import com.cos765.common.Common;

public class ProducerConsumerSolution {

	public static void main(String args[]) {
		
		Vector buffer = new Vector();
		final int MAX_BUFFER_SIZE = 4;
		Thread producer = new Thread(new Producer(buffer, MAX_BUFFER_SIZE),
				"Produtor");
		Thread consumer = new Thread(new BufferConsumer(buffer, MAX_BUFFER_SIZE),
				"Consumidor");
		producer.start();
		consumer.start();
	}
}

class Producer implements Runnable {

	private Vector buffer;
	private final int SIZE;

	public Producer(Vector buffer, int size) {
		this.buffer = buffer;
		this.SIZE = size;
	}

	@Override
	public void run() {
		for (int i = 0; i < 7; i++) {
			try {
				produce(i);
				Thread.sleep(20);
			} catch (InterruptedException ex) {
			}
		}
	}

	private void produce(int i) throws InterruptedException {

		while (buffer.size() == SIZE) {
			synchronized (buffer) {
				System.out.println("Buffer cheio. "
						+ Thread.currentThread().getName()
						+ " esperando, size: " + buffer.size());
				buffer.wait();
			}
		}

		// producing element and notify consumers
		synchronized (buffer) {
			buffer.add(i);
			if (buffer.size() == SIZE)
				Common.bufferFull = true;
			System.out.println("P: " + buffer.toString());
			buffer.notifyAll(); // só permite consumir quando esteve cheio em
								// algum momento
		}
	}
}

