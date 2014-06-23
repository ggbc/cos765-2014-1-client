package com.cos765.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import com.cos765.common.Common;
import com.cos765.common.Segment;

public class Client {

	public static void main(String args[]) throws SocketException, UnknownHostException {

		Vector buffer = new Vector();

		Thread producer = new Thread(new Producer(buffer,
				Common.MAX_BUFFER_SIZE), "Produtor");
		Thread consumer = new Thread(new BufferConsumer(buffer,
				Common.MAX_BUFFER_SIZE), "Consumidor");
		producer.start();
		consumer.start();

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[Segment.PAYLOAD_SIZE]; // nome do arquivo
															// solicitado
		byte[] receiveData = new byte[Segment.PAYLOAD_SIZE
				+ Segment.HEADER_SIZE]; // bytes do arquivo recebido
		byte[] payload = new byte[Segment.PAYLOAD_SIZE];
		long receiveTime = 0;
		byte sendOrder = 0;

		try {
			// Cliente informa o nome do arquivo desejado
			String fileName = inFromUser.readLine();
			sendData = fileName.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, Common.SERVER_PORT);
			clientSocket.send(sendPacket);

			// Recebendo stream de bytes do arquivo solicitado
			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				clientSocket.receive(receivePacket);

				receiveTime = (new Date()).getTime(); // "Quando um segmento é recebido, o tempo atual t é lido."
				sendOrder = receiveData[0];
				payload = Arrays.copyOfRange(receiveData, 1,
						receiveData.length - 1);

				Segment segment = new Segment(sendOrder, payload, receiveTime);

				// A thread produtora vai ler a lista de atrasos e produzir um
				// segmento para a camada acima somente no tempo certo
				LossDelayEmulator.doEmulate(segment);

				// String modifiedSentence = new
				// String(receivePacket.getData());
				// System.out.println("FROM SERVER: " + modifiedSentence);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}
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
		while (true) {
			try {
				Thread.sleep(1);

				// TODO: Caso o buffer esteja cheio, deve-se remover o pacote
				// mais velho que
				// encontra-se no buffer para dar espa¸co ao novo pacote.

				// TODO: Por outro lado, um pacote que chegar da rede
				// mas j´a estiver expirado nunca deve ser armazenado no buffer.

				Segment segment = LossDelayEmulator.segmentsList.peek();
				if (segment != null)
					if (segment.getTime() == (new Date().getTime())) {
						System.out.println("s: " + segment.getOrder() + " now: " + segment.getTime() + " seg.t:" + (new Date().getTime()));
						produce(LossDelayEmulator.segmentsList.take());						
						System.out.println("s: " + segment.getOrder() + " retirado da lista de atrasos: " + LossDelayEmulator.segmentsList.toString());
					} else
						System.out.println("now: " + (new Date().getTime()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void produce(Segment s) throws InterruptedException {

		while (buffer.size() == SIZE) {
			synchronized (buffer) {
				System.out.println("Buffer cheio. " + Thread.currentThread().getName() + " esperando, size: " + buffer.size());
				buffer.wait();
			}
		}

		// producing element and notify consumers
		synchronized (buffer) {
			buffer.add(s);
			if (buffer.size() == SIZE) { 
				Common.bufferFull = true; 
			}
			System.out.println("P: " + buffer.toString());
			buffer.notifyAll(); // só permite consumir quando esteve cheio em
								// algum momento
		}
	}
}
