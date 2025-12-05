package org.example.spring.ui.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailActivacion(String destinatario, String nombreUsuario, String codigoActivacion) {

        try
        {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Activación de cuenta - Sistema");
            helper.setText(construirMensajeActivacion(nombreUsuario, codigoActivacion), true);
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}", destinatario, e);
            System.err.println("Error al enviar correo: " + e.getMessage());
            // En desarrollo, mostramos el código en consola
            System.out.println("=================================================");
            System.out.println("CÓDIGO DE ACTIVACIÓN PARA: " + destinatario);
            System.out.println("Código: " + codigoActivacion);
            System.out.println("=================================================");
        }
    }

    private String construirMensajeActivacion(String nombreUsuario, String codigoActivacion) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f4f4f4;
                        }
                        .container {
                            background-color: #ffffff;
                            border-radius: 10px;
                            padding: 40px;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align: center;
                            color: #4CAF50;
                            margin-bottom: 30px;
                            font-size: 28px;
                        }
                        .content {
                            font-size: 16px;
                        }
                        .code-box {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            font-size: 28px;
                            font-weight: bold;
                            text-align: center;
                            padding: 25px;
                            border-radius: 8px;
                            margin: 30px 0;
                            letter-spacing: 4px;
                            font-family: 'Courier New', monospace;
                            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                        }
                        .button {
                            display: inline-block;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white !important;
                            padding: 15px 40px;
                            text-decoration: none;
                            border-radius: 25px;
                            margin: 20px 0;
                            text-align: center;
                            font-weight: bold;
                            font-size: 16px;
                            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                            transition: transform 0.2s;
                        }
                        .button:hover {
                            transform: translateY(-2px);
                        }
                        .alert-box {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .footer {
                            margin-top: 40px;
                            padding-top: 20px;
                            border-top: 1px solid #e0e0e0;
                            font-size: 13px;
                            color: #666;
                            text-align: center;
                        }
                        .emoji {
                            font-size: 24px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1 class="header">
                            <span class="emoji">🎉</span> ¡Bienvenido a nuestro Sistema! <span class="emoji">🎉</span>
                        </h1>
                        
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            
                            <p>¡Gracias por registrarte! Estamos emocionados de tenerte con nosotros.</p>
                            
                            <p>Para activar tu cuenta, usa el siguiente código de activación:</p>
                            
                            <div class="code-box">%s</div>
                            
                            <p style="text-align: center;">O simplemente haz clic en el botón de abajo:</p>
                            
                            <div style="text-align: center;">
                                <a href="http://localhost:8080/auth/activar?codigo=%s" class="button">
                                    ✅ Activar Mi Cuenta
                                </a>
                            </div>
                            
                            <div class="alert-box">
                                <strong>⚠️ Importante:</strong> Este código expirará en <strong>24 horas</strong>.
                            </div>
                            
                            <p>Una vez activada tu cuenta, podrás acceder a todas las funcionalidades del sistema.</p>
                        </div>
                        
                        <div class="footer">
                            <p>Si no has solicitado este registro, por favor ignora este correo.</p>
                            <p style="margin-top: 15px;">
                                <strong>Saludos cordiales,</strong><br>
                                El equipo de Sistema 💼
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombreUsuario, codigoActivacion, codigoActivacion);
    }
}

