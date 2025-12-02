(ns kaymarket.dominio.carteira) 

(defn- atualizar-mapa-saldo [saldo-acumulado transacao]
  (let [{:keys [codigo quantidade tipo]} transacao 
        qtd-atual (get saldo-acumulado codigo 0)]
    (if (= tipo "compra")
      (assoc saldo-acumulado codigo (+ qtd-atual quantidade))
      (assoc saldo-acumulado codigo (- qtd-atual quantidade)))))

(defn- calcular-carteira [lista-transacoes acumulador]
  (if (empty? lista-transacoes) acumulador 
      (recur (rest lista-transacoes)
             (atualizar-mapa-saldo acumulador (first lista-transacoes)))))

;; Funções pura
(defn obter-carteira-consolidada [transacoes]
  (calcular-carteira transacoes {}))

(defn obter-quantidade-ativo [transacoes codigo]
  (let [carteira (obter-carteira-consolidada transacoes)]
    (get carteira codigo 0)))