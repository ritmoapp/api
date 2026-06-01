# 🧍‍♂️ USER STORY PRINCIPAL (Visão do Usuário)

### **US0 — Como usuário iniciante ou intermediário de corrida, quero um aplicativo que entenda meu objetivo e meu tempo disponível, para que eu receba um plano de treino simples, claro e adaptado à minha rotina.**

---

# 🟦 **USER STORIES DETALHADAS — FASE 1**

A seguir, as user stories divididas pelos tópicos principais do app.

---

# 🟦 1. **User Stories — Cadastro e Onboarding**

### **US1 — Criar Conta**

**Como** usuário,**quero** criar uma conta usando e-mail e senha,

**para** acessar os meus treinos e dados em qualquer dispositivo.

**Critérios de Aceitação**

- O usuário deve poder se registrar com e-mail e senha válidos.
- Senha mínima de 6 caracteres.
- Após registrar, usuário deve logar automaticamente ou ser direcionado ao login.

---

### **US2 — Login**

**Como** usuário, **quero** fazer login com minhas credenciais, **para** acessar apenas meus dados.

**Critérios de Aceitação**

- Login via email + senha.
- Token JWT armazenado no app.
- Mostra erro caso email ou senha estejam incorretos.

---

### **US3 — Onboarding Básico**

**Como** usuário,

**quero** informar meus dados iniciais (idade, peso, altura — opcional),

**para** que o app personalize levemente o meu plano.

---

### **US4 — Objetivo Principal**

**Como** usuário, **quero** informar meu objetivo principal (ex: correr 5km, perder peso, completar uma prova, treinar 3x por semana), **para** que o app gere um plano baseado nisso.

**Critérios de Aceitação**

- O usuário deve selecionar: distância alvo OU prova específica.
- O usuário informa pace médio atual.
- O usuário informa quantos dias pode treinar na semana (3 a 6).

---

### **US5 — Disponibilidade**

**Como** usuário,

**quero** informar meus dias e horários disponíveis,

**para** que o plano encaixe na minha rotina.

---

# 🟦 2. **User Stories — Plano de Treino**

### **US6 — Gerar Plano de Treino Inicial**

**Como** usuário, **quero** receber um plano semanal baseado nos meus objetivos e disponibilidade,

**para** saber exatamente o que treinar a cada dia.

**Critérios de Aceitação**

- Plano deve ser gerado com base em:
    - objetivo
    - pace atual
    - frequência semanal
    - disponibilidade
- Cada treino deve ter:
    - tipo (leve, intervalado, tempo run, longão)
    - distância alvo
    - ritmo recomendado
    - descrição simples

**Como o app deve montar:**

- Backend retorna um JSON com:

```json
{
  "week": 1,
  "sessions": [
    { "day": "segunda", "type": "leve", "distance": 5, "pace": "6:00" },
    { "day": "quarta", "type": "intervalado", "distance": 6 },
    { "day": "sábado", "type": "longão", "distance": 10 }
  ]
}

```

- App exibe de forma clara em cards.

---

### **US7 — Ver Plano da Semana**

**Como** usuário, **quero** visualizar meu plano organizado por dias, **para** entender minha rotina semanal de treinos.

**Critérios de Aceitação**

- App exibe os treinos do dia/semana.
- Treinos futuros não editáveis.
- Treinos passados podem ser marcados como completos.

---

# 🟩 3. **User Stories — Registro de Treino**

### **US8 — Registrar Treino Manualmente**

**Como** usuário, **quero** registrar um treino manualmente, **para** manter a evolução mesmo se correr sem GPS ou outro app.

**Campos:**

- distância
- tempo
- pace calculado
- percepção de esforço (0–10)
- observações

---

### **US9 — Ver Evolução**

**Como** usuário,**quero** ver minha evolução semanal e mensal, **para** saber se estou melhorando.

**Critérios de Aceitação**

- Ver km por semana
- Ver pace médio por semana
- Ver média da percepção de esforço
- Exibir tendências simples

---

# 🟩 4. **User Stories — Integração Strava**

### **US10 — Integrar Strava**

**Como** usuário,

**quero** conectar minha conta Strava,

**para** importar meus treinos automaticamente.

---

### **US11 — Importar Treinos**

**Como** usuário,

**quero** que corridas feitas no Strava apareçam automaticamente no app,

**para** manter tudo sincronizado sem esforço.

---

# 🟧 5. **User Stories — Social Básico**

### **US12 — Criar Grupo**

**Como** usuário,

**quero** criar um grupo simples com amigos,

**para** treinarmos juntos e ver a evolução de todos.

---

### **US13 — Entrar em Grupo**

**Como** usuário,

**quero** buscar grupos existentes e entrar,

**para** acompanhar pessoas com objetivos similares.

---

### **US14 — Feed de Evolução**

**Como** usuário,

**quero** ver os treinos mais recentes das pessoas do grupo,

**para** ter motivação e acompanhar progresso.

---

# 🟦 **User Story Central: O QUE O USUÁRIO QUER VER NA FASE 1?**

### **Como usuário, quero que o app me mostre:**

- Meu plano de treino da semana
- O treino do dia com explicação simples
- Minha evolução (km, pace, percepção)
- Treinos recentes realizados
- Minha lista de objetivos
- Opção de registrar treino rápido
- Se estou seguindo o plano corretamente
- Motivação: “Você está no caminho!” / “Essa semana foi incrível”
- Grupos de amigos e evolução deles
- Treinos importados do Strava
- Resumo da semana:
    - quantos km fiz
    - qual treino faltou
    - como me senti
    - comparação com semana anterior

---

# 🟩 **User Story Central: COMO O APP DEVE MONTAR ISSO?**

### **O Backend deve fornecer:**

1. **Plano semanal na estrutura**

```json
{ "week": 4, "sessions": [...] }

```

1. **Treino do dia**

```json
{ "type": "intervalado", "distance": 6 }

```

1. **Evolução**

```json
{
  "weekKm": 22,
  "avgPace": "6:10",
  "avgFeeling": 7
}

```

1. **Feed de grupo**

```json
[
  { "user": "Maria", "distance": 10, "pace": "5:30" },
  { "user": "João", "distance": 7, "pace": "6:00" }
]

```

---

# 🟧 **User Story Final — App Inteligente da Fase 1 (Sem IA)**

### **Como usuário, quero que o app pareça inteligente, mesmo sem IA, me mostrando decisões simples como:**

- “Seu próximo treino é quarta-feira.”
- “Você correu 12 km esta semana. Faltam 8 km para bater sua meta.”
- “Você se sentiu melhor nesta semana (média 7) do que na anterior (média 6).”
- “Seu grupo correu 87 km no total. Continue assim!”