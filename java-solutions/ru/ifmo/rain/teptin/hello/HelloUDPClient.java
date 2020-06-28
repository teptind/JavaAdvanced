package ru.ifmo.rain.teptin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.ifmo.rain.teptin.hello.HelloUtils.closeExecutor;
import static ru.ifmo.rain.teptin.hello.HelloUtils.getMessage;

//  java -cp . -p . -m info.kgeorgiy.java.advanced.hello client ru.ifmo.rain.teptin.hello.HelloUDPClient

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress address;
        try {
            address = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            System.err.println(String.format("Host \"%s\" is unavailable", host));
            return;
        }
        ExecutorService sender = Executors.newFixedThreadPool(threads);
        for (int threadNumber = 0; threadNumber < threads; ++threadNumber) {
            sender.submit(getSendingTask(address, threadNumber, prefix, requests));
        }
        closeExecutor(sender, 5000);
    }

    private Runnable getSendingTask(SocketAddress address, int threadNumber, String prefix, int requests) {
        return () -> {
            try(DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(150);
                for (int requestNumber = 0; requestNumber < requests; ++requestNumber) {
                    byte[] bytesMsg =  String.format("%s%d_%d",prefix, threadNumber, requestNumber)
                            .getBytes(StandardCharsets.UTF_8);
                    DatagramPacket request = new DatagramPacket(bytesMsg, bytesMsg.length, address);
                    pressForResponse(request, socket);
                }
            } catch (SocketException e) {
                System.err.println(String.format("There is an error creating or accessing UDP socket, address: %s", address));
            }
        };
    }

    private void pressForResponse(DatagramPacket request, DatagramSocket socket) throws SocketException {
        int responseSize = socket.getReceiveBufferSize();
        DatagramPacket response = new DatagramPacket(new byte[responseSize], responseSize);
        while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                socket.send(request);
                socket.receive(response);
                String sentMsg = getMessage(request);
                String receivedMsg = getMessage(response);
                if (receivedMsg.contains(sentMsg)) {
                    System.out.println(receivedMsg);
                    return;
                }
            } catch (IOException e) {   // TODO: messages?
//                System.err.println(String.format("An error occurred while sending the request: %s", e.getMessage()));
            }
        }
    }
}
