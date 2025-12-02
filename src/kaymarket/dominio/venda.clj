(ns kaymarket.dominio.venda
 (:require [kaymarket.dominio.carteira :as d-carteira]))

(defn venda-valida? [transacoes codigo qtd-venda]
  (let [qtd-em-carteira (d-carteira/obter-quantidade-ativo transacoes codigo)]
    (>= qtd-em-carteira qtd-venda)))