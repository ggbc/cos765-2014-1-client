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
import java.util.concurrent.LinkedBlockingQueue;

import com.cos765.common.Common;
import com.cos765.common.Segment;

public class Client {

	public static void main(String args[]) throws SocketException, UnknownHostException {
		
		Vector buffer = new Vector();
		LinkedBlockingQueue<Integer> delayList = new LinkedBlockingQueue<Integer>();
		
		final int MAX_BUFFER_SIZE = 4;
		Thread producer = new Thread(new Producer(buffer, MAX_BUFFER_SIZE, delayList),
				"Produtor");
		Thread consumer = new Thread(new BufferConsumer(buffer, MAX_BUFFER_SIZE),
				"Consumidor");
		producer.start();
		consumer.start();
		
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[Segment.PAYLOAD_SIZE]; //nome do arquivo solicitado
		byte[] receiveData = new byte[Segment.PAYLOAD_SIZE + Segment.HEADER_SIZE]; // bytes do arquivo recebido 
		byte[] payload = new byte[Segment.PAYLOAD_SIZE]; 
		long receiveTime = 0;
		byte sendOrder = 0;	
	
			
		try {
			// Cliente informa o nome do arquivo desejado
			String fileName = inFromUser.readLine(); 
			sendData = fileName.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Common.SERVER_PORT);
			clientSocket.send(sendPacket);
			
			// Recebendo stream de bytes do arquivo solicitado
			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);

				receiveTime = (new Date()).getTime(); // "Quando um segmento é recebido, o tempo atual t é lido."
				sendOrder = receiveData[0]; 
				payload = Arrays.copyOfRange(receiveData, 1, receiveData.length - 1);

				Segment segment = new Segment(sendOrder, payload, receiveTime);
				
				delayList.put((int) sendOrder);	
				System.out.println("Chegou via UDP: " + delayList.toString());

				// TODO: registrar log do que foi recebido
//				String modifiedSentence = new String(receivePacket.getData());
//				System.out.println("FROM SERVER: " + modifiedSentence);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}		
	}
}

//Vai implementar a camada de simulação de atrasos e perda de dados
//1 - Vai receber cada pacote da camada UDP
//2 - 
class Producer implements Runnable { 

	private Vector buffer;
	private final int SIZE;
	private LinkedBlockingQueue<Integer> list;

	public Producer(Vector buffer, int size, LinkedBlockingQueue<Integer> list) {
		this.buffer = buffer;
		this.SIZE = size;
		this.list = list;
	}

	@Override
	public void run() {
		while(true)
		{
			
			
			try {
				produce((int)list.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
//		for (int i = 0; i < 7; i++) {
//			try {
//				produce(i);
//				Thread.sleep(20);
//			} catch (InterruptedException ex) {
//			}
//		}
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

