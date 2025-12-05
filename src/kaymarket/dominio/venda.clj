(ns kaymarket.dominio.venda
  (:require [kaymarket.dominio.carteira :as d-carteira]))

(defn- transacao-anterior-ou-igual? [transacao data-limite]
  ;;compara string de data da transacao alfabeticam.
  (let [data-transacao (:data transacao)]
    (<= (compare data-transacao data-limite) 0)))

(defn venda-valida? [transacoes codigo qtd-venda data-venda]
  (let [transacoes-validas (filter #(transacao-anterior-ou-igual? % data-venda) transacoes)
        qtd-em-carteira (d-carteira/obter-quantidade-ativo transacoes-validas codigo)]
;;valida se a transacao e valida pela data inserida
    (>= qtd-em-carteira qtd-venda)))