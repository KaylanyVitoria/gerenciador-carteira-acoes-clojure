(ns kaymarket.db)

(def transacoes (atom '()))

(defn registrar-transacao!
  "Adiciona uma transação ao início da lista de forma atômica.
   Retorna o estado atualizado."
  [transacao]
  (swap! transacoes conj transacao))

(defn ler-transacoes
  "Retorna a lista atual de transações (snapshot imutável do momento)."
  []
  @transacoes)