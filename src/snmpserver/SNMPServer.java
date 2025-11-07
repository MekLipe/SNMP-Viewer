package snmpserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;

public class SNMPServer {
    private final int porta;
    private final MIBData mibData;
    private final String usuario = "admin";
    private final String senhaHash = gerarHash("12345"); // senha simulada

    public SNMPServer(int porta) {
        this.porta = porta;
        this.mibData = new MIBData();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor SNMPv3 ouvindo na porta " + porta);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> processarRequisicao(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processarRequisicao(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String linha;
            StringBuilder request = new StringBuilder();
            while ((linha = in.readLine()) != null && !linha.isEmpty()) {
                request.append(linha).append("\n");
            }

            String corpo = in.readLine();
            if (corpo != null) request.append(corpo);

            String req = request.toString();
            if (!req.contains("GET")) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\n");
                return;
            }

            // Extrair parâmetros (usuario, senha, oid)
            String[] partes = req.split("\\?");
            if (partes.length < 2) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\nFaltam parâmetros");
                return;
            }

            String[] params = partes[1].split("&");
            String user = null, pass = null, oid = null;

            for (String p : params) {
                if (p.startsWith("user=")) user = p.substring(5);
                if (p.startsWith("pass=")) pass = p.substring(5);
                if (p.startsWith("oid=")) oid = p.substring(4).replace(" HTTP/1.1", "");
            }

            if (user == null || pass == null || oid == null) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\nParâmetros inválidos");
                return;
            }

            if (!user.equals(usuario) || !gerarHash(pass).equals(senhaHash)) {
                out.write("HTTP/1.1 401 Unauthorized\r\n\r\nFalha na autenticação SNMPv3");
                return;
            }

            mibData.atualizarDados();
            String valor = mibData.getValor(oid);
            String json = "{ \"oid\": \"" + oid + "\", \"valor\": \"" + valor + "\" }";

            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String gerarHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
