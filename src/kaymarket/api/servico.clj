(ns kaymarket.api.servico
  (:require [cheshire.core :as json]
            [ring.util.response :as r]
            [kaymarket.bd.db :as db]
            [kaymarket.dominio.carteira :as d-carteira]
            [kaymarket.dominio.venda :as d-venda]
            [kaymarket.dominio.extrato :as d-extrato]
            [kaymarket.bd.client :as client]))

(defn- json-response [data & [status]]
  (-> (r/response (json/generate-string data))
      (r/content-type  "application/json")
      (r/status (or status 200))))

(defn consultar-acao-handler [codigo data]
  (let [dados (client/consultar-acao-externa codigo data)]
    (if (:erro dados)
      (json-response dados 404)
      (json-response dados))))

(defn- processar-compra [transacao]
  (db/registrar-transacao! transacao)
  (json-response {:status "sucesso" :mensagem "Compra registrada" :dados transacao}))

(defn- processar-venda [transacao]
  (let [codigo    (:codigo transacao)
        qtd-venda (:quantidade transacao)
        data-venda (:data transacao)
        historico (db/ler-transacoes)]

    (if (d-venda/venda-valida? historico codigo qtd-venda data-venda)
      (do (db/registrar-transacao! transacao)
          (json-response {:status "sucesso" :mensagem "Venda registrada" :dados transacao}))

      (json-response {:status "erro"
                      :mensagem (str "Saldo insuficiente em " data-venda " para vender " qtd-venda " de " codigo)}
                     400))))

(defn registrar-operacao [body tipo]
  (let [transacao (assoc body :tipo tipo)]
    (if (= tipo "compra")
      (processar-compra transacao)
      (processar-venda transacao))))

(defn extrato-handler [inicio fim]
  (let [todas (db/ler-transacoes)
        filtradas (d-extrato/filtrar-por-periodo todas inicio fim)]
    (json-response filtradas)))

(defn saldo-handler []
  (let [todas (db/ler-transacoes)
        saldo (d-extrato/gerar-relatorio-saldo todas)]
    (json-response saldo)))