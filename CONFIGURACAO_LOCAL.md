# Configuração local do Agenday

O projeto usa Java 21. As senhas ficam fora do código e devem ser configuradas no ambiente em que a aplicação é iniciada.

## Eclipse Estudos

1. Abra **Run Configurations…** na configuração Spring Boot/Maven do Agenday.
2. Na aba **Environment**, adicione estas variáveis:

   | Nome | Valor |
   | --- | --- |
   | `DB_PASSWORD` | senha do usuário `postgres` no banco local `agenday` |
   | `AGENDAY_ADMIN_USERNAME` | usuário escolhido para a terapeuta |
   | `AGENDAY_ADMIN_PASSWORD` | senha forte escolhida para a terapeuta |

3. Salve a configuração e inicie novamente o projeto.
4. Acesse `http://localhost:8080/login` ou clique em **Acesso profissional** na página inicial.

Se qualquer variável de autenticação não estiver definida, a aplicação não inicia. Isso evita criar uma conta administrativa com senha padrão.

As credenciais são mantidas em memória e a senha é codificada com BCrypt ao iniciar a aplicação. Para esta primeira versão existe uma única conta administrativa; contas persistidas, recuperação de senha e gestão de usuários ficam para uma etapa posterior.
