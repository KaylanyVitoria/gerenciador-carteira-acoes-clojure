(ns kaymarket.db)

;; O Estado Global da aplicação (Atomicidade)
;; Inicializa com uma lista vazia
(def transacoes (atom '()))

(defn registrar-transacao!
  "Adiciona uma transação ao início da lista de forma atômica.
   Retorna o estado atualizado."
  [transacao]
  ;; swap! garante que a mudança de estado seja atômica
  (swap! transacoes conj transacao))

(defn ler-transacoes
  "Retorna a lista atual de transações (snapshot imutável do momento)."
  []
  @transacoes)