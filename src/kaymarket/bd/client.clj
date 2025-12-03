(ns kaymarket.bd.client
  (:require
   [clj-http.client :as http]
   [clojure.string :as str])
  (:import (java.time Instant ZoneId)
           (java.time.format DateTimeFormatter))
  )

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

(defn- converter-timestamp-para-data [timestamp]
  (try
    (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd")
          instant   (Instant/ofEpochSecond timestamp)
          zone      (ZoneId/of "UTC")] 
      (.format (.withZone formatter zone) instant))
    (catch Exception _
      ;; Se falhar a conversão, retorna o próprio valor como string
      (str timestamp))))

(defn- encontrar-preco-por-data [historico data-alvo]
  (if (empty? historico)
    nil
      (let [item (first historico)
        raw-date (:date item)]
        (let [data-formatada (cond
                               (number? raw-date) (converter-timestamp-para-data raw-date)
                               (string? raw-date) (if (> (count raw-date) 10)
                                                    (subs raw-date 0 10)
                                                    raw-date)
                               :else "")]
      (if (= data-formatada data-alvo)
        (:close item)
        (recur (rest historico) data-alvo))))))

(defn- buscar-na-api [url query-params]
  (try
    (let [response (http/get url {:query-params (merge {"token" brapi-token} query-params)
                                  :as :json
                                  :throw-exceptions true})
          results (get-in response [:body :results])]
   (if (not-empty results)
     (first results)
     nil))
  (catch Exception e
    nil)))

(defn consultar-acao-externa [simbolo data-referencia]
  (if (or (nil? data-referencia) (str/blank? data-referencia))
    (let [dados (buscar-na-api (str base-url simbolo) {})]
      (if dados
        (formatar-resposta-brapi dados)
        {:erro "Ação não encontrada"}))


    (let [dados (buscar-na-api (str base-url simbolo) {"range" "3mo" "interval" "1d"})]
      (if dados
        (let [historico (:historicalDataPrice dados)]
          (if (empty? historico)
            {:erro "Histórico não disponível para esta ação."}
            (let [preco-historico (encontrar-preco-por-data historico data-referencia)]
              (if preco-historico
                {:codigo (:symbol dados)
                 :ultimo-preco preco-historico
                 :data-cotacao data-referencia
                 :nome-curto (:shortName dados)}
                {:erro (str "Cotação não encontrada para a data " data-referencia)}))))
            {:erro "Ação não encontrada"}))))