(ns kaymarket.cli
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(def api-url "http://localhost:3000")

;; --- Funções Auxiliares e Formatação ---

(defn data-hora-atual []
  (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")]
    (.format (LocalDateTime/now) formatter)))

(defn ler-entrada []
  (some-> (read-line) str/trim str/upper-case))

(defn ler-inteiro []
  (try
    (Integer/parseInt (str/trim (read-line)))
    (catch NumberFormatException _ nil)))

(defn formatar-dinheiro [valor]
  ;; Formata o número para 2 casas decimais. Ex: 1500.50
  (format "%.2f" (float valor)))

;; --- Requisições HTTP ---

(defn get-json [endpoint]
  (try
    (let [response (http/get (str api-url endpoint) {:as :json})]
      (:body response))
    (catch Exception e
      (let [data (ex-data e)
            status (some-> data :data :status)]
        (if (= status 404)
          nil ;; Retorna nil silenciosamente se não achar
          {:erro (str "Erro: " (.getMessage e))})))))

(defn post-json [endpoint body]
  (try
    (http/post (str api-url endpoint)
               {:body (json/generate-string body)
                :content-type :json
                :as :json
                :throw-exceptions false})
    (catch Exception e
      {:status 500 :body {:mensagem (.getMessage e)}})))

;; --- Funções Recursivas de Impressão (COM CÁLCULOS) ---

(defn imprimir-lista-extrato [lista]
  (if (empty? lista)
    nil
    (let [t (first lista)
          preco      (:preco t)
          qtd        (:quantidade t)
          tipo       (:tipo t)
          ;; CÁLCULO 1: Valor total da transação
          total      (* preco qtd)
          ;; Define o sinal visual (+ ou -)
          sinal      (if (= tipo "compra") "+" "-")
          cor-sinal  (str sinal " " (formatar-dinheiro total))]

      (println (str (:data t) " | "
                    (str/upper-case tipo) " | "
                    (:codigo t) " | Qtd: " qtd
                    " | Unit: R$ " (formatar-dinheiro preco)
                    " | Total: " cor-sinal))

      (recur (rest lista)))))

(defn imprimir-lista-saldo [lista total-acumulado]
  (if (empty? lista)
    ;; CASO BASE: A lista acabou. Imprime o TOTAL GERAL acumulado.
    (println (str "\n--> Patrimônio Total: R$ " (formatar-dinheiro total-acumulado)))

    ;; PASSO RECURSIVO
    (let [item (first lista)
          codigo (:acao item)
          qtd    (:quantidade item)]

      (if (zero? qtd)
        ;; Se qtd é 0, não soma nada e passa pro próximo
        (recur (rest lista) total-acumulado)

        ;; Busca preço atual
        (let [dados-atual (get-json (str "/acao/" codigo))]
          (if dados-atual
            (let [preco-hoje  (:ultimo-preco dados-atual)
                  valor-ativo (* preco-hoje qtd)]

              ;; Imprime a linha individual
              (println (str "Acao: " codigo
                            " | Cotacao Atual: R$ " (formatar-dinheiro preco-hoje)
                            " | Qtd: " qtd
                            " | Patrimonio: R$ " (formatar-dinheiro valor-ativo)))

              ;; RECURSÃO: Chama a função com o resto da lista E soma o valor atual ao acumulado
              (recur (rest lista) (+ total-acumulado valor-ativo)))

            ;; Se der erro na API, avisa e mantém o acumulado anterior
            (do
              (println (str "Acao: " codigo " | Qtd: " qtd " (Erro ao obter cotação)"))
              (recur (rest lista) total-acumulado))))))))

;; --- Lógica de Negócio ---

(defn buscar-preco-real [codigo]
  (if (str/blank? codigo)
    {:erro "Código invalido."}
    (do
      (println "Buscando cotação para" codigo "...")
      (let [dados (get-json (str "/acao/" codigo))]
        (if (:erro dados)
          (do (println "Erro:" (:erro dados)) nil)
          dados)))))

(defn realizar-operacao [tipo-operacao]
  (let [endpoint (if (= tipo-operacao :compra) "/compra" "/venda")
        titulo   (if (= tipo-operacao :compra) "COMPRAR" "VENDER")]

    (println (str "\n--- " titulo " ACAO ---"))
    (print "Digite o codigo (ex: PETR4): ") (flush)

    (let [codigo (ler-entrada)]
      (if (str/blank? codigo)
        (println "Cancelado.")
        (when-let [dados-acao (buscar-preco-real codigo)]
          (let [preco (:ultimo-preco dados-acao)
                nome  (:nome dados-acao)
                agora (data-hora-atual)]

            (println "Acao:" codigo)
            (println "Preco: R$" preco "| Data:" agora)
            (print "Quantidade: ") (flush)

            (if-let [qtd (ler-inteiro)]
              (if (pos? qtd)
                (let [resp (post-json endpoint {:codigo codigo
                                                :quantidade qtd
                                                :preco preco
                                                :data agora})]
                  (if (= (:status resp) 200)
                    (println (str/capitalize titulo) "sucesso! Valor total: R$"
                             (formatar-dinheiro (* preco qtd)))
                    (println "ERRO:" (get-in resp [:body :mensagem]))))
                (println "Qtd deve ser positiva."))
              (println "Operacao cancelada."))))))))

;; Wrappers
(defn acao-comprar [] (realizar-operacao :compra))
(defn acao-vender []  (realizar-operacao :venda))

(defn acao-extrato []
  (println "\n--- EXTRATO FINANCEIRO ---")
  (let [extrato (get-json "/extrato?inicio=2020-01-01&fim=2030-12-31")]
    (if (or (:erro extrato) (empty? extrato))
      (println "Sem transações.")
      (imprimir-lista-extrato extrato))))

(defn acao-saldo []
  (println "\n--- SALDO E PATRIMONIO ---")
  (println "(Consultando valores atuais de mercado...)")
  (let [c (get-json "/carteira")]
    (if (or (:erro c) (empty? c))
      (println "Carteira vazia.")
      ;; MUDANÇA AQUI: Inicia a recursão com acumulador zerado (0.0)
      (imprimir-lista-saldo c 0.0))))

;; --- Menu Principal ---

(defn menu-principal []
  (println "\n-------------------------")
  (println "   KAYMARKET ACOES  ")
  (println "----------------------------")
  (println "1. Comprar Acoes")
  (println "2. Vender Acoes")
  (println "3. Consultar Extrato")
  (println "4. Ver Saldo da Carteira")
  (println "5. Sair")
  (print "Escolha uma opcao: ") (flush)

  (let [opcao (ler-entrada)]
    (case opcao
      "1" (do (acao-comprar) (menu-principal))
      "2" (do (acao-vender) (menu-principal))
      "3" (do (acao-extrato) (menu-principal))
      "4" (do (acao-saldo) (menu-principal))
      "5" (println "Saindo... Ate logo")
      (do (println "Opcao invalida.") (menu-principal)))))

(defn -main []
  (menu-principal))