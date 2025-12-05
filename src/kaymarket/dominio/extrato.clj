(ns kaymarket.dominio.extrato
  (:require [kaymarket.dominio.carteira :as d-carteira]
            [clojure.string :as str]))

(defn gerar-relatorio-saldo [transacoes]
  ;;obtem o mapa da carteira consolidada
  (let [carteira (d-carteira/obter-carteira-consolidada transacoes)]
    (map (fn [[k v]] {:acao k :quantidade v}) carteira))) ;;k e o codigo/v e saldo

(defn- data-no-periodo? [data inicio fim]
  ;;comparando as datas dentro do intervalo
  ;;data inicio fim sao var. locais, nao altera o valor global
  (let [d (if (> (count data) 10) (subs data 0 10) data);;se data tiver horario, usa somente os 10 primeiros caracteres
        i (if (> (count inicio) 10) (subs inicio 0 10) inicio)
        f (if (> (count fim) 10) (subs fim 0 10) fim)]
    ;;compara as strings d,i, p/ ordenar alfabeticamente
    (and (>= (compare d i) 0)
         (<= (compare d f) 0))))

(defn filtrar-por-periodo [transacoes inicio fim]
  ;;filtra p/ usar somente as datas no periodo de interesse
  (filter (fn [t] (data-no-periodo? (:data t) inicio fim))
          transacoes))