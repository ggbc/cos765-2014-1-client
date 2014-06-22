package com.cos765.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import com.cos765.common.Common;
import com.cos765.common.Segment;

public class Client {
			
	public static void main(String[] args) throws Exception {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[Segment.PAYLOAD_SIZE]; //nome do arquivo solicitado
		byte[] receiveData = new byte[Segment.PAYLOAD_SIZE + Segment.HEADER_SIZE]; // bytes do arquivo recebido 
		byte[] payload = new byte[Segment.PAYLOAD_SIZE]; 
		long receiveTime = 0;
		byte sendOrder = 0;

//		InputBuffer inBuffer = new InputBuffer();
//		BufferConsumer consumer = new BufferConsumer(inBuffer);
					
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
//				inBuffer.add(segment);
				//TODO: levar para camada de atraso
				//TODO: levar para camada de buffer
				//TODO: levar para camada de aplicação (esta de novo?) 

				// TODO: registrar log do que foi recebido
				String modifiedSentence = new String(receivePacket.getData());
				System.out.println("FROM SERVER: " + modifiedSentence);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}
	}
}

