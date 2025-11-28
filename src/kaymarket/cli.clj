(ns kaymarket.cli
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str])
  ;; CORREÇÃO: Uso de parenteses para importação Java é o padrão mais seguro
  (:import (java.time LocalDate)))

;; URL do seu servidor local
(def api-url "http://localhost:3000")

;; --- Funções Auxiliares de Input/Output ---

(defn data-hoje []
  (str (LocalDate/now))) ;; Retorna ex: "2025-11-27"

(defn ler-entrada []
  "Lê o input do usuário, remove espaços e converte para maiúsculo."
  (some-> (read-line)
          str/trim
          str/upper-case))

(defn ler-inteiro []
  "Lê o input e tenta converter para inteiro de forma segura."
  (try
    (Integer/parseInt (str/trim (read-line)))
    (catch NumberFormatException _
      (println "Valor inválido. Digite apenas números.")
      nil)))

;; --- Funções de Requisição HTTP ---

(defn get-json [endpoint]
  (try
    (let [response (http/get (str api-url endpoint) {:as :json})]
      (:body response))
    (catch Exception e
      (let [data (ex-data e)
            status (some-> data :data :status)]
        (if (= status 404)
          {:erro "Recurso não encontrado (404)."}
          {:erro (str "Erro de conexão: " (.getMessage e))})))))

(defn post-json [endpoint body]
  (try
    (http/post (str api-url endpoint)
               {:body (json/generate-string body)
                :content-type :json
                :as :json})
    (catch Exception e
      (println "Erro ao enviar dados:" (.getMessage e)))))

;; --- Lógica de Negócio ---

(defn buscar-preco-real [codigo]
  (if (str/blank? codigo)
    {:erro "Código da ação não pode ser vazio."}
    (do
      (println "Buscando cotação em tempo real para" codigo "...")
      (let [dados (get-json (str "/acao/" codigo))]
        (if (:erro dados)
          (do (println "Erro:" (:erro dados)) nil)
          dados)))))

(defn realizar-operacao [tipo-operacao]
  "Função genérica para Comprar ou Vender para evitar repetição de código."
  (let [endpoint (if (= tipo-operacao :compra) "/compra" "/venda")
        titulo   (if (= tipo-operacao :compra) "COMPRAR" "VENDER")]

    (println (str "\n--- " titulo " AÇÃO ---"))
    (print "Digite o código da ação (ex: PETR4): ") (flush)

    (let [codigo (ler-entrada)]
      (if (str/blank? codigo)
        (println "Operação cancelada: Código inválido.")

        ;; Busca dados na API
        (when-let [dados-acao (buscar-preco-real codigo)]
          (let [preco (:ultimo-preco dados-acao)
                nome  (:nome dados-acao)
                hj    (data-hoje)]

            (println "Ação:" nome)
            (println "Preço de Mercado: R$" preco "| Data:" hj)

            (print "Digite a quantidade: ") (flush)

            (if-let [qtd (ler-inteiro)]
              (if (pos? qtd)
                (do
                  (post-json endpoint {:codigo codigo
                                       :quantidade qtd
                                       :preco preco
                                       :data hj})
                  (println (str/capitalize titulo) "registrada com sucesso!"))
                (println "A quantidade deve ser maior que zero."))
              (println "Operação cancelada."))))))))

;; Wrappers para o menu
(defn acao-comprar [] (realizar-operacao :compra))
(defn acao-vender []  (realizar-operacao :venda))

(defn acao-extrato []
  (println "\n--- EXTRATO ---")
  ;; Range de datas amplo para garantir retorno nos testes
  (let [extrato (get-json "/extrato?inicio=2020-01-01&fim=2030-12-31")]
    (cond
      (:erro extrato) (println "Erro ao buscar extrato:" (:erro extrato))
      (empty? extrato) (println "Nenhuma transação encontrada.")
      :else (doseq [t extrato]
              (println (str (:data t) " | "
                            (str/upper-case (:tipo t)) " | "
                            (:codigo t) " | Qtd: "
                            (:quantidade t) " | R$ " (:preco t)))))))

(defn acao-saldo []
  (println "\n--- SALDO ATUAL ---")
  (let [carteira (get-json "/carteira")]
    (if (:erro carteira)
      (println "Erro ao buscar saldo:" (:erro carteira))
      (doseq [item carteira]
        (println "Ação:" (:acao item) "| Quantidade em carteira:" (:quantidade item))))))

;; --- Menu Principal ---

(defn menu-principal []
  (println "\n==========================")
  (println "   CARTEIRA DE AÇÕES CLI  ")
  (println "==========================")
  (println "1. Comprar Ações")
  (println "2. Vender Ações")
  (println "3. Consultar Extrato")
  (println "4. Ver Saldo da Carteira")
  (println "5. Sair")
  (print "Escolha uma opção: ") (flush)

  (let [opcao (ler-entrada)]
    (case opcao
      "1" (do (acao-comprar) (menu-principal))
      "2" (do (acao-vender) (menu-principal)) ;; Agora funciona completo
      "3" (do (acao-extrato) (menu-principal))
      "4" (do (acao-saldo) (menu-principal))
      "5" (println "Saindo... Até logo!")
      (do (println "Opção inválida.") (menu-principal)))))

(defn -main []
  (menu-principal))