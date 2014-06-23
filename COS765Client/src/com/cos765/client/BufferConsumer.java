package com.cos765.client;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.cos765.common.Common;

public class BufferConsumer implements Runnable {

	private Vector buffer;
	private final int SIZE;

	public BufferConsumer(Vector buffer, int size) {
		this.buffer = buffer;
		this.SIZE = size;
	}

	@Override
	public void run() {
		while (true) {
			try {
				// TODO: Se no momento da leitura do pacote i ele n˜ao estiver no buffer, deve-se tocar o pacote com n´umero
//				de sequencia i + 1 (ou o de menor n´umero de sequencia armazenado no buffer) e o pacote i nunca ser´a
//				tocado. 				
				
				Thread.sleep(20);
				consume();
			} catch (InterruptedException ex) {
			}
		}
	}

	private int consume() throws InterruptedException {
		// Se não ficou cheio ainda, espere
		while (Common.bufferFull == false) {
			synchronized (buffer) {
				System.out.println("Buffer ainda não (re)encheu. "
						+ Thread.currentThread().getName()
						+ " esperando, size: " + buffer.size());
				buffer.wait();
			}
		}

		synchronized (buffer) {
			Integer i = (Integer) buffer.remove(0); // TODO: Trocar Integer por Common.Segment
			System.out.println("C: " + buffer.toString());
			if (buffer.size() == 0)
				Common.bufferFull = false;
			buffer.notifyAll();
			return i;
		}
	}
}
