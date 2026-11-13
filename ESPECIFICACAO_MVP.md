# AGENDAY — Documento de Levantamento de Requisitos (MVP)
> **Sistema Web de Agendamento para Massoterapia**

---

## 1. Objetivo do Projeto
Desenvolver um sistema web responsivo para agendamento de sessões de massoterapia, permitindo que clientes realizem seus próprios agendamentos através de um link compartilhado pela profissional, reduzindo a necessidade de atendimento manual via WhatsApp e melhorando a organização da agenda.

O sistema deverá funcionar em computadores, tablets e celulares, priorizando a experiência mobile (*mobile-first*).

---

## 2. Usuários do Sistema

### 2.1 Cliente
Pessoa interessada em contratar uma sessão de massagem.
* **Responsabilidades:**
  * Visualizar serviços disponíveis e seus valores.
  * Consultar horários livres gerados pelo sistema.
  * Realizar agendamentos informando nome e telefone.
  * Receber confirmação na tela e enviar notificação via WhatsApp para a profissional.
  * Identificar-se no sistema utilizando o próprio número de telefone.

### 2.2 Massoterapeuta
Profissional responsável pelos atendimentos.
* **Responsabilidades:**
  * Visualizar agenda diária e semanal.
  * Cancelar agendamentos quando necessário.
  * Bloquear horários indisponíveis na agenda (pausas, compromissos pessoais, férias).
  * Gerenciar os serviços oferecidos (valores, descrições e durações).

---

## 3. Requisitos Funcionais

* **RF01 – Consultar Serviços:** O sistema deverá permitir que o cliente visualize os serviços disponíveis.
* **RF02 – Consultar Horários Disponíveis:** O sistema deverá exibir apenas horários livres para agendamento, calculados dinamicamente.
* **RF03 – Realizar Agendamento:** O cliente deverá informar:
  * Nome
  * Telefone / WhatsApp (chave única de identificação)
  * Serviço desejado
  * Data
  * Horário
* **RF04 – Confirmar Agendamento:** Após o cadastro, o sistema deverá confirmar a reserva do horário.
* **RF05 – Visualizar Agenda:** A massoterapeuta deverá visualizar todos os agendamentos cadastrados.
* **RF06 – Cancelar Agendamento:** A massoterapeuta deverá conseguir cancelar um agendamento.
* **RF07 – Bloquear Horários:** A massoterapeuta poderá bloquear horários específicos para pausas, férias ou indisponibilidade, removendo-os da grade de horários livres.
* **RF08 – Impedir Conflitos e Intervalo Técnico:** O sistema não deverá permitir dois agendamentos com sobreposição de horários. A checagem considera o horário de início + duração do serviço + 15 minutos de intervalo técnico entre atendimentos (para higienização da sala e maca).
* **RF09 – Identificação Única por Telefone:** O sistema deverá utilizar o telefone celular como chave única do cliente, reaproveitando o cadastro existente e evitando duplicidades.
* **RF10 – Notificação via WhatsApp:** Ao concluir o agendamento, a tela de sucesso deverá disponibilizar um botão para envio direto de mensagem no WhatsApp da massoterapeuta com os detalhes pré-formatados.

---

## 4. Requisitos Não Funcionais

* **RNF01 – Responsividade:** O sistema deverá funcionar perfeitamente em dispositivos móveis (*mobile-first*).
* **RNF02 – Facilidade de Uso:** O processo de agendamento deverá ser concluído em menos de 30 segundos, em poucos passos intuitivos.
* **RNF03 – Disponibilidade:** O sistema deverá estar disponível 24 horas por dia.
* **RNF04 – Segurança:** Os dados dos clientes deverão ser armazenados de forma íntegra e segura.
* **RNF05 – Performance:** As páginas deverão carregar em até 3 segundos em condições normais de navegação móvel.

---

## 5. Casos de Uso

### UC01 – Agendar Sessão
* **Ator:** Cliente
* **Fluxo Principal:**
  1. Cliente acessa o link de agendamento compartilhado.
  2. Sistema exibe os serviços disponíveis.
  3. Cliente seleciona o serviço desejado.
  4. Sistema exibe o calendário com as datas disponíveis.
  5. Cliente seleciona uma data.
  6. Sistema calcula e exibe os horários disponíveis (considerando expediente, duração e bloqueios).
  7. Cliente seleciona o horário.
  8. Cliente informa nome e telefone celular.
  9. Sistema valida a disponibilidade final e registra o agendamento.
  10. Sistema exibe a mensagem de confirmação do agendamento.
  11. Sistema disponibiliza botão para envio imediato de mensagem ao WhatsApp da massoterapeuta com texto pré-preenchido.

### UC02 – Visualizar Agenda
* **Ator:** Massoterapeuta
* **Fluxo Principal:**
  1. Acessa área administrativa.
  2. Sistema exibe os agendamentos do dia em ordem cronológica.
  3. Sistema permite alternar para visão semanal ou datas futuras.

### UC03 – Cancelar Agendamento
* **Ator:** Massoterapeuta
* **Fluxo Principal:**
  1. Seleciona o agendamento na agenda.
  2. Solicita o cancelamento informando motivo opcional.
  3. Sistema altera o status do agendamento para `CANCELADO`.
  4. O horário volta a ficar livre para novos agendamentos de clientes.

### UC04 – Bloquear Horário na Agenda
* **Ator:** Massoterapeuta
* **Fluxo Principal:**
  1. Acessa área administrativa.
  2. Seleciona a opção de bloquear horário.
  3. Informa data, horário de início, horário de término e motivo do bloqueio.
  4. Sistema registra o bloqueio com status `BLOQUEADO`.
  5. Os horários contidos no intervalo são imediatamente retirados da disponibilidade pública.

---

## 6. Modelagem Inicial das Entidades

### Cliente
* `id` (BIGINT PK)
* `nome` (VARCHAR 100)
* `telefone` (VARCHAR 20 UNIQUE)

### Serviço
* `id` (BIGINT PK)
* `nome` (VARCHAR 100)
* `descricao` (VARCHAR 500)
* `duracao_minutos` (INTEGER)
* `valor` (DECIMAL 10,2)

### Agendamento
* `id` (BIGINT PK)
* `cliente_id` (BIGINT FK, NULL em bloqueios)
* `servico_id` (BIGINT FK, NULL em bloqueios)
* `data` (DATE)
* `horario` (TIME)
* `status` (VARCHAR 20: `AGENDADO`, `CANCELADO`, `CONCLUIDO`, `BLOQUEADO`)
* `observacao` (VARCHAR 500)

---

## 7. Protótipos das Telas (Fluxo do Cliente)

### Tela Inicial
```text
Agenda Massagem
[ Agendar Sessão ]
```

### Tela de Seleção de Serviço
```text
Escolha um Serviço:
(o) Massagem Relaxante (60 min) - R$ 120,00
( ) Massagem Terapêutica (90 min) - R$ 160,00
( ) Drenagem Linfática (60 min) - R$ 130,00
[ Continuar ]
```

### Tela de Seleção de Data
```text
Escolha uma Data:
[ 20/06/2026 ] [ 21/06/2026 ] [ 22/06/2026 ]
```

### Tela de Seleção de Horário
```text
Horários Disponíveis:
[ 09:00 ] [ 10:30 ] [ 14:00 ] [ 15:30 ]
```

### Tela de Confirmação
```text
Nome: [ Maria Silva               ]
WhatsApp: [ (11) 99999-8888       ]
Resumo: Massagem Relaxante | 20/06 às 09:00
[ Confirmar Agendamento ]
```

### Tela de Sucesso
```text
✅ Agendamento realizado com sucesso!
[ Enviar Confirmação no WhatsApp ]
(Abre o WhatsApp da profissional com a mensagem: "Olá! Confirmei meu agendamento de Massagem Relaxante para 20/06 às 09:00.")
```

---

## 8. Tecnologias Utilizadas

* **Backend:** Java 17, Spring Boot 3
* **Frontend:** Thymeleaf, Bootstrap 5 (HTML5 / CSS3)
* **Persistência:** Spring Data JPA, Hibernate
* **Banco de Dados:** PostgreSQL
* **Controle de Versão:** Git / GitHub
* **Hospedagem:** Render / Neon PostgreSQL (Inicial) → VPS Hostinger / Nginx (Futuro)

---

## 9. Regras de Negócio

* **RN01 – Conflito de Horários e Duração:** Não permitir dois agendamentos com sobreposição de horários. A validação considera `horario_inicio` até o término (`horario + duracao_minutos`), acrescido de **15 minutos de intervalo técnico** para higienização e organização da sala.
* **RN02 – Exibição de Horários Livres:** Apenas horários estritamente livres poderão ser apresentados para seleção.
* **RN03 – Liberação de Cancelados:** Horários com agendamentos cancelados voltam a ser elegíveis para cálculo da grade.
* **RN04 – Duração Específica:** Cada serviço possui sua própria duração que impacta o cálculo dos slots disponíveis.
* **RN05 – Validação de Concorrência:** Ao submeter a confirmação, o sistema revalida a disponibilidade para evitar colisões simultâneas.
* **RN06 – Histórico:** Agendamentos concluídos ou cancelados permanecem arquivados no banco de dados para histórico.
* **RN07 – Grade Padrão de Atendimento:** Os horários do sistema respeitam a jornada semanal cadastrada da massoterapeuta (ex.: Seg–Sex das 08:00 às 18:00 e Sáb das 08:00 às 13:00, com almoço das 12:00 às 13:30).
* **RN08 – Unicidade pelo Telefone:** O telefone do cliente é único. Em novos agendamentos, o cadastro existente é reutilizado e o histórico de sessões fica unificado.
* **RN09 – Bloqueio de Horários:** Bloqueios administrativos efetuados pela profissional ocupam o período na agenda sob status `BLOQUEADO`.

---

## 10. Diagrama Entidade Relacionamento (DER)

### CLIENTE
| Campo | Tipo | Observação |
| :--- | :--- | :--- |
| `id` | BIGINT | Chave Primária (PK) |
| `nome` | VARCHAR(100) | Nome do cliente |
| `telefone` | VARCHAR(20) | Telefone para contato e chave única (UNIQUE) |

### SERVICO
| Campo | Tipo | Observação |
| :--- | :--- | :--- |
| `id` | BIGINT | Chave Primária (PK) |
| `nome` | VARCHAR(100) | Nome do serviço |
| `descricao` | VARCHAR(500) | Descrição do serviço |
| `duracao_minutos`| INTEGER | Duração do atendimento |
| `valor` | DECIMAL(10,2)| Valor do serviço |

### AGENDAMENTO
| Campo | Tipo | Observação |
| :--- | :--- | :--- |
| `id` | BIGINT | Chave Primária (PK) |
| `cliente_id` | BIGINT | FK para CLIENTE (Permite NULL se status for BLOQUEADO) |
| `servico_id` | BIGINT | FK para SERVICO (Permite NULL se status for BLOQUEADO) |
| `data` | DATE | Data do atendimento |
| `horario` | TIME | Horário de início do atendimento ou bloqueio |
| `status` | VARCHAR(20) | `AGENDADO`, `CANCELADO`, `CONCLUIDO` ou `BLOQUEADO` |
| `observacao` | VARCHAR(500) | Observações adicionais ou motivo do cancelamento/bloqueio |

---

## 11. Modelo Relacional
```sql
CLIENTE (
    id PK,
    nome,
    telefone UNIQUE
);

SERVICO (
    id PK,
    nome,
    descricao,
    duracao_minutos,
    valor
);

AGENDAMENTO (
    id PK,
    cliente_id FK (NULL se bloqueio),
    servico_id FK (NULL se bloqueio),
    data,
    horario,
    status,
    observacao
);
```

criei o banco com o nome agenday, a porta é 5432 o usuário é o postgres e a senha é uma minha especifica
