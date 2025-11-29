(ns kaymarket.dominio)

;; Lógica Pura: Calcular Saldo da Carteira (Quantidade deAcoes)
;; Usaremos recursão explícita para evitar loops como doseq/for

(defn- atualizar-mapa-saldo
  "Função pura que recebe o acumulador (mapa de ativos) e uma transação,
   retornando o novo estado do acumulador."
  [saldo-acumulado transacao]
  (let [{:keys [codigo quantidade tipo]} transacao
        qtd-atual (get saldo-acumulado codigo 0)]
    (if (= tipo "compra")
      (assoc saldo-acumulado codigo (+ qtd-atual quantidade))
      ;; Se for venda, subtrai
      (assoc saldo-acumulado codigo (- qtd-atual quantidade)))))

(defn calcular-carteira-recursivo
  "Percorre a lista de transações e retorna um mapa {CODIGO QUANTIDADE}."
  [lista-transacoes acumulador]
  (if (empty? lista-transacoes)
    acumulador
    (recur (rest lista-transacoes)
           (atualizar-mapa-saldo acumulador (first lista-transacoes)))))

;; --- Funções Públicas de Negócio ---

(defn obter-carteira-consolidada
  "Retorna mapa simples: {'PETR4' 100, 'VALE3' 50}"
  [transacoes]
  (calcular-carteira-recursivo transacoes {}))

(defn obter-quantidade-ativo
  "Retorna quantas ações de um código específico o usuário tem."
  [transacoes codigo]
  (let [carteira (obter-carteira-consolidada transacoes)]
    (get carteira codigo 0)))

(defn venda-valida?
  "Retorna true se o usuário tiver saldo suficiente para vender."
  [transacoes codigo qtd-venda]
  (let [qtd-em-carteira (obter-quantidade-ativo transacoes codigo)]
    (>= qtd-em-carteira qtd-venda)))

(defn gerar-relatorio-saldo
  "Formata para visualização (Lista de mapas)"
  [transacoes]
  (let [carteira (obter-carteira-consolidada transacoes)]
    ;; Transforma o mapa em lista para JSON
    (map (fn [[k v]] {:acao k :quantidade v}) carteira)))

;; (Mantém a função filtrar-por-periodo que já corrigimos antes)
(defn filtrar-por-periodo [transacoes inicio fim]
  (filter (fn [t]
            (let [data-hora-transacao (:data t)]
              (if (and (string? data-hora-transacao)
                       (>= (count data-hora-transacao) 10) ;; Garante que tem tamanho p/ extrair data
                       (string? inicio)
                       (string? fim))
                ;; Extrai apenas "YYYY-MM-DD" da string completa "YYYY-MM-DD HH:mm:ss"
                (let [data-pura (subs data-hora-transacao 0 10)]
                  (and (>= (compare data-pura inicio) 0)
                       (<= (compare data-pura fim) 0)))
                ;; Se formato for inválido, ignora ou aceita (aqui optamos por false/ignorar)
                false)))
          transacoes))