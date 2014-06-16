package com.cos765.client;

import java.util.SortedSet;
import java.util.TreeSet;

import com.cos765.common.Segment;

public class ReceptionBuffer {

	private byte length = 0;
	private SortedSet<Segment> buffer = new TreeSet<Segment>();
	
	public byte getLength() {
		return length;
	}

	public void setLength(byte length) {
		this.length = length;
	}

	public boolean add(Segment segment) {
		if (length == 127)
			return false;
		buffer.add(segment);
		length++;
		return true;
	}

}
