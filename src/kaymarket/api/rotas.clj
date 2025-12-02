(ns kaymarket.api.rotas 
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body ]]
            [ring.middleware.params :refer [wrap-params]]
            [kaymarket.api.servico :as servico]))
(defroutes app-routes 
  (GET "/acao/:codigo" [codigo] (servico/consultar-acao-handler codigo)) 
  (POST "/compra" request (servico/registrar-operacao (:body request) "compra"))
  (POST "/venda" request (servico/registrar-operacao (:body request) "venda"))
  (GET "/extrato"[inicio fim] (servico/extrato-handler inicio fim))
  (GET "/carteira" [] (servico/saldo-handler))
  (route/not-found "Rota nao encontrada"))

(def app 
  (-> app-routes
      (wrap-json-body {:keywords? true})
      (wrap-params)))