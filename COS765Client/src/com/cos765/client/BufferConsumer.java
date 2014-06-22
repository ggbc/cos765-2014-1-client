package com.cos765.client;

public class BufferConsumer {

	private InputBuffer buffer;
//	private Thread t;

	public BufferConsumer(InputBuffer buffer) {
		this.buffer = buffer;
		this.buffer.registerConsumer(this);
	}

	public void startConsuming() {
		Thread t = new Thread(new ReaderThread());		
		t.start();
	}

	private class ReaderThread implements Runnable {
		public void run() {
			while (buffer.getSize() > 0) {
				buffer.consume();
			}
		}
	}
}
