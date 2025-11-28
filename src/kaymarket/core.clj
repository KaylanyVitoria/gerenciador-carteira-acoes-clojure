(ns kaymarket.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [kaymarket.servico :as h])
  (:gen-class))

(defroutes app-routes
  ;; 1. Consultar dados da ação (Brapi)
  (GET "/acao/:codigo" [codigo] (h/consultar-acao-handler codigo))

  ;; 2. Registrar Compra
  (POST "/compra" request (h/registrar-operacao (:body request) "compra"))

  ;; 3. Registrar Venda
  (POST "/venda" request (h/registrar-operacao (:body request) "venda"))

  ;; 4. Extrato por período (Query params: ?inicio=2023-01-01&fim=2023-12-31)
  (GET "/extrato" [inicio fim] (h/extrato-handler inicio fim))

  ;; 5. Saldo da carteira
  (GET "/carteira" [] (h/saldo-handler))

  (route/not-found "Rota não encontrada"))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      (wrap-params))) ;; Converte JSON body para keywords Clojure automaticamente

(defn -main []
  (println "Servidor rodando na porta 3000...")
  (run-jetty app {:port 3000}))