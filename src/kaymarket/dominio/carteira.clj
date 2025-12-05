(ns kaymarket.dominio.carteira)

(defn- atualizar-mapa-saldo [saldo-acumulado transacao]
  (let [{:keys [codigo quantidade tipo]} transacao
        ;;keys p/ destruturar, definindo esses valores
        qtd-atual (get saldo-acumulado codigo 0)]
    (if (= tipo "compra")
      ;;assoc nao muda o mapa saldo acumulado original, cria nova versao do mapa
      (assoc saldo-acumulado codigo (+ qtd-atual quantidade))
      (assoc saldo-acumulado codigo (- qtd-atual quantidade)))))

;;usando reduce p/ consolidar a carteira
(defn obter-carteira-consolidada [transacoes]
  (reduce atualizar-mapa-saldo {} transacoes))

;; receber todas as transac. e o codigo de cada acao
;;e chama obter carteira p/ fazer o mapa final de saldos
(defn obter-quantidade-ativo [transacoes codigo]
  (let [carteira (obter-carteira-consolidada transacoes)]
    (get carteira codigo 0)))