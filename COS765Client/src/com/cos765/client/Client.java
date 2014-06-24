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
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import org.uncommons.maths.random.Probability;

import com.cos765.common.Common;
import com.cos765.common.Segment;

public class Client {

	public static void main(String args[]) throws SocketException, UnknownHostException {		
		
		LossDelaySimulator.configure(); // ler parâmetros de configuração do simulador		
		
		LinkedList<Segment> buffer = new LinkedList<Segment>();
		Thread producer = new Thread(new BufferProducer(buffer, Common.maxBufferSize), "Produtor");
		Thread consumer = new Thread(new BufferConsumer(buffer, Common.maxBufferSize), "Consumidor");		
				
		producer.start();
		consumer.start();

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[Segment.PAYLOAD_SIZE]; // nome do arquivosolicitado
		byte[] receiveData = new byte[Segment.PAYLOAD_SIZE + Segment.HEADER_SIZE]; // bytes do arquivo recebido
		byte[] payload = new byte[Segment.PAYLOAD_SIZE];
		long receiveTime = 0;
		byte sequenceNumber = 0;

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
				sequenceNumber = receiveData[0];
				payload = Arrays.copyOfRange(receiveData, 1,
						receiveData.length - 1);

				Segment segment = new Segment(sequenceNumber, payload, receiveTime);
				LossDelaySimulator.doSimulate(segment);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}
	}
}


