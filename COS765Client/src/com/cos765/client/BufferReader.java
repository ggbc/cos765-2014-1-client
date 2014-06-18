package com.cos765.client;

public class BufferReader {
	// é uma thread separada que monitora o inputbuffer e verifica sempre se pode ler dele.
	// quando pode ler, usa sua unica thread para disparar o timer e ao termino do timer ler o primeiro pacote do buffer e passa-lo adiante ao cliente.
}
