package snmpclient;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;

public class SNMPClient {

    private final String servidor;
    private final int porta;

    public SNMPClient(String servidor, int porta) {
        this.servidor = servidor;
        this.porta = porta;
    }

    // Agora recebe usuário e senha na hora da chamada
    public String get(String oid, String usuario, String senha) throws IOException {

        // Criptografa a senha aqui no cliente antes de enviar
        String senhaHash = gerarHash(senha);

        try (Socket socket = new Socket(servidor, porta);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Monta a requisição enviando o Hash em vez da senha pura
            String request = "GET /?user=" + usuario + "&pass=" + senhaHash + "&oid=" + oid + " HTTP/1.1\r\n" +
                    "Host: " + servidor + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(request);
            out.flush();

            StringBuilder response = new StringBuilder();
            String linha;
            while ((linha = in.readLine()) != null) {
                response.append(linha).append("\n");
            }

            return response.toString();
        }
    }

    private String gerarHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }
}