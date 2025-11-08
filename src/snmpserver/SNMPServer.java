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

            socket.setSoTimeout(2000); // evita travamento

            String linha;
            StringBuilder request = new StringBuilder();

            // ✅ Lê até linha vazia (fim do cabeçalho)
            while ((linha = in.readLine()) != null) {
                if (linha.trim().isEmpty()) break;
                request.append(linha).append("\n");
            }

            String req = request.toString().trim();
            System.out.println("\n📩 Requisição recebida:\n" + req);

            if (!req.startsWith("GET")) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\n");
                return;
            }

            // ✅ Pega apenas a primeira linha do GET
            String primeiraLinha = req.split("\n")[0];

            // Exemplo: "GET /?user=admin&pass=12345&oid=1.3.6.1.2.1.1.3.0 HTTP/1.1"
            int inicioParams = primeiraLinha.indexOf("/?");
            int fimParams = primeiraLinha.indexOf("HTTP/1.1");

            if (inicioParams == -1 || fimParams == -1) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\nFormato inválido de requisição");
                return;
            }

            String parametros = primeiraLinha.substring(inicioParams + 2, fimParams).trim();
            String[] params = parametros.split("&");

            String user = null, pass = null, oid = null;

            for (String p : params) {
                if (p.startsWith("user=")) user = p.substring(5);
                if (p.startsWith("pass=")) pass = p.substring(5);
                if (p.startsWith("oid=")) oid = p.substring(4);
            }

            if (user == null || pass == null || oid == null) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\nParâmetros inválidos");
                return;
            }

            if (!user.equals(usuario) || !gerarHash(pass).equals(senhaHash)) {
                out.write("HTTP/1.1 401 Unauthorized\r\n\r\nFalha na autenticação SNMPv3");
                return;
            }

            // ✅ Responde corretamente
            mibData.atualizarDados();
            String valor = mibData.getValor(oid);
            String json = "{ \"oid\": \"" + oid + "\", \"valor\": \"" + valor + "\" }";

            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + json);
            out.flush();

            System.out.println("✅ Resposta enviada: " + json);

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar requisição: " + e.getMessage());
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

    // 🚀 Método main para rodar o servidor
    public static void main(String[] args) {
        SNMPServer servidor = new SNMPServer(16100); // mesma porta usada no cliente
        servidor.iniciar();
    }
}
