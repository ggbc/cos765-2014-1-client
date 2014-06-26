package com.cos765.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import com.cos765.common.Common;
import com.cos765.common.Common.Metrics;
import com.cos765.common.Segment;
import com.cos765.common.MetricsPrinter;

public class Client {
	
	public static void main(String args[]) throws SocketException, UnknownHostException {		
		
		LossDelaySimulator.configure(); // ler parâmetros de configuração do simulador		
		
		LinkedList<Segment> buffer = new LinkedList<Segment>();
		Thread producer = new Thread(new BufferProducer(buffer, Common.maxBufferSize), "Produtor");
		Thread consumer = new Thread(new BufferConsumer(buffer, Common.maxBufferSize), "Consumidor");
		Thread statisticsPrinter = new Thread(new MetricsPrinter(), "Provedor de Estatísticas");
				
		producer.start();
		consumer.start();
		statisticsPrinter.start();

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[Segment.PAYLOAD_SIZE]; // nome do arquivo solicitado
		byte[] receiveData = new byte[Segment.PAYLOAD_SIZE + Segment.HEADER_SIZE]; // bytes do arquivo recebido
		byte[] payload = new byte[Segment.PAYLOAD_SIZE];
		long receiveTime = 0;
		int sequenceNumber = 0;


		try {
			System.out.println("Digite o nome do arquivo desejado (ex. test.txt): ");
			
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
				
				sequenceNumber = getSequenceNumber(getHeader(receiveData));	
				payload = getPayload(receiveData);				
				receiveTime = (new Date()).getTime(); // "Quando um segmento é recebido, o tempo atual t é lido."

				Segment segment = new Segment(sequenceNumber, payload, receiveTime);
				LossDelaySimulator.doSimulate(segment);
				
				Metrics.receivedSegments++;				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}
	}
	
	private static int getSequenceNumber(ByteBuffer header) {
		return header.getInt();
	}
	
	private static ByteBuffer getHeader(byte[] receiveData) {
		byte headerBytes[] = Arrays.copyOfRange(receiveData, 0, Segment.HEADER_SIZE); 
		ByteBuffer header = ByteBuffer.wrap(headerBytes);
		return header;
	}	
	
	private static byte[] getPayload(byte[] receiveData) {
		return Arrays.copyOfRange(receiveData, Segment.HEADER_SIZE,
				Segment.HEADER_SIZE + Segment.PAYLOAD_SIZE);
	}	
}


