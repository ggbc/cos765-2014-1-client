package com.cos765.client;

public class BufferReader {

	private static InputBuffer buffer = null;
	private static Thread t;
	
	public BufferReader(InputBuffer buffer) {
		t = new Thread(new ReaderThread());		
		BufferReader.buffer = buffer;
	}

	public static void consumeBuffer() {
		if (!t.isAlive())
			t.run();
	}

	private static class ReaderThread implements Runnable {
		public void run() {
			while (BufferReader.buffer.getSize() > 0) {
				try {
					Thread.sleep(20);
					BufferReader.buffer.consume();					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
