# REGRAS.md — App de marcação do General

> Fonte da verdade do projeto. Em caso de dúvida entre este arquivo e qualquer outra coisa, vale este arquivo.

## 1. Visão geral

Aplicativo Android para **marcar pontos do jogo de dados General**. Os dados são físicos: o app **não rola dados**, apenas funciona como cartela digital.

- Linguagem: Kotlin
- UI: Jetpack Compose (Material 3)
- Arquitetura: MVVM, com **camada de domínio independente de Android**
- Persistência local: Room (banco) e, se preciso, DataStore (preferências)
- Idioma da interface: português do Brasil

---

## 2. Regras do jogo

### 2.1 Fluxo do turno

- Usam-se **5 dados**. Cada turno tem **até 3 jogadas**.
- Na 1ª jogada lançam-se os 5 dados. Nas seguintes, o jogador mantém os dados que quiser e relança os demais.
- Ao final do turno, o resultado é marcado em **uma casa livre** da cartela. Cada casa só pode ser preenchida uma vez.
- Se não houver combinação possível, ou se o jogador preferir, ele pode **queimar** uma casa livre (marca 0).
- O jogo termina quando **todas as casas de todos os jogadores** estiverem preenchidas. Vence quem tiver **mais pontos**.

### 2.2 Casas da cartela (10 casas)

| Casa                 | Combinação                                                              | Normal         | De mão (1ª jogada)   |
| -------------------- | ----------------------------------------------------------------------- | -------------- | -------------------- |
| **1, 2, 3, 4, 5, 6** | Soma apenas dos dados com aquele número (ex.: três 4 = 12 na casa do 4) | soma dos dados | não se aplica        |
| **Full**             | Trinca + par de números **diferentes** (ex.: 3-3-3-5-5)                 | 30             | 35                   |
| **Seguida**          | 1-2-3-4-5, 2-3-4-5-6 **ou 3-4-5-6-1**                                   | 25             | 30                   |
| **Quadra**           | Quatro dados iguais + **um diferente** (ex.: 4-4-4-4-2)                 | 40             | 45                   |
| **General**          | Cinco dados iguais                                                      | 50             | **Vitória imediata (registra 1000 pontos)** |

### 2.3 Regras especiais

1. **Bônus de mão:** Full, Seguida e Quadra feitas já na **primeira jogada** valem +5 pontos (valor "de mão" da tabela).
2. **Casas numéricas** não aceitam "de mão".
3. **General de mão:** cinco dados iguais na 1ª jogada **encerram o jogo com vitória imediata** do jogador, **somente se a casa do General dele estiver livre**. O General de mão registra 1000 pontos.
4. **Cinco dados iguais não valem como Full nem como Quadra.**
5. **General com a casa do General já preenchida (ou queimada):** o jogador pode marcar na **casa numérica correspondente** (ex.: cinco 4 = 20 na casa do 4), **desde que ela esteja livre**. Se não estiver, resta queimar uma casa livre. Não há vitória imediata nesse caso.
6. **Queimar** é permitido a qualquer momento, tanto quando não há opção quanto por escolha do jogador.
7. **Seguida** aceita exatamente três conjuntos de valores: `{1,2,3,4,5}`, `{2,3,4,5,6}` e `{3,4,5,6,1}` (este último equivale a `{1,3,4,5,6}`). Ordem dos dados é irrelevante.

### 2.4 Detalhes de interpretação

- "De mão" é uma **declaração do jogador na hora da marcação**, pois o app não vê os dados. A interface oferece "Normal", "De mão" ou "Queimar".
- O app **confia nos jogadores**: a marcação **não valida os 5 dados**.
- "De mão" **não se aplica a casas numéricas**.
- Sem bônus de mão, "de mão" usa os **valores normais** em Full, Seguida e Quadra.
- General de mão só é oferecido na interface se a casa do General do jogador estiver livre.
- Ao marcar General de mão, o jogo encerra imediatamente, **mesmo que outros jogadores ainda tenham casas livres**. O vencedor é quem declarou o General de mão.

---

## 3. Perfis de regras (configurável)

As regras da seção 2 são o **perfil padrão**, chamado **"Regras da casa"**. O usuário pode criar outros perfis copiando e editando.

### 3.1 Parâmetros configuráveis

| Parâmetro                                                            | Padrão |
| -------------------------------------------------------------------- | ------ |
| Valor normal do Full                                                 | 30     |
| Valor de mão do Full                                                 | 35     |
| Valor normal da Seguida                                              | 25     |
| Valor de mão da Seguida                                              | 30     |
| Valor normal da Quadra                                               | 40     |
| Valor de mão da Quadra                                               | 45     |
| Valor normal do General                                              | 50     |
| Valor de mão do General                                              | 1000   |
| Seguida aceita 3-4-5-6-1                                             | sim    |
| General de mão vence na hora                                         | sim    |
| Cinco dados iguais valem como Full                                   | não    |
| Cinco dados iguais valem como Quadra                                 | não    |
| Cinco dados iguais podem ir para casa numérica (com General ocupado) | sim    |
| Bônus de mão existe                                                  | sim    |

> Valor de mão do General: vale com ou sem vitória imediata, independente do bônus de mão.

### 3.2 Regras sobre perfis

- Existe sempre um perfil padrão que **não pode ser excluído** (pode ser duplicado).
- O usuário escolhe o perfil ao iniciar uma partida e pode **ajustar valores só para aquela partida**.
- **Cada partida guarda uma cópia (snapshot) das regras usadas.** Alterar ou excluir um perfil depois **nunca** altera partidas já iniciadas ou finalizadas.
- O motor de regras recebe o perfil **como parâmetro**. Nenhum valor de pontuação pode ficar fixo no código.

---

## 4. Jogadores

### 4.1 Cadastro

- Jogador é entidade permanente com: `id` (`JogadorId`, valor `String`/UUID gerado **fora** do domínio), `nome` (editável), `nomeNormalizado`, `cor`, `arquivado`, `convidado`.
- Partidas e marcações referenciam o jogador **pelo `JogadorId`, nunca pelo nome**.
- Ao iniciar uma partida, escolhem-se jogadores entre os cadastrados, definindo a **ordem de jogo** (reordenável). Mínimo de 2 jogadores.
- **Convidado:** jogador criado na hora, com opção "salvar como jogador fixo". Convidado também obedece ao nome único. `tornarFixo` remove a marca de convidado.
- **Excluir jogador = arquivar.** Jogadores arquivados somem das listas de seleção, mas o histórico permanece íntegro. Pode-se restaurar.

### 4.2 Nome único

- Não pode haver dois jogadores com o mesmo nome.
- **Comparação ignora** maiúsculas/minúsculas, acentos, espaços nas pontas e espaços duplicados no meio. "joão", "João" e "JOAO" são o mesmo nome.
- A verificação **inclui jogadores arquivados**. Ao tentar criar um nome que pertence a um arquivado, a interface oferece **restaurar** o jogador arquivado.
- Vale também para convidados e para a promoção de convidado a jogador fixo.
- Ao renomear, o próprio jogador **não** conta como conflito (permite corrigir só a capitalização, ex.: "ana" → "Ana").
- Validações do nome: não vazio após o corte de espaços; máximo de 20 caracteres.
- A regra é aplicada em **dois lugares**: na UI (mensagem "Já existe um jogador com esse nome" e botão salvar desabilitado) e no banco (**índice único** em `nomeNormalizado`).
- Guardam-se duas colunas: `nome` (como digitado, para exibir) e `nomeNormalizado` (minúsculas, sem acento, espaços normalizados, para comparar).

### 4.3 Renomear

- Renomear **altera o nome em todo lugar**, inclusive no histórico, pois tudo referencia o `id`.
- Locais de edição: tela de jogadores e, durante a partida, toque longo no nome da coluna da cartela (edição rápida).

---

## 5. Partida e cartela

### 5.1 Interface da cartela

- Uma coluna por jogador, uma linha por casa (1 a 6, Full, Seguida, Quadra, General) e linha de **total** no fim.
- Destaque visual do jogador da vez.
- Ao tocar numa casa livre do jogador da vez:
    - **Casas numéricas:** escolher quantos dados saíram (0 a 5); o app calcula `quantidade × número`.
    - **Full, Seguida, Quadra:** botões **Normal**, **De mão**, **Queimar**, com os pontos do perfil já exibidos.
    - **General:** **Normal**, **De mão** (só se a casa estiver livre), **Queimar**. Se a casa do General já estiver ocupada e saírem cinco dados iguais, o jogador usa a casa numérica correspondente (item 2.3.5).
- **Desfazer** a última marcação (múltiplos níveis, até o início da partida).
- Fim de jogo: tela de resultado com o vencedor em destaque.

### 5.2 Empate

Em caso de empate no total, a partida termina com **empate declarado**, listando todos os empatados como vencedores. (Pode virar opção configurável no futuro.)

### 5.3 Estados da partida

`EM_ANDAMENTO` → `FINALIZADA`. Uma partida em andamento pode ser **retomada** após fechar o app, sem perda de progresso. Cada marcação deve ser salva imediatamente.

---

## 6. Histórico

- Toda partida finalizada fica no histórico, ordenada da mais recente para a mais antiga.
- Cada registro guarda: data/hora, jogadores (por `id`), cartela completa de cada um, totais, vencedor(es), snapshot das regras, e se terminou por **General de mão**.
- Toque numa partida abre a cartela completa (somente leitura).
- É possível **excluir** partidas do histórico (com confirmação).
- Estatísticas por jogador (derivadas do histórico): partidas jogadas, vitórias, média de pontos, melhor pontuação, quantidade de Generais de mão.
- Nomes exibidos no histórico são sempre os **atuais** do cadastro.
- Extra futuro: compartilhar o resultado como texto ou imagem.

---

## 7. Modelo de dados

```
Jogador
  id (JogadorId: valor String, UUID gerado fora do domínio),
  nome, nomeNormalizado (UNIQUE), cor, arquivado, convidado

PerfilDeRegras
  id, nome, padrao (bool), valores e opções (seção 3.1)

Partida
  id, dataInicio, dataFim?, status, regrasSnapshot, vencedores?, terminouPorGeneralDeMao

ParticipantePartida
  partidaId, jogadorId (JogadorId), ordem

Marcacao
  id, partidaId, jogadorId (JogadorId), casa, tipo (NORMAL | DE_MAO | QUEIMADA), pontos, ordemGlobal
```

- Casas: `UM, DOIS, TRES, QUATRO, CINCO, SEIS, FULL, SEGUIDA, QUADRA, GENERAL`.
- **Total do jogador é sempre calculado** a partir das marcações, nunca armazenado. Isso mantém "desfazer" consistente.
- `ordemGlobal` define a sequência das marcações e viabiliza o desfazer.
- Restrição: `(partidaId, jogadorId, casa)` é único.

---

## 8. Arquitetura sugerida

```
domain/        (Kotlin puro, sem Android)
  model/       Casa, TipoMarcacao, Jogador, PerfilDeRegras, Partida...
  rules/       cálculo de pontos, validação de marcação, fim de jogo
data/
  db/          Room: entidades, DAOs, índice único
  repository/  implementações dos repositórios
ui/
  jogadores/  perfis/  partida/  historico/  estatisticas/
```

- Regras ficam **fora** da UI e do banco, e recebem o perfil como parâmetro.
- ViewModels expõem estado imutável (`StateFlow`) para o Compose.

---

## 9. Casos de teste obrigatórios (camada de domínio)

### Pontuação das casas numéricas

- Três dados 4 na casa do 4 → 12.
- Zero dados iguais ao número → 0 (marcação válida, não queimada).
- Cinco dados 6 na casa do 6 → 30.

### Full

- 3-3-3-5-5 → válido; 30 normal, 35 de mão.
- 4-4-4-4-4 → **inválido** como Full.
- 3-3-3-5-6 → inválido.

### Seguida

- 1-2-3-4-5 → válido (25 / 30).
- 2-3-4-5-6 → válido.
- 3-4-5-6-1 (qualquer ordem) → válido.
- 1-2-3-4-6 → inválido.
- Com a opção "aceita 3-4-5-6-1" desligada → 3-4-5-6-1 inválido.

### Quadra

- 4-4-4-4-2 → válido (40 / 45).
- 4-4-4-4-4 → **inválido** como Quadra.
- 4-4-4-2-2 → inválido.

### General

- 5-5-5-5-5 normal → 50.
- General de mão com casa livre → **fim imediato de jogo, vitória do jogador**.
- General de mão registra 1000 pontos quando vence na hora.
- General de mão registra 1000 pontos quando `generalDeMaoVenceNaHora=false`.
- General de mão registra 1000 pontos mesmo com `bonusDeMaoExiste=false`.
- Cinco dados iguais com a casa do General ocupada → não vence; marca-se na casa numérica livre correspondente como NORMAL.
- General com casa do General e casa numérica correspondente ocupadas → só é possível queimar outra casa livre.

### Bônus de mão

- Full, Seguida e Quadra de mão valem +5.
- "De mão" em casa numérica é rejeitado.

### Queimar

- Queimar marca 0 em casa livre, a qualquer momento.
- Não é possível marcar casa já preenchida (inclusive queimada).

### Fim de jogo e desfazer

- Jogo termina quando todas as casas de todos os jogadores estão preenchidas.
- Empate gera múltiplos vencedores.
- Desfazer remove a última marcação e restaura o jogador da vez.
- Desfazer a marcação de um General de mão reabre a partida.

### Perfis

- Alterar valor da Seguida num perfil **não** muda pontos de partidas já iniciadas.
- Perfil padrão não pode ser excluído.

### Jogadores

- "João", "joão", " JOAO " → conflito entre si.
- Criar nome igual ao de um arquivado → conflito (com oferta de restaurar).
- Renomear "ana" para "Ana" (mesmo jogador) → permitido.
- Nome vazio ou só com espaços → inválido; mais de 20 caracteres → inválido.
- Renomear jogador reflete o novo nome em partidas antigas.

---

## 10. Ordem de construção

1. **Domínio e testes** (seções 2, 3 e 9), sem UI.
2. **Banco e repositórios** (seção 7), incluindo índice único de nome.
3. **Jogadores:** cadastro, edição, arquivamento.
4. **Perfis de regras.**
5. **Partida e cartela** (seção 5), com desfazer e retomada.
6. **Histórico e estatísticas.**
7. **Acabamento:** tema escuro, manter a tela ligada, ícone, compartilhar resultado.
8. **Distribuição:** APK para uso privado; Play Store opcional.

---

## 11. Prompt inicial sugerido para o Cursor

> Crie um projeto Android em Kotlin com Jetpack Compose (Material 3, MVVM). Leia o `REGRAS.md`, que é a fonte da verdade. Comece **apenas pela camada de domínio**, sem UI e sem banco: modelos (casas, tipos de marcação, jogador, perfil de regras, partida), cálculo de pontuação de cada casa, regras de "de mão", queimar, General de mão encerrando o jogo, e a normalização/validação de nomes de jogador. O perfil de regras deve ser **parâmetro** do motor, sem valores fixos no código. Escreva testes unitários cobrindo todos os casos da seção 9.

---

## 12. Pontos em aberto

- Empate: manter "empate declarado" ou criar critério de desempate configurável?
- Outras regras exóticas como parâmetros do perfil, além dos listados na seção 3.1?
- Limite máximo de jogadores por partida (sugestão: 6, por causa da largura da tela).
