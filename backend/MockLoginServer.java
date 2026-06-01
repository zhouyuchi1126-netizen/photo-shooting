import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MockLoginServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Login endpoint
        server.createContext("/api/auth/login", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                    
                    byte[] body = exchange.getRequestBody().readAllBytes();
                    String requestBody = new String(body, StandardCharsets.UTF_8);
                    
                    // Simple login logic
                    String responseJson;
                    if (requestBody.contains("admin") && requestBody.contains("123456")) {
                        responseJson = "{\"success\":true,\"displayName\":\"管理员\",\"token\":\"dummy-token\",\"message\":\"登录成功\"}";
                    } else {
                        responseJson = "{\"success\":false,\"message\":\"用户名或密码错误\"}";
                    }
                    
                    byte[] responseBytes = responseJson.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                }
            }
        });
        
        server.setExecutor(null);
        server.start();
        System.out.println("Mock Login Server running on http://localhost:8080");
    }
}
