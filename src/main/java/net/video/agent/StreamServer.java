package net.video.agent;

import java.awt.Dimension;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.github.sarxos.webcam.Webcam;
import main.DAO;


public class StreamServer {

	/**
	 * @author kerr
	 * @param args
	 */
	public static void main(String[] args) {
		Webcam.setAutoOpenMode(true);
		Webcam webcam = Webcam.getDefault();
		Dimension dimension = new Dimension(DAO.videoWidth, DAO.videoHeight);
		webcam.setViewSize(dimension);
		StreamServerAgent serverAgent = new StreamServerAgent(webcam, dimension);
		serverAgent.start(new InetSocketAddress("localhost", 20000));
		serverAgent.stop();
	}

}
