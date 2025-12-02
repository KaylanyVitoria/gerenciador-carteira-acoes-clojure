(ns kaymarket.dominio.extrato
  (:require [kaymarket.dominio.carteira :as d-carteira]
            [clojure.string :as str]))

(defn gerar-relatorio-saldo [transacoes]
  (let [carteira (d-carteira/obter-carteira-consolidada transacoes)]
    (map (fn [[k v]] {:acao k :quantidade v})carteira)))

(defn filtrar-por-periodo [transacoes inicio fim]
  (filter (fn [t]
            (let [data-hora-transacao (:data t)]
              (if (and (string? data-hora-transacao)
                       (>= (count data-hora-transacao) 10)
                       (str/ends-with? (str/lower-case inicio) "z")
                       (str/ends-with? (str/lower-case fim) "z"))
                (let [data-pura (subs data-hora-transacao 0 10)]
                  (and (>= (compare data-pura (subs inicio 0 10)) 0)
                       (<= (compare data-pura (subs fim 0 10)) 0)))
                false))) transacoes))