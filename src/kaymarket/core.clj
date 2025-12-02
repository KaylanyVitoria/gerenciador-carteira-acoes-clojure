(ns kaymarket.core 
  (:require  [ring.adapter.jetty :refer [run-jetty]]
             [kaymarket.api.rotas :as rotas])
  (:gen-class))

(defn -main []
  (println "Iniciando o servidor na porta 3000")
  (run-jetty rotas/app {:port 3000}))