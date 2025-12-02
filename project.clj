(defproject gerenciador-carteira-acoes-clojure "0.1.0-SNAPSHOT"
  :description "Sistema de gerenciamento de carteira de ações KayMarket"
  :url ""
  :license {:name "MIT License"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [compojure "1.7.0"]
                 [ring/ring-core "1.12.1"]
                 [ring/ring-jetty-adapter "1.12.1"]
                 [ring/ring-json "0.5.1"]
                 [cheshire "5.11.0"]
                 [clj-http "3.12.3"]
                 ]

  :main kaymarket.core 
  :profiles {:uberjar {:aot :all}})
