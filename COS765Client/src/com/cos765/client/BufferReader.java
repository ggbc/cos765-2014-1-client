package com.cos765.client;

public class BufferReader {
	// é uma thread separada que monitora o inputbuffer e verifica sempre se
	// pode
	// ler dele.
	// quando pode ler, usa sua unica thread para disparar o timer e ao termino
	// do
	// timer ler o primeiro pacote do buffer e passa-lo adiante ao cliente.

	private static InputBuffer buffer = null;

	public BufferReader(InputBuffer buffer) {
		this.buffer = buffer;
	}

	public static void consumeBuffer() {
		// quando o buffer está cheio ele chama este método
		while (buffer.getSize() > 0) {
			buffer.consume();
			System.out.println("CONSUMI UM SEGMENTO DO BUFFER!");
		}
	}

	private static class ReaderThread implements Runnable {
		public void run() {
			consumeBuffer();
			System.out.println(System.currentTimeMillis());
		}
	}

	public static void startThread() {
		Thread t = new Thread(new ReaderThread());
		t.start();
	}
}
