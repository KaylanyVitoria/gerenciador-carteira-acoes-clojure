(ns kaymarket.client
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def brapi-token "SUA_CHAVE") ; Substitua pelo token gratuito da Brapi
(def base-url "https://brapi.dev/api/quote/")

(defn consultar-acao [simbolo]
  (try
    (let [response (http/get (str base-url simbolo)
                             {:query-params {"token" brapi-token}
                              :as :json})
          dados (first (get-in response [:body :results]))]
      {:codigo (:symbol dados)
       :nome-longo (:longName dados)
       :nome-curto (:shortName dados)
       :moeda (:currency dados)
       :ultimo-preco (:regularMarketPrice dados)
       :preco-max (:regularMarketDayHigh dados)
       :preco-min (:regularMarketDayLow dados)
       :abertura (:regularMarketOpen dados)
       :fechamento (:regularMarketPreviousClose dados)
       :hora (:regularMarketTime dados)})
    (catch Exception e
      {:erro "Não foi possível buscar a ação" :detalhes (.getMessage e)})))