import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpCookie;

public class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Invalidate the session by setting an expired cookie
            HttpCookie cookie = new HttpCookie("user_matricule", "");
            cookie.setMaxAge(0); // Expire the cookie immediately
            exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());

            // Redirect to login page
            exchange.getResponseHeaders().add("Location", "/index.html");
            exchange.sendResponseHeaders(302, -1); // 302 Found for redirection
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}
