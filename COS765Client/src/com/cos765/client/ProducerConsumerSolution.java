package com.cos765.client;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerSolution {

	public static Boolean bufferFull = false;

	public static void main(String args[]) {

		Vector buffer = new Vector();
		int MAX_SIZE = 4;
		Thread producer = new Thread(new Producer(buffer, MAX_SIZE),
				"Produtor");
		Thread consumer = new Thread(new Consumer(buffer, MAX_SIZE),
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
				ProducerConsumerSolution.bufferFull = true;
			System.out.println("P: " + buffer.toString());
			buffer.notifyAll(); // só permite consumir quando esteve cheio em
								// algum momento
		}
	}
}

class Consumer implements Runnable {

	private Vector buffer;
	private final int SIZE;

	public Consumer(Vector buffer, int size) {
		this.buffer = buffer;
		this.SIZE = size;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(20);
				consume();
			} catch (InterruptedException ex) {
			}
		}
	}

	private int consume() throws InterruptedException {
		// Se não ficou cheio ainda, espere
		while (ProducerConsumerSolution.bufferFull == false) {
			synchronized (buffer) {
				System.out.println("Buffer esvaziou. "
						+ Thread.currentThread().getName()
						+ " esperando, size: " + buffer.size());
				buffer.wait();
			}
		}

		synchronized (buffer) {
			Integer i = (Integer) buffer.remove(0);
			System.out.println("C: " + buffer.toString());
			if (buffer.size() == 0)
				ProducerConsumerSolution.bufferFull = false;
			buffer.notifyAll();
			return i;
		}
	}
}
