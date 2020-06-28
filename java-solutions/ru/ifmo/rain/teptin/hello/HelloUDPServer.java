package ru.ifmo.rain.teptin.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.rain.teptin.hello.HelloUtils.closeExecutor;
import static ru.ifmo.rain.teptin.hello.HelloUtils.getMessage;

//  java -cp . -p . -m info.kgeorgiy.java.advanced.hello server ru.ifmo.rain.teptin.hello.HelloUDPServer

public class HelloUDPServer implements HelloServer {

    private DatagramSocket socket;
    private ExecutorService mainExecutor;
    private ExecutorService receiver;

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            mainExecutor = Executors.newSingleThreadExecutor();
            receiver = Executors.newFixedThreadPool(threads);
            mainExecutor.submit(getRequestReceivingTask(socket, receiver));
        } catch (SocketException e) {
            System.err.println("Socket exception occurred while starting the server: " + e.getMessage());
        }
    }

    private Runnable getRequestReceivingTask(DatagramSocket socket, ExecutorService receiver) {
        return () -> {
            try {
                while (!socket.isClosed()) {
                    DatagramPacket request = new DatagramPacket(new byte[socket.getReceiveBufferSize()],
                                                                socket.getReceiveBufferSize());
                    socket.receive(request);
                    receiver.submit(getRespondingTask(socket, request));
                }
            } catch (RejectedExecutionException ignored) {
                System.err.println("All executors are busy now, please wait for maintaining your request");
            } catch (SocketException ignored) {
                System.err.println(String.format("Unable to create or access a UDP socket with host %s", socket.getInetAddress().getHostName()));
            } catch (IOException e) {
                System.err.println("An error occurred while receiving the response " + e.getMessage());
            }
        };
    }

    private Runnable getRespondingTask(DatagramSocket socket, DatagramPacket request) {
        return () -> {
            byte[] bytesMessage = (HelloUtils.ANS_PREF
                    + getMessage(request))
                    .getBytes(StandardCharsets.UTF_8);
            try {
                socket.send(new DatagramPacket(bytesMessage, bytesMessage.length, request.getSocketAddress()));
            } catch (IOException e) {
                System.err.println("An error occurred while sending the response " + e.getMessage());
            }
        };
    }

    @Override
    public void close() {
        closeExecutor(mainExecutor, 300);
        closeExecutor(receiver, 300);
        socket.close();
    }
}
