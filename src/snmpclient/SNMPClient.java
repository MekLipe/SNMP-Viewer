package snmpclient;

import java.io.*;
import java.net.Socket;

public class SNMPClient {

    private final String servidor;
    private final int porta;
    private final String usuario;
    private final String senha;

    // ✅ Construtor que o UI chama
    public SNMPClient(String servidor, int porta, String usuario, String senha) {
        this.servidor = servidor;
        this.porta = porta;
        this.usuario = usuario;
        this.senha = senha;
    }

    // ✅ Método que envia o OID e recebe a resposta
    public String get(String oid) throws IOException {
        try (Socket socket = new Socket(servidor, porta);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Requisição HTTP completa — incluindo cabeçalho e linha vazia no fim
            String request = "GET /?user=" + usuario + "&pass=" + senha + "&oid=" + oid + " HTTP/1.1\r\n" +
                    "Host: " + servidor + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(request);
            out.flush();

            // ✅ Recebe a resposta do servidor
            StringBuilder response = new StringBuilder();
            String linha;
            while ((linha = in.readLine()) != null) {
                response.append(linha).append("\n");
            }

            return response.toString();
        }
    }
}
