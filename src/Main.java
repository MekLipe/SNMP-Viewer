import snmpserver.SNMPServer;
import snmpclient.SNMPClient;
import snmpclient.SNMPClientUI;

public class Main {
    public static void main(String[] args) {
        // Inicia o servidor SNMPv3 simulado em uma thread separada
        new Thread(() -> {
            SNMPServer servidor = new SNMPServer(8086);
            servidor.iniciar();
        }).start();

        // Aguarda um pouco e abre o cliente
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        SNMPClient cliente = new SNMPClient("http://localhost:8086/");
        new SNMPClientUI(cliente);
    }
}
