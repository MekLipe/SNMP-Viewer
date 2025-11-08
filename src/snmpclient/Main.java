package snmpclient;

public class Main {
    public static void main(String[] args) {
        SNMPClient client = new SNMPClient("localhost", 16100, "admin", "12345");
        new SNMPClientUI(client);
    }
}
