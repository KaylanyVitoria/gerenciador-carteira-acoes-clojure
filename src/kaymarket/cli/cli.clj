(ns kaymarket.cli.cli
  (:require [kaymarket.cli.cli-helper :as h]
            [clojure.string :as str])
  (:gen-class))

;; ==========================================
;;   Funçao generica de consulta smples
;; ==========================================
 (defn acao-consultar []
  (println "\n--- CONSULTAR COTAÇÃO ATUAL ---")
  (print "Digite o código da ação (ex: VALE3): ") (flush)
  (let [codigo (h/ler-entrada)]
    (if (str/blank? codigo)
      (println "Operação cancelada.")
      (let [dados (h/buscar-preco-real codigo)]
        (when dados
          (h/imprimir-detalhes-acao dados))))))

;; ==========================================
;;   Função Genérica de Compra e Venda
;; ==========================================
 (defn realizar-operacao [tipo-operacao]
   (let [endpoint (if (= tipo-operacao :compra) "/compra" "/venda")
         titulo   (if (= tipo-operacao :compra) "ACAO COMPRAR" "ACAO VENDER")]
 
     (println (str "\n--- " titulo " ACAO ---"))
     (print "Digite o codigo (ex: PETR4): ") (flush)
 
     (let [codigo (h/ler-entrada)]
       (if (str/blank? codigo)
         (println "Operação cancelada.")
 
         (do
           (print "Digite a data da operação (YYYY-MM-DD): ") (flush)
           (let [data-op (h/ler-entrada)]
             (if (or (str/blank? data-op) (< (count data-op) 10))
               (println "Data inválida.")
 
              
               (let [dados-acao (h/buscar-preco-historico codigo data-op)]
 
                
                 (if (or (nil? dados-acao) (:erro dados-acao))
                   (println "\n(Operação cancelada: Não foi possível obter o preço para esta data.)")
 
                 
                   (let [{preco :ultimo-preco} dados-acao]
                     (if (or (nil? preco) (zero? preco))
                       (println "\nERRO CRÍTICO: O preço retornado é zero ou inválido.")
 
                       (do
                         (println "--------------------------------")
                         (println "Ação:" codigo)
                         (println "Data:" data-op)
                         (println "Preço Histórico: R$" (h/formatar-dinheiro preco))
                         (println "--------------------------------")
 
                         (print "Quantidade: ") (flush)
                         (if-let [qtd (h/ler-inteiro)]
                           (if (pos? qtd)
                             (let [resp (h/post-json endpoint
                                                     {:codigo codigo
                                                      :quantidade qtd
                                                      :preco preco
                                                      :data data-op})]
                               (if (:erro resp)
                                 (println "ERRO:" (:erro resp))
                                 (println titulo "realizada com sucesso!")))
                             (println "Quantidade deve ser positiva."))
                           (println "Valor inválido."))))))))))))))

(defn acao-comprar [] (realizar-operacao :compra))
(defn acao-vender  [] (realizar-operacao :venda))

;; ==================================================
;;            EXTRATO
;; ==================================================
(defn obter-e-imprimir-extrato [inicio-param fim-param]
  (let [query   (str "/extrato?inicio=" inicio-param "&fim=" fim-param)
        extrato (h/get-json query)]

    (if (or (:erro extrato) (empty? extrato))
      (println "Sem transações para este período.")
      (do
        (println "\n--- EXTRATO FINANCEIRO (Período Solicitado) ---")
        (h/imprimir-lista-extrato extrato)))))

(defn extrato-por-periodo []
  (print "Digite a data de INÍCIO (YYYY-MM-DD): ") (flush)
  (let [inicio (h/ler-entrada)]
    (if (str/blank? inicio)
      (println "Operação cancelada.")

      (do
        (print "Digite a data de FIM (YYYY-MM-DD): ") (flush)
        (let [fim (h/ler-entrada)]
          (if (str/blank? fim)
            (println "Operação cancelada.")
              (obter-e-imprimir-extrato inicio fim)))))))

(defn acao-extrato []
  (println "\n--- CONSULTAR EXTRATO ---")
  (println "1. Extrato Completo")
  (println "2. Extrato por Período")
  (print "Escolha uma opção: ") (flush)

  (let [op (h/ler-entrada)]
    (case op
      "1" (obter-e-imprimir-extrato
           "2020-01-01"
           "2030-12-31")

      "2" (extrato-por-periodo)

      (println "Opção inválida."))))

;; ==================================================
;;            SALDO
;; ==================================================
(defn acao-saldo []
  (println "\n--- SALDO E PATRIMONIO ---")
  (println "(Consultando valores de mercado...)")

  (let [resp (h/get-json "/carteira")]
    (if (or (:erro resp) (empty? resp))
      (println "Carteira vazia.")
      (h/imprimir-lista-saldo resp 0.0))))

;; ==================================================
;;            MENU PRINCIPAL
;; ==================================================
(defn menu-principal []
  (println "\n-----------------------------")
  (println "      KAYMARKET AÇÕES")
  (println "-----------------------------")
  (println "1. Consultar Cotação Atual")
  (println "2. Comprar Ações")
  (println "3. Vender Ações")
  (println "4. Consultar Extrato")
  (println "5. Ver Saldo da Carteira")
  (println "6. Sair")
  (print "Escolha uma opção: ") (flush)
  (let [op (h/ler-entrada)]
    (case op
      "1" (do (acao-consultar) (recur))
      "2" (do (acao-comprar)   (recur))
      "3" (do (acao-vender)    (recur))
      "4" (do (acao-extrato)   (recur))
      "5" (do (acao-saldo)     (recur))
      "6" (println "Saindo... Até logo!")
      (do (println "Opção inválida.") (recur)))))

(defn -main []
  (menu-principal))
