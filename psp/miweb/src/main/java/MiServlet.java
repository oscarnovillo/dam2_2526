import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/mi")
public class MiServlet extends HttpServlet  {


    // no se hace, caca
    private int i = 0;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doTusCosas(req, resp);


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doTusCosas(req, resp);

    }

    private void doTusCosas(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var contador = 1;
        if (req.getSession().getAttribute(Constantes.CONTADOR) != null) {
            contador = (Integer) req.getSession().getAttribute(Constantes.CONTADOR);
        }

        String param = req.getParameter("nombre");
        resp.getWriter().write("<html><h1>"+contador+" "+param+"</h1>" +

                "<form action='mi' method='post' style='margin-top: 30px; text-align: center;'>"+
                "<input type='text' name='nombre' id='nombre' value=''  />" +
                "<button type='submit' style='padding: 10px 20px; font-size: 16px; border: none; border-radius: 5px; background-color: #28a745; color: white; cursor: pointer;'>"+
                "Ir a mi"+
                "</button>"+
                "</form>"+

                "</html>");

        req.getSession().setAttribute(Constantes.CONTADOR, contador+1);
    }
}
