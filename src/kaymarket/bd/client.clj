(ns kaymarket.bd.client
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def brapi-token "2sX4R369RTb5o8KYZK4EWr")
(def base-url "https://brapi.dev/api/quote/")

(defn- formatar-resposta-brapi [dados-brutos] 
  (when dados-brutos
    {:codigo (:symbol dados-brutos)
     :nome-longo (:longName dados-brutos)
     :nome-curto (:shortName dados-brutos)
     :moeda (:currency dados-brutos)
     :ultimo-preco (:regularMarketPrice dados-brutos)
     :preco-max (:regularMarketDayHigh dados-brutos)
     :preco-min (:regularMarketDayLow dados-brutos)
     :abertura (:regularMarketOpen dados-brutos)
     :fechamento (:regularMarketPreviousClose dados-brutos)
     :hora (:regularMarketTime dados-brutos)}))
(defn consultar-acao-externa
  [simbolo]
  (try
    (let [response (http/get (str base-url simbolo)
                             {:query-params {"token" brapi-token}
                              :as :json
                              :throw-exceptions true})
          dados (first (get-in response [:body :results]))]

      (if dados
        (formatar-resposta-brapi dados)
        {:erro "Ação não encontrada" :simbolo simbolo}))

    (catch Exception e
      (let [data (ex-data e)]
        (if (= (:status data) 404)
          {:erro "Ação não encontrada" :simbolo simbolo}
          {:erro "Não foi possível buscar a ação"
           :detalhes (.getMessage e)})))))