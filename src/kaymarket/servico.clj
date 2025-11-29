(ns kaymarket.servico
  (:require [cheshire.core :as json]
            [ring.util.response :as r]
            [kaymarket.db :as db]
            [kaymarket.dominio :as domain]
            [kaymarket.client :as client]))

(defn json-response [data & [status]]
  (-> (r/response (json/generate-string data))
      (r/content-type "application/json")
      (r/status (or status 200))))

(defn consultar-acao-handler [codigo]
  (let [dados (client/consultar-acao codigo)]
    (json-response dados)))

(defn processar-compra [transacao]
  (db/registrar-transacao! transacao)
  (json-response {:status "sucesso" :mensagem "Compra registrada" :dados transacao}))

(defn processar-venda [transacao]
  (let [codigo (:codigo transacao)
        qtd-venda (:quantidade transacao)
        ;; 1. Lê o histórico atual (Leitura pura do átomo)
        historico-atual (db/ler-transacoes)]

    ;; 2. Usa lógica pura para validar
    (if (domain/venda-valida? historico-atual codigo qtd-venda)
      (do
        ;; Se válido, altera o estado (efeito colateral)
        (db/registrar-transacao! transacao)
        (json-response {:status "sucesso" :mensagem "Venda registrada" :dados transacao}))

      ;; Se inválido, retorna erro 400
      (json-response {:status "erro"
                      :mensagem (str "Saldo insuficiente para vender " qtd-venda " de " codigo)}
                     400))))

(defn registrar-operacao [body tipo]
  (let [transacao (assoc body :tipo tipo)]
    (if (= tipo "compra")
      (processar-compra transacao)
      (processar-venda transacao))))

(defn extrato-handler [inicio fim]
  (let [todas (db/ler-transacoes)
        filtradas (domain/filtrar-por-periodo todas inicio fim)]
    (json-response filtradas)))

(defn saldo-handler []
  (let [todas (db/ler-transacoes)
        saldo (domain/gerar-relatorio-saldo todas)]
    (json-response saldo)))