(ns kaymarket.servico
  (:require [cheshire.core :as json]
            [ring.util.response :as r]
            [kaymarket.db :as db]
            [kaymarket.dominio :as domain]
            [kaymarket.client :as client]))

(defn json-response [data]
  (-> (r/response (json/generate-string data))
      (r/content-type "application/json")))

(defn consultar-acao-handler [codigo]
  (let [dados (client/consultar-acao codigo)]
    (json-response dados)))

(defn registrar-operacao [body tipo]
  ;; Body esperado: {:codigo "PETR4" :quantidade 10 :data "2023-11-27"}
  (let [transacao (assoc body :tipo tipo)
        ;; Persiste no atom (efeito colateral controlado)
        _ (db/registrar-transacao! transacao)]
    (json-response {:status "sucesso" :mensagem (str tipo " registrada") :dados transacao})))

(defn extrato-handler [inicio fim]
  (let [todas (db/ler-transacoes)
        filtradas (domain/filtrar-por-periodo todas inicio fim)]
    (json-response filtradas)))

(defn saldo-handler []
  (let [todas (db/ler-transacoes)
        saldo (domain/gerar-relatorio-saldo todas)]
    (json-response saldo)))