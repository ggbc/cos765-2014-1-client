package com.cos765.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.InetAddress;

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

		String dataSource = inFromUser.readLine(); // cliente informa o nome do
													// arquivo desejado
		sendData = dataSource.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, IPAddress, PORT);
		clientSocket.send(sendPacket);
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("FROM SERVER: " + modifiedSentence);
		}
		// clientSocket.close();
	}
}
