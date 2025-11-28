(ns kaymarket.dominio)

;; Lógica Pura: Calcular Saldo da Carteira (Quantidade de Ações)
;; Usaremos recursão explícita para evitar loops como doseq/for

(defn- atualizar-mapa-saldo [saldo-acumulado transacao]
  (let [{:keys [codigo quantidade tipo]} transacao
        qtd-atual (get saldo-acumulado codigo 0)]
    (if (= tipo "compra")
      (assoc saldo-acumulado codigo (+ qtd-atual quantidade))
      (assoc saldo-acumulado codigo (- qtd-atual quantidade)))))

(defn calcular-saldo-recursivo
  "Percorre a lista de transações recursivamente para montar o saldo final."
  [lista-transacoes acumulador]
  (if (empty? lista-transacoes)
    acumulador ;; Caso base: lista vazia, retorna o acumulado
    (recur (rest lista-transacoes) ;; Passo recursivo: chama com o resto da lista
           (atualizar-mapa-saldo acumulador (first lista-transacoes)))))

(defn gerar-relatorio-saldo [transacoes]
  ;; Transforma o mapa final em uma lista de mapas para visualização
  (let [mapa-saldos (calcular-saldo-recursivo transacoes {})]
    (map (fn [[k v]] {:acao k :quantidade v}) mapa-saldos)))

;; Lógica Pura: Filtrar por data (Extrato)
;; Assume datas formato "YYYY-MM-DD"
(defn filtrar-por-periodo [transacoes inicio fim]
  (filter (fn [t]
            (let [data-transacao (:data t)]
              ;; Verifica se tudo é string antes de comparar para evitar erro
              (and (string? data-transacao)
                   (string? inicio)
                   (string? fim)
                   ;; (compare A B) retorna: negativo se A < B, 0 se igual, positivo se A > B
                   (>= (compare data-transacao inicio) 0)  ; data >= inicio
                   (<= (compare data-transacao fim) 0))))  ; data <= fim
          transacoes))