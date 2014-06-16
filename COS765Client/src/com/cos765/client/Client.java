package com.cos765.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

import com.cos765.common.Segment;

public class Client {

	private static int PORT = 15000;
	private static int PACKET_SIZE = 160; // 160 bytes por pacote

	public static void main(String[] args) throws Exception {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[PACKET_SIZE];
		byte[] receiveData = new byte[PACKET_SIZE];
		long t = 0; // tempo de recebimento de um pacote
		byte order = 0;

		String fileName = inFromUser.readLine(); // cliente informa o nome do
													// arquivo desejado
		sendData = fileName.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, IPAddress, PORT);
		clientSocket.send(sendPacket);
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			t = (new Date()).getTime(); // "Quando um segmento � recebido, o tempo atual t � lido."
			Segment s = new Segment(++order, receiveData, t);
			
			System.out.println(t);
			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("FROM SERVER: " + modifiedSentence);
		}
		// clientSocket.close();
	}
}