(ns kaymarket.cli.cli-helper
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import (java.time LocalDateTime LocalDate)
           (java.time.format DateTimeFormatter)
           (java.time.temporal ChronoUnit)))

;; configura o end. da API
(def api-url "http://localhost:3000")

;; utilitarios
(defn data-hora-atual []
  (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd")]
    (.format (LocalDateTime/now) formatter)))
(defn data-recente? [data-string]
  (try
    (let [data-input (LocalDate/parse data-string)
          hoje       (LocalDate/now)
          diferenca  (.between ChronoUnit/DAYS data-input hoje)]
      (<= (Math/abs diferenca) 2))
    (catch Exception _ false)))

(defn ler-entrada []
  (some-> (read-line) str/trim str/upper-case))

(defn ler-inteiro []
  (try
    (Integer/parseInt (str/trim (read-line)))
    (catch Exception _ nil)))

(defn formatar-dinheiro [valor] ;;so pega o numero e formata a string
  (if (number? valor)
    (format "%.2f" (float valor))
  "0.00"))
;; rotas get do jsonn, ja com erros padrao
(defn get-json [endpoint]
  (try
    (let [resp (http/get (str api-url endpoint)
                         {:as :json
                          :throw-exceptions false})]
      (if (>= (:status resp) 400)
        {:erro (get-in resp [:body :mensagem] "Erro desconhecido no servidor")}
        (:body resp)))

    (catch Exception e
      {:erro (.getMessage e)})))

;; rotas post JSON com retorno padrao
(defn post-json [endpoint body]
  (try
    (let [resp (http/post (str api-url endpoint)
                          {:body (json/generate-string body)
                           :content-type :json
                           :as :json
                           :throw-exceptions false})]

      (if (>= (:status resp) 400)
        {:erro (get-in resp [:body :mensagem] "Erro ao processar transação")}
        {:sucesso true :dados (:body resp)}))

    (catch Exception e
      {:erro (.getMessage e)})))

;; busca preco real (em tempo real, atual)
(defn buscar-preco-real [codigo]
  (println "Buscando cotação para" codigo "...")
  (let [resp (get-json (str "/acao/" codigo))]
    (if (:erro resp)
      (do (println "ERRO:" (:erro resp)) nil)
      resp)))

;; E busca historica (pela data, coom historicalPrice)
(defn buscar-preco-historico [codigo data]
  (println "Buscando cotação para" codigo "em" data "...")
  (let [resp (get-json (str "/acao/" codigo "?data=" data))]
    (if (:erro resp)
      (do (println "ERRO:" (:erro resp)) nil)
      resp)))

;; imprime na tela os dados da acao simples

(defn imprimir-detalhes-acao [dados]
  (println "\n========================================")
  (println "          DETALHES DA AÇÃO")
  (println "========================================")
  (println "Código:      " (:codigo dados))
  (println "Nome:        " (:nome-curto dados))
  (println "Preço Atual:  R$" (formatar-dinheiro (:ultimo-preco dados)))
  (println "----------------------------------------")
  (println "Abertura:     R$" (formatar-dinheiro (:abertura dados)))
  (println "Fechamento:   R$" (formatar-dinheiro (:fechamento dados)))
  (println "Mínima Dia:   R$" (formatar-dinheiro (:preco-min dados)))
  (println "Máxima Dia:   R$" (formatar-dinheiro (:preco-max dados)))
  (println "Horário Ref: " (:hora dados))
  (println "========================================\n"))

;; imprime os dados do extrato
(defn imprimir-lista-extrato [lista]
  (if (empty? lista)
    nil
    (let [t (first lista)
          preco (or (:preco t) 0)
          qtd   (or (:quantidade t) 0)
          tipo  (or (:tipo t) "DESCONHECIDO")
          total (* preco qtd)
          sinal (if (= tipo "compra") "+" "-")]

    
      (println (str (:data t) " | "
                    (str/upper-case tipo) " | "
                    (:codigo t) " | Qtd: " qtd
                    " | Unit: R$ " (formatar-dinheiro preco)
                    " | Total: " sinal " " (formatar-dinheiro total)))

    
      (recur (rest lista)))))
;; imprime os dados de saldo
(defn imprimir-lista-saldo [lista total]
  (if (empty? lista)
    (println (str "\n--> Patrimônio Total: R$ " (formatar-dinheiro total)))

    (let [{codigo :acao qtd :quantidade} (first lista)]
      (if (zero? qtd)
        (recur (rest lista) total)

        ;; buscar cotacao ATUAL na API
        (let [resp (get-json (str "/acao/" codigo))]
          (if (:erro resp)
            (do (println (str "Ação: " codigo " | Qtd: " qtd " (Erro ao obter cotação)"))
              (recur (rest lista) total))

            (let [preco (:ultimo-preco resp)
                  valor (* preco qtd)]
              (println (str "Ação: " codigo
                            " | Cotação Atual: R$ " (formatar-dinheiro preco)
                            " | Qtd: " qtd
                            " | Patrimônio: R$ " (formatar-dinheiro valor)))
              (recur (rest lista) (+ total valor)))))))))

