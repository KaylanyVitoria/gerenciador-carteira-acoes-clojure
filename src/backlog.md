# Backlog - KayMarket - Carteira de Ações

### Instruções de execução

A seguir estão instruções para executar o projeto em ambiente Windows.

Utilizando leiningen, no Prompt de Comando

1. **INSERIR CHAVE DA API NO CLIENT.CLJ**

Insira sua chave gratuita corretamente no campo “SUA_CHAVE”

1. Acessa pasta do projeto no Prompt de Comando
Roda o servidor com:
lein run
    
    Essa execução irá iniciar o servidor na porta 3000.
    
2. Acessa OUTRA aba de Prompt de Comando
Roda o REPL:
lein repl
3. Roda os seguinte comandos, linha por linha:
(require '[kaymarket.cli :as cli])
    
    (cli/menu-principal)
    

→ Executar os testes de acordo com a necessidade.

### BACKLOG:

⇒ ~~Próximos passos / date: **27/11/25**~~

- O extrato não está sendo feito por período, somente total do que existe no banco
- A consulta também não está calculando o total (EM DINHEIRO) que o usuário possui, está só indicando quantas ações tem de cada ativo
- O extrato NÃO está exibindo todas as informações, somente DATA (falta hora), tipo da transação (se compra ou venda), ativo (ex. PETR4), quantidade de ações que possui e valor da ação (NÃO valor total gasto)
- Faltam TODOS os testes unitários e integrados do Clojure

⇒ Próximos passos / date: **29/11/25**

- O extrato não está sendo feito por período, somente total do que existe no banco
- Refatoração - Divisão de Responsabilidades
- Faltam TODOS os testes unitários e integrados do Clojure
- ~~A consulta também não está calculando o total (EM DINHEIRO) que o usuário possui, está só indicando quantas ações tem de cada ativo~~ (FEITO)
- ~~O extrato NÃO está exibindo todas as informações, somente DATA (falta hora), tipo da transação (se compra ou venda), ativo (ex. PETR4), quantidade de ações que possui e valor da ação (NÃO valor total gasto~~) (FEITO)