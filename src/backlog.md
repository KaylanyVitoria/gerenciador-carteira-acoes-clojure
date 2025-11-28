backlog.md

Instruções para rodar o projeto

INSERIR CHAVE DA API NO CLIENT.CLJ


Acessa pasta do projeto no Prompt de Comando
Roda o servidor com:
lein run
Acessa outra aba de Prompt de Comando
Roda o REPL:
lein repl

Roda o seguinte comando, linha por linha:
(require '[kaymarket.cli :as cli])

(cli/menu-principal)

Executar os testes de acordo com a necessidade

Observações dos próximos passos>
- O extrato não está sendo feito por período, somente total do que existe no banco
- A consulta também não está calculando o total (EM DINHEIRO) que o usuário possui, está só indicando quantas ações tem de cada ativo
- O extrato NÃO está exibindo todas as informações, somente DATA (falta hora), tipo da transação (se compra ou venda), ativo (ex. PETR4), quantidade de ações que possui e valor da ação (NÃO valor total gasto)
- Faltam TODOS os testes unitários e integrados do Clojure
