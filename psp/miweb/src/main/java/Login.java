import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/login")
public class Login extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        String usuario = session != null ? (String) session.getAttribute("usuario") : null;

        out.println("<!DOCTYPE html>");
        out.println("<html lang='es'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>Login</title>");
        out.println("    <style>");
        out.println("        body {");
        out.println("            font-family: Arial, sans-serif;");
        out.println("            background-color: #f5f5f5;");
        out.println("            display: flex;");
        out.println("            justify-content: center;");
        out.println("            align-items: center;");
        out.println("            height: 100vh;");
        out.println("            margin: 0;");
        out.println("        }");
        out.println("        .login-container {");
        out.println("            background-color: white;");
        out.println("            padding: 40px;");
        out.println("            border-radius: 10px;");
        out.println("            box-shadow: 0 2px 10px rgba(0,0,0,0.1);");
        out.println("            width: 300px;");
        out.println("        }");
        out.println("        h1 {");
        out.println("            color: #333;");
        out.println("            text-align: center;");
        out.println("            margin-bottom: 30px;");
        out.println("        }");
        out.println("        .form-group {");
        out.println("            margin-bottom: 20px;");
        out.println("        }");
        out.println("        label {");
        out.println("            display: block;");
        out.println("            margin-bottom: 5px;");
        out.println("            color: #555;");
        out.println("            font-weight: bold;");
        out.println("        }");
        out.println("        input[type='text'], input[type='password'] {");
        out.println("            width: 100%;");
        out.println("            padding: 10px;");
        out.println("            border: 1px solid #ddd;");
        out.println("            border-radius: 5px;");
        out.println("            box-sizing: border-box;");
        out.println("            font-size: 14px;");
        out.println("        }");
        out.println("        button {");
        out.println("            width: 100%;");
        out.println("            padding: 12px;");
        out.println("            background-color: #007bff;");
        out.println("            color: white;");
        out.println("            border: none;");
        out.println("            border-radius: 5px;");
        out.println("            font-size: 16px;");
        out.println("            cursor: pointer;");
        out.println("            transition: background-color 0.3s;");
        out.println("        }");
        out.println("        button:hover {");
        out.println("            background-color: #0056b3;");
        out.println("        }");
        out.println("        .error {");
        out.println("            color: #dc3545;");
        out.println("            text-align: center;");
        out.println("            margin-bottom: 20px;");
        out.println("        }");
        out.println("        .success {");
        out.println("            color: #28a745;");
        out.println("            text-align: center;");
        out.println("        }");
        out.println("        .logout-btn {");
        out.println("            background-color: #dc3545;");
        out.println("            margin-top: 10px;");
        out.println("        }");
        out.println("        .logout-btn:hover {");
        out.println("            background-color: #c82333;");
        out.println("        }");
        out.println("        a {");
        out.println("            display: block;");
        out.println("            text-align: center;");
        out.println("            margin-top: 15px;");
        out.println("            color: #007bff;");
        out.println("            text-decoration: none;");
        out.println("        }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='login-container'>");

        if (usuario != null) {
            out.println("        <h1>Sesión Activa</h1>");
            out.println("        <p class='success'>¡Bienvenido, " + usuario + "!</p>");
            out.println("        <p>Ya has iniciado sesión correctamente.</p>");
            out.println("        <form method='post' action='login'>");
            out.println("            <input type='hidden' name='action' value='logout'>");
            out.println("            <button type='submit' class='logout-btn'>Cerrar Sesión</button>");
            out.println("        </form>");
            out.println("        <a href='index.html'>Volver al inicio</a>");
        } else {
            out.println("        <h1>Iniciar Sesión</h1>");

            String error = request.getParameter("error");
            if (error != null) {
                out.println("        <p class='error'>Usuario o contraseña incorrectos</p>");
            }

            out.println("        <form method='post' action='login'>");
            out.println("            <div class='form-group'>");
            out.println("                <label for='usuario'>Usuario:</label>");
            out.println("                <input type='text' id='usuario' name='usuario' required>");
            out.println("            </div>");
            out.println("            <div class='form-group'>");
            out.println("                <label for='password'>Contraseña:</label>");
            out.println("                <input type='password' id='password' name='password' required>");
            out.println("            </div>");
            out.println("            <button type='submit'>Ingresar</button>");
            out.println("        </form>");
            out.println("        <a href='index.html'>Volver al inicio</a>");
        }

        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            // Cerrar sesión
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("login");
            return;
        }

        // Procesar login
        String usuario = request.getParameter("usuario");
        String password = request.getParameter("password");

        // Validar credenciales: solo admin/admin es válido
        if ("admin".equals(usuario) && "admin".equals(password)) {
            // Login exitoso - crear sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setAttribute("loginTime", System.currentTimeMillis());

            // Redirigir a página de éxito
            response.sendRedirect("login");
        } else {
            // Login fallido - redirigir con error
            response.sendRedirect("login?error=true");
        }
    }

}
