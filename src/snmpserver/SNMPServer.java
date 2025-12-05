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

    // Senha "12345" já hashada para comparação
    private final String senhaHash = gerarHash("12345");

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

            socket.setSoTimeout(2000);

            String linha;
            StringBuilder request = new StringBuilder();

            while ((linha = in.readLine()) != null) {
                if (linha.trim().isEmpty())
                    break;
                request.append(linha).append("\n");
            }

            String req = request.toString().trim();
            // System.out.println("Requisição: " + req); // Descomente para debug

            if (!req.startsWith("GET"))
                return;

            String primeiraLinha = req.split("\n")[0];
            int inicioParams = primeiraLinha.indexOf("/?");
            int fimParams = primeiraLinha.indexOf("HTTP/1.1");

            if (inicioParams == -1 || fimParams == -1) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\n");
                return;
            }

            String parametros = primeiraLinha.substring(inicioParams + 2, fimParams).trim();
            String[] params = parametros.split("&");

            String user = null, pass = null, oid = null;

            for (String p : params) {
                if (p.startsWith("user="))
                    user = p.substring(5);
                if (p.startsWith("pass="))
                    pass = p.substring(5);
                if (p.startsWith("oid="))
                    oid = p.substring(4);
            }

            // Validação alterada: Compara o hash recebido direto com o hash do banco
            if (user == null || pass == null || !user.equals(usuario) || !pass.equals(senhaHash)) {
                out.write("HTTP/1.1 401 Unauthorized\r\n\r\nAutenticacao Falhou");
                return;
            }

            mibData.atualizarDados();
            String valor = mibData.getValor(oid);
            String json = "{ \"oid\": \"" + oid + "\", \"valor\": \"" + valor + "\" }";

            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + json);
            out.flush();

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
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

    public static void main(String[] args) {
        new SNMPServer(16100).iniciar();
    }
}