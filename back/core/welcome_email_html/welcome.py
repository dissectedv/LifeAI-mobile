from LIFEAI_ import settings


def _send_welcome_email_html(user):
    subject = "Bem-vindo ao LifeAI!"
    from_email = getattr(settings, 'DEFAULT_FROM_EMAIL', settings.EMAIL_HOST_USER)
    to_email = [user.email]

    text_content = f"""
        Olá, {user.username}!
        Seja muito bem-vindo ao LifeAI, sua nova plataforma de bem-estar e inteligência personalizada.
        Sua conta foi criada com sucesso e agora você pode aproveitar recursos exclusivos para melhorar sua saúde física e mental.
        Atenciosamente,
        Equipe LifeAI
    """

    html_content = f"""
    <html lang="pt-BR">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body {{
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                margin: 0;
                padding: 0;
                background-color: #f4f7f6;
                color: #333;
            }}
            .container {{
                width: 90%;
                max-width: 600px;
                margin: 20px auto;
                background-color: #ffffff;
                border-radius: 12px;
                overflow: hidden;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            }}
            .header {{
                background: linear-gradient(90deg, #007BFF, #6C63FF);
                padding: 30px 20px;
                text-align: center;
            }}
            .header h1 {{
                margin: 0;
                color: #ffffff;
                font-size: 28px;
                font-weight: 700;
            }}
            .content {{
                padding: 35px 30px;
                line-height: 1.6;
            }}
            .content p {{
                font-size: 16px;
                margin-bottom: 20px;
            }}
            .footer {{
                padding: 20px 30px;
                background-color: #f9f9f9;
                text-align: center;
                font-size: 12px;
                color: #999;
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>Bem-vindo ao LifeAI!</h1>
            </div>
            <div class="content">
                <p>Olá, <strong>{user.username}</strong>!</p>
                <p>Seja muito bem-vindo ao <strong>LifeAI</strong>, sua nova plataforma de bem-estar e inteligência personalizada.</p>
                <p>Sua conta foi criada com sucesso e agora você pode aproveitar recursos exclusivos para melhorar sua saúde física e mental.</p>
                <p>Estamos felizes em ter você conosco.</p>
                <p>Atenciosamente,<br>Equipe LifeAI</p>
            </div>
            <div class="footer">
                <p>&copy; 2025 LifeAI. Todos os direitos reservados.</p>
            </div>
        </div>
    </body>
    </html>
    """

    msg = EmailMultiAlternatives(subject, text_content, from_email, to_email)
    msg.attach_alternative(html_content, "text/html")
    msg.send(fail_silently=False)