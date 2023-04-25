package org.example;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {
        String host = "47.120.34.88";
        int port = 9008;
        String password = "foobared";

        try (Jedis jedis = new Jedis(host, port)) {
            jedis.auth(password);
            String response = jedis.ping();
            System.out.println("Server response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}