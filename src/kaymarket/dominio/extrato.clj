(ns kaymarket.dominio.extrato
  (:require [kaymarket.dominio.carteira :as d-carteira]
            [clojure.string :as str]))

(defn gerar-relatorio-saldo [transacoes]
  (let [carteira (d-carteira/obter-carteira-consolidada transacoes)]
    (map (fn [[k v]] {:acao k :quantidade v}) carteira)))

(defn- data-no-periodo? [data inicio fim]
  (let [d (if (> (count data) 10) (subs data 0 10) data)
        i (if (> (count inicio) 10) (subs inicio 0 10) inicio)
        f (if (> (count fim) 10) (subs fim 0 10) fim)]
    (and (>= (compare d i) 0)
         (<= (compare d f) 0))))

(defn filtrar-por-periodo [transacoes inicio fim]
  (filter (fn [t]
            (data-no-periodo? (:data t) inicio fim))
          transacoes))