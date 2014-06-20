package com.cos765.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import com.cos765.common.*;

public class Client {

	private static int PORT = 15000;

	public static void main(String[] args) throws Exception {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[Segment.PAYLOAD_SIZE]; 
		byte[] receiveData = new byte[Segment.PAYLOAD_SIZE + Segment.HEADER_SIZE]; 
		byte[] payload = new byte[Segment.PAYLOAD_SIZE]; // somente o payload do pacote recebido
		long t = 0; // tempo de recebimento de um pacote
		byte order = 0;
		InputBuffer inBuffer = InputBuffer.getInstance();
//		BufferReader bufReader = new BufferReader();
//		bufReader.startThread();

		try {
			// Cliente informa o nome do arquivo desejado
			String fileName = inFromUser.readLine(); 
			sendData = fileName.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
			clientSocket.send(sendPacket);
			
			// Recebendo stream de bytes do arquivo solicitado
			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);

				t = (new Date()).getTime(); // "Quando um segmento é recebido, o tempo atual t é lido."
				order = receiveData[0]; // ordem de envio do pacote
				payload = Arrays.copyOfRange(receiveData, 1, receiveData.length - 1);

				Segment segment = new Segment(order, payload, t);
				segment = LossDelayEmulator.doEmulate(segment); // Emulação de perdas e atrasos.
				inBuffer.add(segment);

				String modifiedSentence = new String(receivePacket.getData());
				System.out.println("FROM SERVER: " + modifiedSentence);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}
	}
}
