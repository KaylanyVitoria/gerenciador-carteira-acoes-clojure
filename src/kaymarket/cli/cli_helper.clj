(ns kaymarket.cli.cli-helper
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

;; ============================================
;; CONFIG
;; ============================================
(def api-url "http://localhost:3000")

;; ============================================
;; UTILITÁRIOS
;; ============================================
(defn data-hora-atual []
  (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd")]
    (.format (LocalDateTime/now) formatter)))

(defn ler-entrada []
  (some-> (read-line) str/trim str/upper-case))

(defn ler-inteiro []
  (try
    (Integer/parseInt (str/trim (read-line)))
    (catch Exception _ nil)))

(defn formatar-dinheiro [valor]
  (if (number? valor)
    (format "%.2f" (float valor))
  "0.00"))

;; ============================================
;; GET JSON COM ERROS PADRONIZADOS
;; ============================================
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

;; ============================================
;; POST JSON COM RETORNO PADRÃO
;; ============================================
(defn post-json [endpoint body]
  (try
    (let [resp (http/post (str api-url endpoint)
                          {:body (json/generate-string body)
                           :content-type :json
                           :as :json
                           :throw-exceptions false})]

      (if (>= (:status resp) 400)
        {:erro (get-in resp [:body :mensagem] "Erro ao processar transação")}
        ;; retorno padronizado
        {:sucesso true :dados (:body resp)}))

    (catch Exception e
      {:erro (.getMessage e)})))

;; ============================================
;; BUSCAR PREÇO REAL
;; ============================================
(defn buscar-preco-real [codigo]
  (println "Buscando cotação para" codigo "...")
  (let [resp (get-json (str "/acao/" codigo))]
    (if (:erro resp)
      (do
        (println "ERRO:" (:erro resp))
        nil)
      resp)))

;; ============================================
;; E BUSCA HISTORICA
;; ============================================
(defn buscar-preco-historico [codigo data]
  (println "Buscando cotação para" codigo "em" data "...")
  (let [resp (get-json (str "/acao/" codigo "?data=" data))]
    (if (:erro resp)
      (do (println "ERRO:" (:erro resp)) nil)
      resp)))
;; ============================================
;; IMPRESSÃO DA AÇÃO SIMPLES
;; ============================================

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

;; ============================================
;; IMPRESSÃO DE EXTRATO
;; ============================================
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

;; ============================================
;; IMPRESSÃO DE SALDO
;; ============================================
(defn imprimir-lista-saldo [lista total]
  (if (empty? lista)
    (println (str "\n--> Patrimônio Total: R$ " (formatar-dinheiro total)))

    (let [{codigo :acao qtd :quantidade} (first lista)]
      (if (zero? qtd)
        (recur (rest lista) total)

        ;; buscar cotação da API
        (let [resp (get-json (str "/acao/" codigo))]
          (if (:erro resp)
            (do
              (println (str "Ação: " codigo " | Qtd: " qtd " (Erro ao obter cotação)"))
              (recur (rest lista) total))

            (let [preco (:ultimo-preco resp)
                  valor (* preco qtd)]
              (println (str "Ação: " codigo
                            " | Cotação Atual: R$ " (formatar-dinheiro preco)
                            " | Qtd: " qtd
                            " | Patrimônio: R$ " (formatar-dinheiro valor)))
              (recur (rest lista) (+ total valor)))))))))

