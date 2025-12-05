package snmpclient;

public class Main {
    public static void main(String[] args) {
        SNMPClient client = new SNMPClient("::1", 16100);
        new SNMPClientUI(client);
    }
}
