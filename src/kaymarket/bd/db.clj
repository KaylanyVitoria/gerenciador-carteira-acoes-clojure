(ns kaymarket.bd.db)

(def transacoes (atom '()))

(defn registrar-transacao! [transacao]
  (swap! transacoes conj transacao)
  @transacoes)

(defn ler-transacoes []
  @transacoes)